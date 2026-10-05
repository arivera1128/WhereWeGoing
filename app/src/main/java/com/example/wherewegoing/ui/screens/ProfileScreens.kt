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
import com.example.wherewegoing.model.HouseholdProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun ProfilePage(
    profile: HouseholdProfile,
    onProfileChange: (HouseholdProfile) -> Unit,
    editing: Boolean,
    onEdit: () -> Unit,
    onSave: () -> Unit
) {
    PageColumn {
        if (!editing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saved household profile", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onEdit) { Text("✎ Edit") }
            }
            Text("Adults: ${profile.adults}")
            Text("Children: ${profile.childCount}")
            if (profile.childCount > 0) {
                Text("Children's ages: ${profile.childAges.joinToString { it?.toString() ?: "Needs age" }}")
            }
            Text("ZIP: ${profile.zip} • Radius: ${profile.radiusMiles} miles")
            Text("Your choices are saved on this device.", style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Household profile", style = MaterialTheme.typography.headlineSmall)
            Text("Adults", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (1..4).forEach { count ->
                    FilterChip(
                        selected = profile.adults == count,
                        onClick = { onProfileChange(profile.copy(adults = count)) },
                        label = { Text("$count") }
                    )
                }
                FilterChip(
                    selected = profile.adults > 4,
                    onClick = { onProfileChange(profile.copy(adults = 5)) },
                    label = { Text("Other") }
                )
            }
            if (profile.adults > 4) {
                OutlinedTextField(
                    value = profile.adults.toString(),
                    onValueChange = { value ->
                        value.filter(Char::isDigit).take(2).toIntOrNull()?.let {
                            onProfileChange(profile.copy(adults = it))
                        }
                    },
                    label = { Text("Enter number of adults") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("Children", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (0..4).forEach { count ->
                    FilterChip(
                        selected = profile.childCount == count,
                        onClick = {
                            onProfileChange(profile.copy(childAges = List(count) { index -> profile.childAges.getOrNull(index) }))
                        },
                        label = { Text("$count") }
                    )
                }
                FilterChip(
                    selected = profile.childCount > 4,
                    onClick = {
                        onProfileChange(profile.copy(childAges = List(5) { index -> profile.childAges.getOrNull(index) }))
                    },
                    label = { Text("Other") }
                )
            }
            if (profile.childCount > 4) {
                OutlinedTextField(
                    value = profile.childCount.toString(),
                    onValueChange = { value ->
                        value.filter(Char::isDigit).take(2).toIntOrNull()?.let { count ->
                            onProfileChange(profile.copy(childAges = List(count) { index -> profile.childAges.getOrNull(index) }))
                        }
                    },
                    label = { Text("Enter number of children") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            profile.childAges.forEachIndexed { index, age ->
                ProfileAgeSelector("Child ${index + 1}", age) { selectedAge ->
                    val ages = profile.childAges.toMutableList().also { it[index] = selectedAge }
                    onProfileChange(profile.copy(childAges = ages))
                }
            }
            OutlinedTextField(
                value = profile.zip,
                onValueChange = { onProfileChange(profile.copy(zip = it.filter(Char::isDigit).take(5))) },
                label = { Text("ZIP code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )
            Text("Search radius", style = MaterialTheme.typography.titleMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { miles ->
                    FilterChip(
                        selected = profile.radiusMiles == miles,
                        onClick = { onProfileChange(profile.copy(radiusMiles = miles)) },
                        label = { Text("$miles mi") }
                    )
                }
            }
            Text("Only Elk Grove places are available in this prototype.", style = MaterialTheme.typography.bodySmall)
            Button(
                enabled = profile.adults > 0 && profile.childAges.all { it != null } && profile.zip.length == 5,
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save profile") }
        }
    }
}

@Composable
private fun ProfileAgeSelector(label: String, age: Int?, onAge: (Int) -> Unit) {
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

@Composable
fun RemovedPage(removed: Set<String>, deals: List<com.example.wherewegoing.model.PlaceDeal>, onRestore: (String) -> Unit) {
    PageColumn {
        Text("Removed places", style = MaterialTheme.typography.headlineSmall)
        Text("These places won't appear in recommendations. You can restore them anytime.")
        if (removed.isEmpty()) Text("No places removed yet.")
        deals.filter { it.id in removed }.forEach { deal ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(deal.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRestore(deal.id) }) { Text("Restore") }
                }
            }
        }
    }
}


