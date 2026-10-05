package com.example.wherewegoing.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wherewegoing.*
import com.example.wherewegoing.model.MealRecord
import com.example.wherewegoing.model.QuizPlace
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun QuizPage(
    places: List<QuizPlace>,
    ratings: Map<String, Int>,
    targetCount: Int,
    editingPlaceId: String?,
    previewMode: Boolean,
    removedPlaces: Set<String>,
    removeInfoSuppressed: Boolean,
    onRate: (QuizPlace, Int) -> Unit,
    onEdit: (QuizPlace) -> Unit,
    onBackToProfile: () -> Unit,
    onRateMore: () -> Unit,
    onRemove: (QuizPlace) -> Unit,
    onSuppressRemoveInfo: () -> Unit,
    onPreviewNewUser: () -> Unit,
    onExitPreview: () -> Unit,
    onSeePicks: () -> Unit
) {
    val currentPlace = editingPlaceId?.let { id -> places.find { it.id == id } }
        ?: if (ratings.size < targetCount) nextQuizPlace(ratings, places) else null
    val answeredCount = ratings.size
    var searchText by remember { mutableStateOf("") }
    var removalCandidate by remember { mutableStateOf<QuizPlace?>(null) }
    var doNotShowRemovalInfo by remember { mutableStateOf(false) }

    fun requestRemoval(place: QuizPlace) {
        if (removeInfoSuppressed) onRemove(place) else removalCandidate = place
    }

    PageColumn {
        if (previewMode) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Previewing the first-time experience", style = MaterialTheme.typography.titleMedium)
                    Text("These answers are temporary. Your saved food profile will not change.")
                    TextButton(onClick = onExitPreview) { Text("Return to my saved profile") }
                }
            }
        }

        if (currentPlace != null) {
            Text(
                when {
                    editingPlaceId != null -> "Update your preference"
                    answeredCount == 0 -> "Build your food profile"
                    else -> "Add to your food profile"
                },
                style = MaterialTheme.typography.headlineSmall
            )
            if (editingPlaceId == null) {
                Text("Place ${answeredCount + 1} of $targetCount")
            }
            Text("Rate your general preference. You can change any answer later.")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("How do you feel about this place?", style = MaterialTheme.typography.titleMedium)
                    Text(currentPlace.name, style = MaterialTheme.typography.headlineMedium)
                    Text(currentPlace.description)
                }
            }

            RatingPanel(
                currentRating = ratings[currentPlace.id],
                onRate = { rating -> onRate(currentPlace, rating) }
            )

            if (currentPlace.id !in removedPlaces) {
                TextButton(onClick = { requestRemoval(currentPlace) }) {
                    Text("Remove this place from recommendations")
                }
            } else {
                Text("This place is removed from recommendations.", style = MaterialTheme.typography.bodySmall)
            }

            Text(
                "Ratings help personalize recommendations. Removing a place excludes it completely.",
                style = MaterialTheme.typography.bodySmall
            )

            if (editingPlaceId != null) {
                OutlinedButton(onClick = onBackToProfile, modifier = Modifier.fillMaxWidth()) {
                    Text("← Back to food profile")
                }
            }
        } else {
            Text("Your food profile", style = MaterialTheme.typography.headlineSmall)

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Thanks! Your food profile is ready.", style = MaterialTheme.typography.titleLarge)
                    Text("We'll use your preferences to find deals that fit you.")
                    Button(onClick = onSeePicks, modifier = Modifier.fillMaxWidth()) {
                        Text("See your picks")
                    }
                }
            }

            if (previewMode) {
                OutlinedButton(onClick = onExitPreview, modifier = Modifier.fillMaxWidth()) {
                    Text("Return to my saved profile")
                }
            } else if (ratings.size < places.size) {
                Button(onClick = onRateMore, modifier = Modifier.fillMaxWidth()) {
                    Text("Rate more places — ${places.size - ratings.size} available")
                }
            } else {
                Text("You've rated all places currently available in this prototype.")
            }

            Text("Your saved ratings", style = MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                label = { Text("Search your rated places") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val matchingPlaces = places.filter {
                it.id in ratings && it.name.contains(searchText.trim(), ignoreCase = true)
            }
            if (matchingPlaces.isEmpty()) {
                Text("No rated places match your search.")
            }
            matchingPlaces.forEach { place ->
                SavedRatingRow(
                    place = place,
                    rating = ratings[place.id] ?: 0,
                    onEdit = { onEdit(place) }
                )
            }

            if (!previewMode) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Prototype tools", style = MaterialTheme.typography.titleMedium)
                        Text("Compare the first-time flow without deleting your saved answers.")
                        OutlinedButton(onClick = onPreviewNewUser, modifier = Modifier.fillMaxWidth()) {
                            Text("Preview first-time experience")
                        }
                    }
                }
            }

            Text(
                "Ratings help personalize recommendations. Removed places can be restored from Removed places.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    removalCandidate?.let { place ->
        AlertDialog(
            onDismissRequest = { removalCandidate = null },
            title = { Text("Remove ${place.name}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("It will no longer appear in your recommendations. Visit Removed places from the menu to review or restore the list.")
                    Row {
                        Checkbox(
                            checked = doNotShowRemovalInfo,
                            onCheckedChange = { doNotShowRemovalInfo = it }
                        )
                        Text("Don't show this message again", modifier = Modifier.padding(top = 12.dp))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onRemove(place)
                    if (doNotShowRemovalInfo) onSuppressRemoveInfo()
                    removalCandidate = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { removalCandidate = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun RatingPanel(currentRating: Int?, onRate: (Int) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                (1..5).forEach { rating ->
                    FilterChip(
                        selected = currentRating == rating,
                        onClick = { onRate(rating) },
                        label = { Text("$rating") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Not for me", style = MaterialTheme.typography.bodySmall)
                Text("Favorite", style = MaterialTheme.typography.bodySmall)
            }
            OutlinedButton(onClick = { onRate(0) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (currentRating == 0) "Haven't tried — selected" else "I haven't tried this place")
            }
        }
    }
}

@Composable
private fun SavedRatingRow(place: QuizPlace, rating: Int, onEdit: () -> Unit) {
    val ratingLabel = when (rating) {
        0 -> "Haven't tried"
        1 -> "Not for me"
        2 -> "Usually not"
        3 -> "It depends"
        4 -> "Sounds good"
        else -> "Favorite"
    }
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(place.name, style = MaterialTheme.typography.titleMedium)
                Text(if (rating == 0) ratingLabel else "$rating of 5 • $ratingLabel")
            }
            TextButton(onClick = onEdit) { Text("Edit") }
        }
    }
}



