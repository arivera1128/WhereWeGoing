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
import com.example.wherewegoing.domain.dayName
import com.example.wherewegoing.domain.nextOfferDay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
@Composable
fun PicksPage(
    displayedPick: PlaceDeal?, isUserSelected: Boolean, hasTonightDeal: Boolean,
    alternatives: List<PlaceDeal>, zip: String, today: Int,
    ratings: Map<String, Int>, picksMessage: String, plannedDealId: String,
    onRate: (PlaceDeal, Int) -> Unit,
    onPlanToTry: (PlaceDeal) -> Unit, onUpdateLocation: () -> Unit, onReviewRemoved: () -> Unit,
    onChoose: (PlaceDeal) -> Unit, onOpen: (PlaceDeal) -> Unit
) {
    PageColumn {
        val featuredOfferToday = displayedPick?.let { today in it.days } == true
        Text(
            if (isUserSelected) "Your selected place"
            else if (displayedPick != null && !featuredOfferToday) "Restaurant pick"
            else "Tonight's pick",
            style = MaterialTheme.typography.headlineSmall
        )
        if (picksMessage.isNotBlank()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                Text(picksMessage, modifier = Modifier.fillMaxWidth().padding(14.dp))
            }
        }
        if (displayedPick == null) {
            if (zip !in setOf("95624", "95757", "95758")) {
                Text("We don't have trustworthy recommendations for this area yet. This prototype currently supports Elk Grove.")
                Button(onClick = onUpdateLocation, modifier = Modifier.fillMaxWidth()) { Text("Update location") }
            } else {
                Text(
                    if (alternatives.isNotEmpty()) "No good match tonight. You can still choose from the other options."
                    else "There are no trustworthy places available to recommend right now. We won't invent a match."
                )
                if (alternatives.isEmpty()) {
                    Button(onClick = onReviewRemoved, modifier = Modifier.fillMaxWidth()) { Text("Review removed places") }
                }
            }
        } else {
            if (!featuredOfferToday) {
                Text("No confirmed deal today. Here's a restaurant that fits your food profile.")
            } else if (!hasTonightDeal) {
                Text("Possible deal today — confirm with this location.")
            }
            DealTile(
                deal = displayedPick,
                featured = true,
                rating = ratings[displayedPick.id],
                offerToday = featuredOfferToday,
                onRate = { onRate(displayedPick, it) },
                onOpen = { onOpen(displayedPick) }
            ) {
                if (plannedDealId == displayedPick.id) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Text("Saved. We'll check in the next time you open the app.", modifier = Modifier.padding(14.dp))
                    }
                } else {
                    Button(onClick = { onPlanToTry(displayedPick) }, modifier = Modifier.fillMaxWidth()) {
                        Text(if (featuredOfferToday) "I'll try this deal" else "I'll try this place")
                    }
                }
            }
        }
        if (alternatives.isNotEmpty()) Text("Other options", style = MaterialTheme.typography.titleLarge)
        alternatives.forEach { deal ->
            DealTile(
                deal = deal,
                rating = ratings[deal.id],
                offerToday = today in deal.days,
                onRate = { onRate(deal, it) },
                onOpen = { onOpen(deal) }
            ) {
                Button(onClick = { onChoose(deal) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (today in deal.days) "Choose this deal" else "Choose this place")
                }
            }
        }
    }
}

@Composable
private fun DealTile(
    deal: PlaceDeal,
    featured: Boolean = false,
    rating: Int?,
    offerToday: Boolean,
    onRate: (Int) -> Unit,
    onOpen: () -> Unit,
    content: (@Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit)? = null
) {
    var ratingExpanded by remember(deal.id) { mutableStateOf(false) }
    var confirmOneStar by remember(deal.id) { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (featured) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(deal.name, style = MaterialTheme.typography.titleLarge)
            Text(deal.category, style = MaterialTheme.typography.bodySmall)
            if (offerToday) {
                Text(deal.offer)
                Text(deal.terms, style = MaterialTheme.typography.bodySmall)
                Text(
                    if (deal.verified) "Verified ${deal.checked}"
                    else "Possible deal · Last checked ${deal.checked} · Confirm with this location",
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                Text("No confirmed deal today.", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = { ratingExpanded = !ratingExpanded }) {
                Text(if (rating in 1..5) "Your rating: $rating/5 · Edit" else "Rate this place")
            }
            if (ratingExpanded) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..5).forEach { value ->
                        OutlinedButton(
                            onClick = {
                                if (value == 1) confirmOneStar = true
                                else {
                                    onRate(value)
                                    ratingExpanded = false
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text(value.toString()) }
                    }
                }
                TextButton(
                    onClick = { onRate(0); ratingExpanded = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("I haven't tried this place") }
            }
            TextButton(onClick = onOpen) { Text("View details →") }
            content?.invoke(this)
        }
    }

    if (confirmOneStar) {
        AlertDialog(
            onDismissRequest = { confirmOneStar = false },
            title = { Text("Find a different pick?") },
            text = { Text("Since you rated this place 1 star, we'll remove it from Tonight's Pick and recalculate your options.") },
            confirmButton = {
                TextButton(onClick = {
                    onRate(1)
                    confirmOneStar = false
                    ratingExpanded = false
                }) { Text("Keep 1 and update picks") }
            },
            dismissButton = {
                TextButton(onClick = { confirmOneStar = false }) { Text("Choose another rating") }
            }
        )
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
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val offerToday = today in deal.days
    val upcomingDay = nextOfferDay(deal, today)
    PageColumn {
        TextButton(onClick = onBack) { Text(backLabel) }
        Text(deal.name, style = MaterialTheme.typography.headlineMedium)
        Text(deal.category)
        if (!offerToday && upcomingDay != null) {
            Text("Upcoming offer: ${dayName(upcomingDay)}", style = MaterialTheme.typography.titleMedium)
        }
        Text(deal.offer, style = MaterialTheme.typography.titleLarge)
        Text("Where: ${deal.address}")
        Text("Details: ${deal.terms}")
        Text(
            if (deal.verified) "Verified ${deal.checked}"
            else "Possible offer · Last checked ${deal.checked} · Confirm with this location"
        )
        OutlinedButton(onClick = onSource) { Text("View deal source") }
        if (!removed) {
            OutlinedButton(onClick = onRemove) { Text("Remove this place from recommendations") }
            Text("You can restore it from Removed places.", style = MaterialTheme.typography.bodySmall)
        }
    }
}


