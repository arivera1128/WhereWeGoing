package com.example.wherewegoing.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI

data class ProofOffer(val restaurant: String, val address: String, val offer: String)

/** Development endpoint reader. Does not import offers into Room or feed recommendations. */
class SharedCatalogProof(private val projectUrl: String, private val publishableKey: String) {
    suspend fun read(): List<ProofOffer> = withContext(Dispatchers.IO) {
        val base = URI(projectUrl.trim().trimEnd('/'))
        require(base.scheme == "https" && base.host?.endsWith(".supabase.co") == true &&
            base.userInfo == null && base.query == null && base.fragment == null &&
            base.path.orEmpty().isEmpty() && base.port == -1) { "Use your development project's HTTPS Supabase URL." }
        require(publishableKey.startsWith("sb_publishable_") ||
            legacyAnonymousKey(publishableKey)) { "Use a publishable key, never a secret or service-role key." }
        val connection = base.resolve("/rest/v1/rpc/ww_proof_catalog").toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("apikey", publishableKey)
            if (!publishableKey.startsWith("sb_publishable_")) {
                connection.setRequestProperty("Authorization", "Bearer $publishableKey")
            }
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            connection.outputStream.use { it.write("{}".toByteArray()) }
            check(connection.responseCode == 200) { "Shared catalog request failed (${connection.responseCode}). Check the development setup." }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    check(output.size() + count <= 65_536) { "Development response exceeded its limit." }
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            val root = JSONObject(bytes.toString(Charsets.UTF_8))
            check(root.getString("environment") == "DEV" && root.getBoolean("test_data")) {
                "This tool accepts only the development proof catalog."
            }
            val rows = root.getJSONArray("offers")
            (0 until rows.length()).map { index ->
                val row = rows.getJSONObject(index)
                val offer = row.getString("offer")
                check(offer.startsWith("TEST ONLY")) { "Unexpected non-test offer in proof catalog." }
                ProofOffer(row.getString("restaurant_name"), row.getString("address"), offer)
            }
        } finally { connection.disconnect() }
    }

    private fun legacyAnonymousKey(key: String): Boolean = try {
        val payload = key.split('.')[1]
        val decoded = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE or android.util.Base64.NO_WRAP)
        JSONObject(decoded.toString(Charsets.UTF_8)).optString("role") == "anon"
    } catch (_: Exception) { false }
}
