package com.example.wherewegoing.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.wherewegoing.model.QuizPlace
import com.example.wherewegoing.nextQuizPlace

private const val ONBOARDING_QUIZ_SIZE = 5
private val supportedElkGroveZips = setOf("95624", "95757", "95758")

@Composable
fun OnboardingScreen(
    places: List<QuizPlace>,
    onComplete: (zip: String, adults: Int, childAges: List<Int>, ratings: Map<String, Int>) -> Unit
) {
    var step by remember { mutableStateOf("location") }
    var zip by remember { mutableStateOf("") }
    var showLocationMessage by remember { mutableStateOf(false) }
    var adults by remember { mutableStateOf<Int?>(null) }
    var children by remember { mutableStateOf<Int?>(null) }
    var customAdults by remember { mutableStateOf("") }
    var customChildren by remember { mutableStateOf("") }
    var childAges by remember { mutableStateOf<List<Int?>>(emptyList()) }
    var ratings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    val adultCount = if (adults == -1) customAdults.toIntOrNull() else adults
    val childCount = if (children == -1) customChildren.toIntOrNull() else children

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ChickLogo()

        when (step) {
            "location" -> {
                Text("Let's find dinner that fits you.", style = MaterialTheme.typography.headlineMedium)
                Text("We'll use your location, household, and food preferences to recommend nearby deals.")
                OutlinedButton(onClick = { showLocationMessage = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Use my location")
                }
                Text("or", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = zip,
                    onValueChange = { zip = it.filter(Char::isDigit).take(5) },
                    label = { Text("ZIP code") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (zip.length == 5 && zip !in supportedElkGroveZips) {
                    Text(
                        "This prototype currently supports Elk Grove ZIP codes 95624, 95757, and 95758.",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Button(
                    onClick = { step = "household" },
                    enabled = zip in supportedElkGroveZips,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue") }
                Text("We'll start with places within 10 miles. You can change this later.", style = MaterialTheme.typography.bodySmall)
            }

            "household" -> {
                Text("Who usually joins dinner?", style = MaterialTheme.typography.headlineMedium)
                Text("This helps us match deals with age and purchase requirements.")
                Text("Adults", style = MaterialTheme.typography.titleMedium)
                CountSelector(
                    choices = 1..4,
                    selected = adults,
                    customValue = customAdults,
                    onSelected = { adults = it; customAdults = "" },
                    onCustom = { adults = -1; customAdults = it.filter(Char::isDigit).take(2) }
                )
                Text("Children", style = MaterialTheme.typography.titleMedium)
                CountSelector(
                    choices = 0..4,
                    selected = children,
                    customValue = customChildren,
                    onSelected = { children = it; customChildren = "" },
                    onCustom = { children = -1; customChildren = it.filter(Char::isDigit).take(2) }
                )
                OutlinedButton(onClick = { step = "location" }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                Button(
                    onClick = {
                        val count = childCount ?: 0
                        childAges = List(count) { index -> childAges.getOrNull(index) }
                        step = if (count == 0) "food" else "ages"
                    },
                    enabled = adultCount != null && adultCount > 0 && childCount != null && childCount >= 0,
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue") }
            }

            "ages" -> {
                Text("How old are the children?", style = MaterialTheme.typography.headlineMedium)
                Text("We only use current ages to check deal eligibility. No names or birthdays are needed.")
                childAges.forEachIndexed { index, age ->
                    AgeSelector(
                        label = "Child ${index + 1}",
                        age = age,
                        onAge = { selectedAge ->
                            childAges = childAges.toMutableList().also { it[index] = selectedAge }
                        }
                    )
                }
                OutlinedButton(onClick = { step = "household" }, modifier = Modifier.fillMaxWidth()) { Text("Back") }
                Button(
                    onClick = { step = "food" },
                    enabled = childAges.all { it != null },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Continue") }
            }

            "food" -> {
                val quizSize = minOf(ONBOARDING_QUIZ_SIZE, places.size)
                val place = nextQuizPlace(ratings, places)
                Text("Build your food profile", style = MaterialTheme.typography.headlineMedium)
                Text("Place ${ratings.size + 1} of $quizSize")
                if (place != null) {
                    Text(place.name, style = MaterialTheme.typography.headlineSmall)
                    Text(place.description)
                    Text("How much do you generally like this place?", style = MaterialTheme.typography.titleMedium)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..5).forEach { rating ->
                            OutlinedButton(
                                onClick = {
                                    ratings = ratings + (place.id to rating)
                                    if (ratings.size >= quizSize) step = "complete"
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(rating.toString()) }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Not appealing", style = MaterialTheme.typography.bodySmall)
                        Text("Great choice", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(
                        onClick = {
                            ratings = ratings + (place.id to 0)
                            if (ratings.size >= quizSize) step = "complete"
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("I haven't tried this place") }
                }
                if (place == null) Button(onClick = { step = "complete" }) { Text("Continue") }
                TextButton(onClick = { step = if ((childCount ?: 0) > 0) "ages" else "household" }) { Text("Back") }
            }

            "complete" -> {
                val directRatings = ratings.values.count { it in 1..5 }
                Text(
                    if (directRatings == 0) "Your food profile is started." else "You're ready for your first pick!",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    if (directRatings == 0) {
                        "We don't know your tastes well yet, but we can still find a nearby deal. Rating more places later will improve your recommendations."
                    } else {
                        "We'll use your location, household details, and food preferences to find a deal that fits."
                    }
                )
                Button(
                    onClick = { onComplete(zip, adultCount ?: 1, childAges.filterNotNull(), ratings) },
                    modifier = Modifier.fillMaxWidth().height(64.dp)
                ) { Text("See my first pick") }
            }
        }
    }

    if (showLocationMessage) {
        AlertDialog(
            onDismissRequest = { showLocationMessage = false },
            title = { Text("Use my location") },
            text = { Text("Automatic location isn't available in this prototype yet. Enter your ZIP code to continue.") },
            confirmButton = { TextButton(onClick = { showLocationMessage = false }) { Text("Enter ZIP code") } }
        )
    }
}

@Composable
private fun CountSelector(
    choices: IntRange,
    selected: Int?,
    customValue: String,
    onSelected: (Int) -> Unit,
    onCustom: (String) -> Unit
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        choices.forEach { value ->
            FilterChip(selected = selected == value && customValue.isBlank(), onClick = { onSelected(value) }, label = { Text(value.toString()) })
        }
        FilterChip(selected = selected == -1, onClick = { onCustom(customValue) }, label = { Text("Other") })
    }
    if (selected == -1) {
        OutlinedTextField(
            value = customValue,
            onValueChange = onCustom,
            label = { Text("Enter number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AgeSelector(label: String, age: Int?, onAge: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(label, style = MaterialTheme.typography.titleMedium)
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(age?.let { "$it years old" } ?: "Select age")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (0..17).forEach { value ->
                DropdownMenuItem(
                    text = { Text(if (value == 0) "Under 1" else "$value") },
                    onClick = { onAge(value); expanded = false }
                )
            }
        }
    }
}
