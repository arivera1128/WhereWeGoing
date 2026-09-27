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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun ProfilePage(
    familySize: String, onFamilySize: (String) -> Unit, kids: String, onKids: (String) -> Unit,
    ageGroups: Set<String>, onAgeGroups: (Set<String>) -> Unit, zip: String, onZip: (String) -> Unit,
    radius: Int, onRadius: (Int) -> Unit, editing: Boolean, onEdit: () -> Unit, onSave: () -> Unit
) {
    PageColumn {
        if (!editing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Saved household profile", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onEdit) { Text("✎ Edit") }
            }
            Text("Household: $familySize people")
            Text("Kids: $kids")
            Text("Kids' ages: ${ageGroups.joinToString().ifBlank { "Not specified" }}")
            Text("ZIP: $zip • Driving radius: $radius miles")
            Text("Your choices are saved on this device.", style = MaterialTheme.typography.bodySmall)
        } else {
            Text("Household profile", style = MaterialTheme.typography.headlineSmall)
            Text("Household size")
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (1..4).forEach { count ->
                    FilterChip(selected = familySize == "$count", onClick = { onFamilySize("$count") }, label = { Text("$count") })
                }
                FilterChip(selected = familySize.toIntOrNull()?.let { it > 4 } == true,
                    onClick = { onFamilySize("5") }, label = { Text("Other") })
            }
            if ((familySize.toIntOrNull() ?: 0) > 4) {
                OutlinedTextField(familySize, { onFamilySize(it.filter(Char::isDigit)) },
                    label = { Text("Enter household size") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Text("Number of kids")
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (0..3).forEach { count ->
                    FilterChip(selected = kids == "$count", onClick = { onKids("$count") }, label = { Text("$count") })
                }
                FilterChip(selected = (kids.toIntOrNull() ?: -1) > 3,
                    onClick = { onKids("4") }, label = { Text("Other") })
            }
            if ((kids.toIntOrNull() ?: 0) > 3) {
                OutlinedTextField(kids, { onKids(it.filter(Char::isDigit)) },
                    label = { Text("Enter number of kids") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            }
            Text("Kids' age groups (choose any)")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("0–4", "5–12", "13–17").forEach { age ->
                    FilterChip(
                        selected = age in ageGroups,
                        onClick = { onAgeGroups(if (age in ageGroups) ageGroups - age else ageGroups + age) },
                        label = { Text(age) }
                    )
                }
            }
            OutlinedTextField(zip, { onZip(it.filter(Char::isDigit).take(5)) },
                label = { Text("ZIP code") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
            Text("Driving radius")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15).forEach { miles ->
                    FilterChip(selected = radius == miles, onClick = { onRadius(miles) }, label = { Text("$miles mi") })
                }
            }
            Text("Only Elk Grove places are available in this prototype.", style = MaterialTheme.typography.bodySmall)
            Button(
                enabled = (familySize.toIntOrNull() ?: 0) > 0 &&
                    (kids.toIntOrNull() ?: -1) in 0..(familySize.toIntOrNull() ?: 0) && zip.length == 5,
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save profile") }
        }
    }
}

@Composable
fun RemovedPage(removed: Set<String>, onRestore: (String) -> Unit) {
    PageColumn {
        Text("Removed places", style = MaterialTheme.typography.headlineSmall)
        Text("These places won't appear in recommendations. You can restore them anytime.")
        if (removed.isEmpty()) Text("No places removed yet.")
        elkGroveDeals.filter { it.id in removed }.forEach { deal ->
            Card(Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(deal.name, modifier = Modifier.weight(1f))
                    TextButton(onClick = { onRestore(deal.id) }) { Text("Restore") }
                }
            }
        }
    }
}

