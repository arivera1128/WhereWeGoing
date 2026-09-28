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
fun HomePage(
    profileSaved: Boolean,
    zip: String,
    onPick: () -> Unit,
    onProfile: () -> Unit,
    hasFoodProfile: Boolean,
    pendingCheckIn: PlaceDeal?,
    checkInReady: Boolean,
    pendingCheckInHadOffer: Boolean,
    checkInShows: Int,
    checkInMessage: String,
    onDealWorked: () -> Unit,
    onDealFailed: () -> Unit,
    onDealNotUsed: () -> Unit,
    onRestaurantVisited: () -> Unit,
    onDismissCheckIn: () -> Unit,
    onViewPlan: (PlaceDeal) -> Unit,
    onChangePlan: (PlaceDeal) -> Unit,
    onCancelPlan: () -> Unit,
    mealHistory: List<MealRecord>,
    ratedPlaceCount: Int,
    upcomingDeal: PlaceDeal?,
    upcomingDaysAway: Int?,
    onViewUpcoming: (PlaceDeal) -> Unit,
    onQuiz: () -> Unit
) {
    PageColumn {
        ChickLogo()
        Text("What's for dinner?", style = MaterialTheme.typography.headlineMedium)
        Text("A smart little pick for you in Elk Grove.")
        Button(onClick = onPick, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("Where should we eat tonight?")
        }
        Text("Starting area: Elk Grove • ZIP $zip", style = MaterialTheme.typography.bodySmall)

        if (upcomingDeal != null && upcomingDaysAway != null) {
            UpcomingOfferCard(
                deal = upcomingDeal,
                daysAway = upcomingDaysAway,
                onView = { onViewUpcoming(upcomingDeal) }
            )
        }

        Text("Your dinner dashboard", style = MaterialTheme.typography.titleLarge)
        DinnerSummary(mealHistory)

        if (pendingCheckIn != null && checkInReady) {
            Text("Pending check-in", style = MaterialTheme.typography.titleLarge)
            CheckInCard(
                deal = pendingCheckIn,
                showNumber = checkInShows,
                hadOffer = pendingCheckInHadOffer,
                onDealWorked = onDealWorked,
                onDealFailed = onDealFailed,
                onDealNotUsed = onDealNotUsed,
                onRestaurantVisited = onRestaurantVisited,
                onDismiss = onDismissCheckIn
            )
        } else if (pendingCheckIn != null) {
            Text("Tonight's plan", style = MaterialTheme.typography.titleLarge)
            TonightPlanCard(
                deal = pendingCheckIn,
                hadOffer = pendingCheckInHadOffer,
                onViewDetails = { onViewPlan(pendingCheckIn) },
                onChangePlan = { onChangePlan(pendingCheckIn) },
                onCancelPlan = onCancelPlan
            )
        }
        if (checkInMessage.isNotBlank()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Text(checkInMessage, modifier = Modifier.padding(16.dp))
            }
        }

        Text("Recent meals", style = MaterialTheme.typography.titleLarge)
        RecentMeals(mealHistory)

        if (!profileSaved) {
            OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("Set up your profile") }
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Your food profile", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (ratedPlaceCount == 1) "1 place rated" else "$ratedPlaceCount places rated",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedButton(onClick = onQuiz, modifier = Modifier.fillMaxWidth()) {
                    Text(if (hasFoodProfile) "Improve recommendations" else "Build your food profile")
                }
            }
        }
        Text("Deals are curated for this prototype. Check the offer before visiting.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun UpcomingOfferCard(deal: PlaceDeal, daysAway: Int, onView: () -> Unit) {
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val offerDay = nextOfferDay(deal, today)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                if (daysAway == 1) "Coming tomorrow" else "Coming ${offerDay?.let(::dayName) ?: "soon"}",
                style = MaterialTheme.typography.titleMedium
            )
            Text(deal.name, style = MaterialTheme.typography.titleLarge)
            Text(deal.offer)
            Text(
                if (deal.verified) "Verified ${deal.checked}"
                else "Possible offer · Last checked ${deal.checked} · Confirm with this location",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = onView, modifier = Modifier.fillMaxWidth()) {
                Text("View upcoming offer")
            }
        }
    }
}

@Composable
private fun TonightPlanCard(
    deal: PlaceDeal,
    hadOffer: Boolean,
    onViewDetails: () -> Unit,
    onChangePlan: () -> Unit,
    onCancelPlan: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(deal.name, style = MaterialTheme.typography.titleLarge)
            Text(if (hadOffer) deal.offer else deal.category)
            Text(
                if (hadOffer) "We'll ask how the deal went the next time you open the app."
                else "We'll ask whether you ate here the next time you open the app.",
                style = MaterialTheme.typography.bodySmall
            )
            Button(onClick = onViewDetails, modifier = Modifier.fillMaxWidth()) {
                Text(if (hadOffer) "View deal details" else "View restaurant details")
            }
            OutlinedButton(onClick = onChangePlan, modifier = Modifier.fillMaxWidth()) { Text("Change my plan") }
            TextButton(onClick = onCancelPlan, modifier = Modifier.fillMaxWidth()) { Text("Cancel plan") }
        }
    }
}

@Composable
private fun DinnerSummary(mealHistory: List<MealRecord>) {
    val workedCount = mealHistory.count { it.result == "worked" }
    val placesVisited = mealHistory.map { it.dealId }.distinct().size
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        SummaryMetric(mealHistory.size.toString(), "Meals recorded", Modifier.weight(1f))
        SummaryMetric(workedCount.toString(), "Deals worked", Modifier.weight(1f))
        SummaryMetric(placesVisited.toString(), "Places visited", Modifier.weight(1f))
    }
}

@Composable
private fun SummaryMetric(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun RecentMeals(mealHistory: List<MealRecord>) {
    if (mealHistory.isEmpty()) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Text(
                "No meals recorded yet. Completed deal check-ins will appear here.",
                modifier = Modifier.padding(16.dp)
            )
        }
        return
    }

    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    mealHistory.take(5).forEach { meal ->
        val deal = elkGroveDeals.find { it.id == meal.dealId }
        if (deal != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(deal.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            when (meal.result) {
                                "worked" -> "Deal worked"
                                "visited" -> "Restaurant visit"
                                else -> "Deal didn't work"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Text(dateFormat.format(Date(meal.recordedAt)), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
private fun CheckInCard(
    deal: PlaceDeal,
    showNumber: Int,
    hadOffer: Boolean,
    onDealWorked: () -> Unit,
    onDealFailed: () -> Unit,
    onDealNotUsed: () -> Unit,
    onRestaurantVisited: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Quick check-in", style = MaterialTheme.typography.titleLarge)
                TextButton(onClick = onDismiss) { Text("Dismiss") }
            }
            Text(deal.name, style = MaterialTheme.typography.titleMedium)
            Text(if (hadOffer) "Did you try this deal?" else "Did you eat here?")
            if (hadOffer) {
                Button(onClick = onDealWorked, modifier = Modifier.fillMaxWidth()) { Text("Yes — the deal worked") }
                OutlinedButton(onClick = onDealFailed, modifier = Modifier.fillMaxWidth()) { Text("Yes — but the deal didn't work") }
            } else {
                Button(onClick = onRestaurantVisited, modifier = Modifier.fillMaxWidth()) { Text("Yes") }
            }
            TextButton(onClick = onDealNotUsed, modifier = Modifier.fillMaxWidth()) { Text("No") }
            Text("Check-in ${showNumber.coerceAtLeast(1)} of 3", style = MaterialTheme.typography.bodySmall)
        }
    }
}


