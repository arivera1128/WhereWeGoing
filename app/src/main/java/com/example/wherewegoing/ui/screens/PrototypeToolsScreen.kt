package com.example.wherewegoing.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.wherewegoing.domain.dayName
import com.example.wherewegoing.model.HouseholdProfile
import java.util.Calendar

@Composable
fun PrototypeToolsPage(
    simulatedDay: Int?,
    household: HouseholdProfile,
    ratedPlaceCount: Int,
    onSelectDay: (Int?) -> Unit,
    onPreviewNewUser: () -> Unit,
    onReset: () -> Unit,
    catalogStatus: String = ""
) {
    var confirmReset by remember { mutableStateOf(false) }
    val actualDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)

    PageColumn {
        Text("Prototype testing tools", style = MaterialTheme.typography.headlineMedium)
        Text("These controls are available only in debug builds.")
        SharedCatalogProofCard()
        if(catalogStatus.isNotBlank()) Text(catalogStatus)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Current test state", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (simulatedDay == null) "Day: Actual (${dayName(actualDay)})"
                    else "Day: Simulated ${dayName(simulatedDay)}"
                )
                Text("Household: ${household.adults} adult(s), ${household.childCount} child(ren)")
                Text("Food profile: $ratedPlaceCount rated place(s)")
            }
        }

        Text("Simulate day", style = MaterialTheme.typography.titleLarge)
        Text("This changes deal timing inside the app. It does not change the emulator clock.")
        DayButton(
            label = "Use actual day (${dayName(actualDay)})",
            selected = simulatedDay == null,
            modifier = Modifier.fillMaxWidth(),
            onClick = { onSelectDay(null) }
        )
        listOf(
            listOf(Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY),
            listOf(Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY)
        ).forEach { days ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                days.forEach { day ->
                    DayButton(
                        label = dayName(day).take(3),
                        selected = simulatedDay == day,
                        modifier = Modifier.weight(1f),
                        onClick = { onSelectDay(day) }
                    )
                }
            }
        }

        Text("Experience state", style = MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = onPreviewNewUser, modifier = Modifier.fillMaxWidth()) {
            Text("Preview new-user Food Profile")
        }
        Text("Preview mode does not change saved household or ratings.", style = MaterialTheme.typography.bodySmall)

        Text("Local prototype data", style = MaterialTheme.typography.titleLarge)
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
            Text("Start fresh")
        }
        Text(
            "Clears household, ratings, plans, check-ins, removed places, meal history, and test-day settings.",
            style = MaterialTheme.typography.bodySmall
        )
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Start with a new user?") },
            text = { Text("This permanently clears all local prototype data and returns to onboarding.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmReset = false
                    onReset()
                }) { Text("Clear data and start fresh") }
            },
            dismissButton = {
                TextButton(onClick = { confirmReset = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DayButton(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (selected) {
        Button(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        OutlinedButton(onClick = onClick, modifier = modifier) { Text(label) }
    }
}
