package com.example.wherewegoing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URI
import java.net.HttpURLConnection

class SupabaseCatalogSource(private val projectUrl: String, private val key: String) : SharedCatalogSource {
    override val identity = projectUrl.trim().trimEnd('/')
    override suspend fun fetch(): SharedCatalog = withContext(Dispatchers.IO) {
        val uri=URI(identity)
        require(uri.scheme=="https" && uri.host?.endsWith(".supabase.co")==true && uri.userInfo==null &&
            uri.query==null && uri.fragment==null && uri.port == -1 && uri.path.orEmpty().isEmpty()) { "Invalid shared project URL" }
        val publishable=key.startsWith("sb_publishable_")
        val anon=try {
            val payload=android.util.Base64.decode(key.split('.')[1],android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
            JSONObject(payload.toString(Charsets.UTF_8)).optString("role")=="anon"
        } catch (_: Exception) { false }
        require(publishable || anon) { "Use only a publishable client key" }
        val connection=uri.resolve("/rest/v1/rpc/ww_app_catalog").toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod="POST"; connection.connectTimeout=10_000; connection.readTimeout=10_000
            connection.instanceFollowRedirects=false; connection.doOutput=true
            connection.setRequestProperty("apikey",key)
            connection.setRequestProperty("Content-Type","application/json")
            if (!publishable) connection.setRequestProperty("Authorization","Bearer $key")
            connection.outputStream.use { it.write("{}".toByteArray()) }
            check(connection.responseCode==200) { "Shared catalog unavailable (${connection.responseCode})" }
            val text=connection.inputStream.use { input ->
                val output=java.io.ByteArrayOutputStream(); val buffer=ByteArray(8192)
                while(true) {
                    val count=input.read(buffer); if(count < 0) break
                    check(output.size()+count <= 1_048_576) { "Catalog exceeds supported snapshot size" }
                    output.write(buffer,0,count)
                }
                output.toString("UTF-8")
            }
            parseSharedCatalog(JSONObject(text))
        } finally { connection.disconnect() }
    }
}

internal fun parseSharedCatalog(root: JSONObject): SharedCatalog {
    require(root.getInt("schema_version")==1 && root.getBoolean("complete") &&
        root.getString("environment")=="DEV" && root.getString("market")=="ELK_GROVE") { "Unsupported or incomplete catalog" }
    fun status(row: JSONObject): Boolean {
        val value=row.getString("status")
        require(value in setOf("ACTIVE","INACTIVE")) { "Unknown catalog status" }
        return value=="ACTIVE"
    }
    val restaurants=root.getJSONArray("restaurants")
    val locations=root.getJSONArray("locations")
    val offers=root.getJSONArray("offers")
    return SharedCatalog(
        (0 until restaurants.length()).map { index -> restaurants.getJSONObject(index).let {
            SharedRestaurant(it.getString("id"),it.getString("key"),it.getString("name"),status(it))
        } },
        (0 until locations.length()).map { index -> locations.getJSONObject(index).let {
            SharedLocation(it.getString("id"),it.getString("restaurant_id"),
                if(it.isNull("legacy_key")) null else it.getString("legacy_key"),
                it.getString("address"),it.getString("time_zone"),status(it))
        } },
        (0 until offers.length()).map { index -> offers.getJSONObject(index).let {
            val days=it.getJSONArray("days")
            SharedOffer(it.getString("id"),it.getString("deal_id"),it.getString("restaurant_id"),it.getString("location_id"),
                it.getInt("version"),it.getLong("published_at"),it.getString("offer"),it.getString("terms"),
                (0 until days.length()).map { day -> days.getInt(day) }.toSet(),it.getBoolean("for_kids"),
                it.getInt("savings_rank"),it.getBoolean("verified"),it.getString("source"),it.getString("checked"))
        } }
    ).also { it.validate() }
}
