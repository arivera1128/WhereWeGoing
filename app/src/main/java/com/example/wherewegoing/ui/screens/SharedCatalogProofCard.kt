package com.example.wherewegoing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wherewegoing.BuildConfig
import com.example.wherewegoing.data.SharedCatalogProof
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SharedCatalogProofCard() {
    if (!BuildConfig.DEBUG) return
    val scope = rememberCoroutineScope()
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf("") }
    val configured = BuildConfig.PROOF_SUPABASE_URL.isNotBlank() && BuildConfig.PROOF_SUPABASE_KEY.isNotBlank()
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Shared catalog development test", style = MaterialTheme.typography.titleMedium)
            Text("Test offers only. This does not change your recommendations or saved data.")
            if (!configured) Text("Not connected yet. Follow backend/proof/README.md to configure the development project.")
            OutlinedButton(enabled = configured && !busy, onClick = {
                scope.launch {
                    busy = true
                    try {
                        val offers = SharedCatalogProof(BuildConfig.PROOF_SUPABASE_URL, BuildConfig.PROOF_SUPABASE_KEY).read()
                        result = if (offers.isEmpty()) "Connected. No published test offers yet."
                        else "Connected: ${offers.size} published test offer(s).\n" + offers.joinToString("\n\n") {
                            "${it.restaurant}\n${it.address}\n${it.offer}"
                        }
                    } catch (error: CancellationException) { throw error
                    } catch (error: Exception) { result = error.message ?: "Could not connect to the development catalog."
                    } finally { busy = false }
                }
            }) { Text(if (busy) "Checking…" else "Check shared catalog") }
            if (result.isNotBlank()) Text(result)
        }
    }
}
