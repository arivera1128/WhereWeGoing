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
import com.example.wherewegoing.model.PlaceDeal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
@Composable
fun PicksPage(
    displayedPick: PlaceDeal?, isUserSelected: Boolean, hasTonightDeal: Boolean,
    alternatives: List<PlaceDeal>, zip: String,
    dealAppeal: String, plannedDealId: String, onDealAppeal: (PlaceDeal, String) -> Unit,
    onPlanToTry: (PlaceDeal) -> Unit, onUpdateLocation: () -> Unit, onReviewRemoved: () -> Unit,
    onChoose: (PlaceDeal) -> Unit, onOpen: (PlaceDeal) -> Unit
) {
    PageColumn {
        Text(
            if (isUserSelected) "Your selected deal" else "Tonight's pick",
            style = MaterialTheme.typography.headlineSmall
        )
        if (displayedPick == null) {
            if (zip !in setOf("95624", "95757", "95758")) {
                Text("We don't have trustworthy recommendations for this area yet. This prototype currently supports Elk Grove.")
                Button(onClick = onUpdateLocation, modifier = Modifier.fillMaxWidth()) { Text("Update location") }
            } else {
                Text("There are no trustworthy places available to recommend right now. We won't invent a match.")
                Button(onClick = onReviewRemoved, modifier = Modifier.fillMaxWidth()) { Text("Review removed places") }
            }
        } else {
            if (!hasTonightDeal) {
                Text("No strong confirmed deal today. Try a place you might like, and check its current offer.")
            }
            DealTile(displayedPick, featured = true, onOpen = { onOpen(displayedPick) }) {
                if (plannedDealId == displayedPick.id) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("Saved. We'll check in the next time you open the app.", modifier = Modifier.padding(14.dp))
                    }
                } else {
                    Button(onClick = { onPlanToTry(displayedPick) }, modifier = Modifier.fillMaxWidth()) {
                        Text("I'll try this deal")
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text("Is this deal useful?", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (dealAppeal == "useful") {
                        Button(onClick = { onDealAppeal(displayedPick, "useful") }) { Text("👍 Useful") }
                    } else {
                        OutlinedButton(onClick = { onDealAppeal(displayedPick, "useful") }) { Text("👍 Useful") }
                    }
                    if (dealAppeal == "not_useful") {
                        Button(onClick = { onDealAppeal(displayedPick, "not_useful") }) { Text("👎 Not useful") }
                    } else {
                        OutlinedButton(onClick = { onDealAppeal(displayedPick, "not_useful") }) { Text("👎 Not useful") }
                    }
                }
            }
            Text(
                if (isUserSelected) "You chose this from tonight's options."
                else if (hasTonightDeal) "Why this pick? This confirmed deal is available today and fits your saved profile."
                else "Why this pick? There is no strong deal today, so this is a place you may like while we wait for a better match.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
        }
        Text("Other options", style = MaterialTheme.typography.titleLarge)
        alternatives.forEach { deal ->
            DealTile(deal, onOpen = { onOpen(deal) }) {
                Button(onClick = { onChoose(deal) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Choose this deal")
                }
            }
        }
    }
}

@Composable
private fun DealTile(
    deal: PlaceDeal,
    featured: Boolean = false,
    onOpen: () -> Unit,
    content: (@Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (featured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(deal.name, style = MaterialTheme.typography.titleLarge)
            Text(deal.category, style = MaterialTheme.typography.bodySmall)
            Text(deal.offer)
            Text(if (deal.verified) "Source checked ${deal.checked}" else "Confirm with location", style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onOpen) { Text("View details →") }
            content?.invoke(this)
        }
    }
}

@Composable
fun DetailPage(
    deal: PlaceDeal,
    removed: Boolean,
    backLabel: String,
    onBack: () -> Unit,
    onRemove: () -> Unit,
    onSource: () -> Unit
) {
    PageColumn {
        TextButton(onClick = onBack) { Text(backLabel) }
        Text(deal.name, style = MaterialTheme.typography.headlineMedium)
        Text(deal.category)
        Text(deal.offer, style = MaterialTheme.typography.titleLarge)
        Text("Where: ${deal.address}")
        Text("Details: ${deal.terms}")
        Text(if (deal.verified) "Source checked ${deal.checked}" else "Not confirmed at this location")
        OutlinedButton(onClick = onSource) { Text("View deal source") }
        if (!removed) {
            OutlinedButton(onClick = onRemove) { Text("Remove this place from recommendations") }
            Text("You can restore it from Removed places.", style = MaterialTheme.typography.bodySmall)
        }
    }
}


