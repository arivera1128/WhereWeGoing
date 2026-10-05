package com.example.wherewegoing.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.wherewegoing.data.local.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import org.json.JSONArray
import java.net.URI
import java.net.HttpURLConnection
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class SubmissionPlace(val restaurantId:String,val locationId:String,val name:String,val address:String)

/** Tokens stay encrypted in noBackupFilesDir; Room stores only the identity association. */
class TesterAccountRepository(context:Context,private val db:WhereWeGoingDatabase,private val url:String,private val key:String) {
 private val directory=context.noBackupFilesDir
 private val mutex=Mutex()
 private fun cipherKey():SecretKey {
  val store=KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
  val alias="wwg-tester-session"
  return (store.getKey(alias,null) as? SecretKey) ?: KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore").apply {
   init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
  }.generateKey()
 }
 private fun file(user:String)=java.io.File(directory,"tester-$user.session")
 private fun load(user:String):JSONObject? {
  if(!file(user).exists()) return null
  val wrapper=JSONObject(android.util.AtomicFile(file(user)).openRead().bufferedReader().use { it.readText() })
  require(wrapper.getString("project")==url) { "Tester project changed" }
  val cipher=Cipher.getInstance("AES/GCM/NoPadding")
  cipher.init(Cipher.DECRYPT_MODE,cipherKey(),GCMParameterSpec(128,Base64.decode(wrapper.getString("iv"),Base64.NO_WRAP)))
  return JSONObject(String(cipher.doFinal(Base64.decode(wrapper.getString("data"),Base64.NO_WRAP)),Charsets.UTF_8))
 }
 private fun save(user:String,session:JSONObject) {
  val cipher=Cipher.getInstance("AES/GCM/NoPadding");cipher.init(Cipher.ENCRYPT_MODE,cipherKey())
  val wrapper=JSONObject().put("project",url).put("iv",Base64.encodeToString(cipher.iv,Base64.NO_WRAP)).put("data",Base64.encodeToString(cipher.doFinal(session.toString().toByteArray()),Base64.NO_WRAP))
  val atomic=android.util.AtomicFile(file(user));val stream=atomic.startWrite()
  try { stream.write(wrapper.toString().toByteArray());atomic.finishWrite(stream) } catch(e:Exception){atomic.failWrite(stream);throw e}
 }
 private fun request(path:String,body:JSONObject,token:String?=null):String {
  val uri=URI(url)
  require(uri.scheme=="https" && uri.host?.endsWith(".supabase.co")==true && uri.rawUserInfo==null && uri.port == -1 && uri.path.orEmpty() in listOf("","/") && uri.query==null && uri.fragment==null)
  require(key.startsWith("sb_publishable_") || runCatching { JSONObject(String(Base64.decode(key.split('.')[1],Base64.URL_SAFE or Base64.NO_WRAP))).getString("role")=="anon" }.getOrDefault(false))
  val connection=uri.resolve(path).toURL().openConnection() as HttpURLConnection
  try {
   connection.requestMethod="POST";connection.doOutput=true;connection.instanceFollowRedirects=false
   connection.connectTimeout=10000;connection.readTimeout=10000
   connection.setRequestProperty("apikey",key);connection.setRequestProperty("Content-Type","application/json")
   if(token!=null) connection.setRequestProperty("Authorization","Bearer $token")
   connection.outputStream.use { it.write(body.toString().toByteArray()) }
   check(connection.responseCode in 200..299) { "Tester connection failed (${connection.responseCode}). Check anonymous sign-in and submission setup." }
   return connection.inputStream.use { input ->
    val bytes=input.readBytesLimited();String(bytes,Charsets.UTF_8)
   }
  } finally { connection.disconnect() }
 }
 private fun java.io.InputStream.readBytesLimited():ByteArray {
  val output=java.io.ByteArrayOutputStream();val buffer=ByteArray(8192)
  while(true){val n=read(buffer);if(n<0)break;check(output.size()+n<=1048576);output.write(buffer,0,n)}
  return output.toByteArray()
 }
 private suspend fun session(user:String):JSONObject {
  val old=load(user)
  require(old!=null || db.userAuthIdentityDao().find(user,url)==null) { "Tester session is missing; reset local test data to start again" }
  val next=if(old==null) JSONObject(request("/auth/v1/signup",JSONObject()))
   else if(old.getLong("expires_at")<=System.currentTimeMillis()/1000+60) JSONObject(request("/auth/v1/token?grant_type=refresh_token",JSONObject().put("refresh_token",old.getString("refresh_token")))) else old
  if(!next.has("expires_at")) next.put("expires_at",System.currentTimeMillis()/1000+next.getLong("expires_in"))
  val remote=next.getJSONObject("user").getString("id")
  val linked=db.userAuthIdentityDao().find(user,url)
  require(linked==null || linked.authUserId==remote) { "Tester identity changed; reset required" }
  // Save before linking: retry can recover a newly created identity after a local write failure.
  save(user,next)
  if(linked==null)db.userAuthIdentityDao().insert(UserAuthIdentity(user,url,remote))
  return next
 }
 suspend fun connect(user:String)=withContext(Dispatchers.IO){mutex.withLock { session(user);Unit }}
 suspend fun submit(user:String,id:String,restaurant:String?,location:String?,place:String,offer:String)=withContext(Dispatchers.IO){mutex.withLock {
  val token=session(user).getString("access_token")
  request("/rest/v1/rpc/ww_submit_deal",JSONObject().put("p_id",id).put("p_restaurant",restaurant ?: JSONObject.NULL).put("p_location",location ?: JSONObject.NULL).put("p_place",place.trim()).put("p_offer",offer.trim()),token);Unit
 }}
 suspend fun mine(user:String):JSONArray=withContext(Dispatchers.IO){mutex.withLock { JSONArray(request("/rest/v1/rpc/ww_my_submissions",JSONObject(),session(user).getString("access_token"))) }}
 suspend fun search(user:String,query:String):List<SubmissionPlace> = withContext(Dispatchers.IO){mutex.withLock {
  if(query.trim().length !in 2..100) return@withLock emptyList()
  val rows=JSONArray(request("/rest/v1/rpc/ww_search_places",JSONObject().put("p_query",query.trim()),session(user).getString("access_token")))
  List(rows.length()){ index -> val row=rows.getJSONObject(index);SubmissionPlace(row.getString("restaurant_id"),row.getString("location_id"),row.getString("name"),row.getString("address")) }
 }}
 fun reset(user:String){file(user).delete()}
}
