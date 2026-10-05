package com.example.wherewegoing.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wherewegoing.data.SubmissionPlace
import com.example.wherewegoing.data.TesterAccountRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.util.UUID

@Composable
fun DealSubmissionPage(userId: String, repository: TesterAccountRepository, onViewSubmissions: () -> Unit) {
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableStateOf(1) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedRestaurant by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedLocation by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedName by rememberSaveable { mutableStateOf("") }
    var selectedAddress by rememberSaveable { mutableStateOf("") }
    var missing by rememberSaveable { mutableStateOf(false) }
    var details by rememberSaveable { mutableStateOf("") }
    var offer by rememberSaveable { mutableStateOf("") }
    var submissionId by rememberSaveable { mutableStateOf(UUID.randomUUID().toString()) }
    var uncertain by rememberSaveable { mutableStateOf(false) }
    var sent by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var searching by remember { mutableStateOf(false) }
    var results by remember { mutableStateOf(emptyList<SubmissionPlace>()) }
    var searchError by remember { mutableStateOf(false) }
    var retrySearch by remember { mutableStateOf(0) }
    var message by remember { mutableStateOf("") }
    LaunchedEffect(query, retrySearch, step, missing) {
        results = emptyList(); searchError = false; searching = false
        if (step != 1 || missing || query.trim().length < 2) return@LaunchedEffect
        searching = true
        try {
            delay(400)
            results = repository.search(userId, query)
        } catch (e: CancellationException) { throw e }
        catch (_: Exception) { searchError = true }
        finally { searching = false }
    }
    BackHandler(enabled = step == 2 && !sent) { if (!busy && !uncertain) step = 1 }
    PageColumn {
        ChickLogo()
        Text("Share a deal", style = MaterialTheme.typography.headlineMedium)
        if (sent) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Thanks for sharing!", style = MaterialTheme.typography.headlineSmall)
                    Text("Your deal is waiting for review. You can follow its progress in My submissions.")
                }
            }
            Button(onClick = onViewSubmissions, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("View my submissions") }
            OutlinedButton(onClick = {
                sent = false; step = 1; offer = ""; details = ""; query = ""; missing = false
                selectedRestaurant = null; selectedLocation = null; selectedName = ""; selectedAddress = ""
                submissionId = UUID.randomUUID().toString()
            }, modifier = Modifier.fillMaxWidth()) { Text("Share another deal") }
        } else {
            Text("Step $step of 2", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            LinearProgressIndicator(progress = { step / 2f }, modifier = Modifier.fillMaxWidth())
            if (step == 1) {
                Text("Where is the deal?", style = MaterialTheme.typography.titleLarge)
                Text("Find the place, then choose the right location.")
                if (!missing) {
                    OutlinedTextField(value = query, onValueChange = { query = it.take(100) }, label = { Text("Search restaurant name") },
                        placeholder = { Text("Start typing, like Chevy’s") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    when {
                        query.trim().length < 2 -> Text("Enter at least 2 letters to search.", style = MaterialTheme.typography.bodySmall)
                        searching -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) { CircularProgressIndicator(Modifier.size(20.dp)); Text("Finding places…") }
                        searchError -> {
                            Text("Couldn’t search places. Try again.")
                            OutlinedButton(onClick = { retrySearch++ }) { Text("Retry search") }
                        }
                        results.isEmpty() -> Text("No matching places yet. Try another name or add the place below.")
                        else -> {
                            results.forEach { place ->
                                Card(onClick = {
                                    selectedRestaurant = place.restaurantId; selectedLocation = place.locationId
                                    selectedName = place.name; selectedAddress = place.address
                                    step = 2; message = ""; submissionId = UUID.randomUUID().toString()
                                }, modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                        Text(place.name, style = MaterialTheme.typography.titleMedium)
                                        Text(place.address, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                            Text("Showing up to 10 locations. Keep typing to narrow your search.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    TextButton(onClick = { missing = true; selectedRestaurant = null; selectedLocation = null }) { Text("Can’t find this place?") }
                } else {
                    OutlinedTextField(value = details, onValueChange = { details = it.take(1000) }, label = { Text("Place name and address") },
                        supportingText = { Text("Tell us enough to find the right location.") }, minLines = 3, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { step = 2; selectedName = "New place"; selectedAddress = details.trim(); submissionId = UUID.randomUUID().toString() },
                        enabled = details.trim().length >= 3, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Continue") }
                    TextButton(onClick = { missing = false }) { Text("Back to search") }
                }
                OutlinedButton(onClick = onViewSubmissions, modifier = Modifier.fillMaxWidth()) { Text("My submissions") }
            } else {
                Text("What’s the deal?", style = MaterialTheme.typography.titleLarge)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(selectedName, style = MaterialTheme.typography.titleMedium)
                        Text(selectedAddress)
                        TextButton(onClick = { step = 1 }, enabled = !busy && !uncertain) { Text("Change place") }
                    }
                }
                OutlinedTextField(value = offer, onValueChange = { offer = it.take(2000); submissionId = UUID.randomUUID().toString() },
                    enabled = !busy && !uncertain, label = { Text("Tell us about the deal") },
                    placeholder = { Text("Kids eat free on Tuesdays, ages 12 and under, with an adult meal.") },
                    minLines = 5, supportingText = { Text("Include days, times and restrictions if you know them.") }, modifier = Modifier.fillMaxWidth())
                Text("We’ll review the details before sharing this deal with others.", style = MaterialTheme.typography.bodySmall)
                if (message.isNotBlank()) Text(message, color = MaterialTheme.colorScheme.error)
                Button(onClick = { scope.launch {
                    busy = true; message = ""
                    try {
                        repository.submit(userId, submissionId, selectedRestaurant, selectedLocation,
                            if (missing) details else "$selectedName — $selectedAddress", offer)
                        uncertain = false; sent = true
                    } catch (e: CancellationException) { throw e }
                    catch (_: Exception) { uncertain = true; message = "Couldn’t confirm delivery. Retry this submission; it won’t create a duplicate." }
                    finally { busy = false }
                } }, enabled = !busy && offer.trim().isNotEmpty(), modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Text(if (busy) "Sending…" else if (uncertain) "Retry submission" else "Send for review")
                }
                OutlinedButton(onClick = { step = 1 }, enabled = !busy && !uncertain, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            }
        }
    }
}

@Composable
fun MySubmissionsPage(userId: String, repository: TesterAccountRepository, onShare: () -> Unit) {
    var entries by remember { mutableStateOf(JSONArray()) }
    var request by remember { mutableStateOf(0) }
    var loading by remember { mutableStateOf(true) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(userId, request) {
        loading = true; failed = false
        try { entries = repository.mine(userId) }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { failed = true }
        finally { loading = false }
    }
    PageColumn {
        Text("My submissions", style = MaterialTheme.typography.headlineMedium)
        Text("A little sharing can help someone find their next meal.")
        if (loading) CircularProgressIndicator()
        if (failed) Text("Couldn’t refresh your submissions. Please try again.")
        if (!loading && !failed && entries.length() == 0) Text("Nothing shared yet. Found a deal? Send it our way.")
        for (i in 0 until entries.length()) {
            val row = entries.getJSONObject(i)
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(row.getString("place"), style = MaterialTheme.typography.titleMedium)
                    Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.small) {
                        Text(when (row.getString("status")) { "PENDING" -> "Pending review"; "NEEDS_CLARIFICATION" -> "Needs clarification"; "APPROVED" -> "Approved"; else -> "Rejected" }, Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge)
                    }
                    Text(row.getString("offer"))
                    if (row.getString("note").isNotBlank()) Text(row.getString("note"), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Button(onClick = onShare, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Share a deal") }
        OutlinedButton(onClick = { request++ }, enabled = !loading, modifier = Modifier.fillMaxWidth()) { Text("Refresh") }
    }
}
