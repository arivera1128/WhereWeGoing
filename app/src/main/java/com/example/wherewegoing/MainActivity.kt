package com.example.wherewegoing

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wherewegoing.ui.theme.WhereWeGoingTheme
import java.util.Calendar
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { WhereWeGoingTheme { DinnerApp() } }
    }
}

@Composable
fun DinnerApp() {
    val context = LocalContext.current
    val prefs = remember {
        val current = context.getSharedPreferences("dinner_prototype", Context.MODE_PRIVATE)
        val previous = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
        if (!current.contains("family_size") && previous.contains("familySize")) {
            current.edit()
                .putString("family_size", previous.getString("familySize", ""))
                .putString("kids", previous.getString("numberOfKids", ""))
                .apply()
        }
        val pendingId = current.getString("pending_checkin_deal_id", "").orEmpty()
        if (pendingId.isNotBlank() && !current.getBoolean("pending_checkin_ready", false)) {
            val previousShows = current.getInt("pending_checkin_shows", 0)
            if (previousShows >= 3) {
                current.edit()
                    .remove("pending_checkin_deal_id")
                    .remove("pending_checkin_stage")
                    .remove("pending_checkin_ready")
                    .remove("pending_checkin_shows")
                    .apply()
            } else {
                current.edit()
                    .putBoolean("pending_checkin_ready", true)
                    .putInt("pending_checkin_shows", previousShows + 1)
                    .apply()
            }
        }
        current
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var page by remember { mutableStateOf("Home") }
    var selectedDeal by remember { mutableStateOf<PlaceDeal?>(null) }
    var selectedTonightDealId by remember {
        mutableStateOf(prefs.getString("selected_tonight_deal_id", null))
    }
    var familySize by remember { mutableStateOf(prefs.getString("family_size", "") ?: "") }
    var kids by remember { mutableStateOf(prefs.getString("kids", "") ?: "") }
    var savedKids by remember { mutableStateOf(kids) }
    var ageGroups by remember { mutableStateOf(prefs.getStringSet("ages", emptySet())?.toSet() ?: emptySet()) }
    var savedAgeGroups by remember { mutableStateOf(ageGroups) }
    var zip by remember { mutableStateOf(prefs.getString("zip", "95758") ?: "95758") }
    var savedZip by remember { mutableStateOf(zip) }
    var radius by remember { mutableStateOf(prefs.getInt("radius", 10)) }
    var profileSaved by remember { mutableStateOf(prefs.contains("family_size")) }
    var editingProfile by remember { mutableStateOf(!profileSaved) }
    var votes by remember {
        mutableStateOf(elkGroveDeals.associate { it.id to (prefs.getString("vote_${it.id}", "neutral") ?: "neutral") })
    }
    var quizRatings by remember {
        mutableStateOf(
            foodQuizPlaces.mapNotNull { place ->
                val key = "quiz_rating_${place.id}"
                if (prefs.contains(key)) place.id to prefs.getInt(key, 0) else null
            }.toMap()
        )
    }
    var quizTarget by remember {
        mutableStateOf(
            if (quizRatings.size < INITIAL_FOOD_QUIZ_SIZE) INITIAL_FOOD_QUIZ_SIZE else quizRatings.size
        )
    }
    var editingQuizPlaceId by remember { mutableStateOf<String?>(null) }
    var previewingNewUser by remember { mutableStateOf(false) }
    var previewRatings by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var previewRemoved by remember { mutableStateOf<Set<String>>(emptySet()) }
    var removeInfoSuppressed by remember { mutableStateOf(prefs.getBoolean("hide_remove_info", false)) }
    var removed by remember { mutableStateOf(prefs.getStringSet("removed", emptySet())?.toSet() ?: emptySet()) }
    var dealAppeal by remember {
        mutableStateOf(elkGroveDeals.associate { it.id to (prefs.getString("deal_appeal_${it.id}", "") ?: "") })
    }
    var pendingCheckInDealId by remember { mutableStateOf(prefs.getString("pending_checkin_deal_id", "") ?: "") }
    var pendingCheckInReady by remember { mutableStateOf(prefs.getBoolean("pending_checkin_ready", false)) }
    var pendingCheckInShows by remember { mutableStateOf(prefs.getInt("pending_checkin_shows", 0)) }
    var checkInMessage by remember { mutableStateOf("") }

    fun saveRemoved(newValue: Set<String>) {
        removed = newValue
        prefs.edit().putStringSet("removed", newValue).apply()
    }

    fun postponeCheckIn() {
        pendingCheckInReady = false
        prefs.edit().putBoolean("pending_checkin_ready", false).apply()
    }

    fun finishCheckIn(message: String, result: String) {
        val completedDealId = pendingCheckInDealId
        if (completedDealId.isNotBlank()) {
            prefs.edit().putString("deal_checkin_result_$completedDealId", result).apply()
        }
        pendingCheckInDealId = ""
        pendingCheckInReady = false
        pendingCheckInShows = 0
        checkInMessage = message
        prefs.edit()
            .remove("pending_checkin_deal_id")
            .remove("pending_checkin_stage")
            .remove("pending_checkin_ready")
            .remove("pending_checkin_shows")
            .apply()
    }

    val supportedZip = savedZip in setOf("95624", "95757", "95758")
    val visibleDeals = if (supportedZip) elkGroveDeals.filter { it.id !in removed } else emptyList()
    val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val activeDeals = visibleDeals.filter {
        today in it.days && it.verified &&
            (!it.forKids || ((savedKids.toIntOrNull() ?: 0) > 0 && savedAgeGroups.any { age -> age == "0–4" || age == "5–12" }))
    }
    val recommendation = activeDeals.maxByOrNull {
        it.savingsRank + if (votes[it.id] == "like") 2 else if (votes[it.id] == "dislike") -2 else 0
    } ?: visibleDeals.firstOrNull { votes[it.id] == "like" } ?: visibleDeals.firstOrNull()
    val selectedTonightDeal = visibleDeals.find { it.id == selectedTonightDealId }
    val displayedPick = selectedTonightDeal ?: recommendation
    val hasTonightDeal = displayedPick in activeDeals

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(20.dp))
                Text("  Dinner, decided.", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(16.dp))
                listOf("Home", "Tonight's picks", "Food profile", "Profile", "Removed places").forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item) },
                        selected = page == item,
                        onClick = {
                            editingQuizPlaceId = null
                            previewingNewUser = false
                            quizTarget = if (quizRatings.size < INITIAL_FOOD_QUIZ_SIZE) {
                                INITIAL_FOOD_QUIZ_SIZE
                            } else {
                                quizRatings.size
                            }
                            page = item
                            scope.launch { drawerState.close() }
                        }
                    )
                }
            }
        }
    ) {
        Scaffold { padding ->
            Column(Modifier.fillMaxSize().padding(padding)) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    IconButton(onClick = { scope.launch { drawerState.open() } }) { Text("☰", fontSize = 26.sp) }
                    ChickLogo()
                    Text("  $page", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
                }
                when (page) {
                    "Home" -> HomePage(
                        profileSaved = profileSaved,
                        zip = savedZip,
                        onPick = { page = "Tonight's picks" },
                        onProfile = { page = "Profile" },
                        hasFoodProfile = quizRatings.size >= INITIAL_FOOD_QUIZ_SIZE,
                        pendingCheckIn = elkGroveDeals.find { it.id == pendingCheckInDealId },
                        checkInReady = pendingCheckInReady,
                        checkInShows = pendingCheckInShows,
                        checkInMessage = checkInMessage,
                        onDealWorked = { finishCheckIn("Thanks! Your confirmation helps us track reliable deals.", "worked") },
                        onDealFailed = { finishCheckIn("Thanks. We'll keep that result separate from your restaurant preferences.", "did_not_work") },
                        onDealNotUsed = { finishCheckIn("Thanks — we'll close that check-in.", "not_used") },
                        onDismissCheckIn = { postponeCheckIn() },
                        onQuiz = {
                            editingQuizPlaceId = null
                            previewingNewUser = false
                            quizTarget = if (quizRatings.size < INITIAL_FOOD_QUIZ_SIZE) INITIAL_FOOD_QUIZ_SIZE else quizRatings.size
                            page = "Food profile"
                        }
                    )
                    "Tonight's picks" -> PicksPage(
                        displayedPick = displayedPick,
                        isUserSelected = selectedTonightDeal != null,
                        hasTonightDeal = hasTonightDeal,
                        alternatives = visibleDeals.filter { it != displayedPick },
                        zip = savedZip,
                        dealAppeal = displayedPick?.let { dealAppeal[it.id] }.orEmpty(),
                        plannedDealId = pendingCheckInDealId,
                        onDealAppeal = { deal, appeal ->
                            dealAppeal = dealAppeal + (deal.id to appeal)
                            prefs.edit().putString("deal_appeal_${deal.id}", appeal).apply()
                        },
                        onPlanToTry = { deal ->
                            pendingCheckInDealId = deal.id
                            pendingCheckInReady = false
                            pendingCheckInShows = 0
                            checkInMessage = ""
                            prefs.edit()
                                .putString("pending_checkin_deal_id", deal.id)
                                .putBoolean("pending_checkin_ready", false)
                                .putInt("pending_checkin_shows", 0)
                                .apply()
                        },
                        onUpdateLocation = { page = "Profile" },
                        onReviewRemoved = { page = "Removed places" },
                        onChoose = {
                            selectedTonightDealId = it.id
                            prefs.edit().putString("selected_tonight_deal_id", it.id).apply()
                        },
                        onOpen = { selectedDeal = it; page = "Deal details" }
                    )
                    "Deal details" -> selectedDeal?.let { deal ->
                        DetailPage(
                            deal = deal,
                            removed = deal.id in removed,
                            onBack = { page = "Tonight's picks" },
                            onRemove = {
                                saveRemoved(removed + deal.id)
                                page = "Tonight's picks"
                            },
                            onSource = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(deal.source))) }
                        )
                    }
                    "Food profile" -> QuizPage(
                        ratings = if (previewingNewUser) previewRatings else quizRatings,
                        targetCount = if (previewingNewUser) INITIAL_FOOD_QUIZ_SIZE else quizTarget,
                        editingPlaceId = editingQuizPlaceId,
                        previewMode = previewingNewUser,
                        removedPlaces = if (previewingNewUser) previewRemoved else removed,
                        removeInfoSuppressed = removeInfoSuppressed,
                        onRate = { place, rating ->
                            if (previewingNewUser) {
                                previewRatings = previewRatings + (place.id to rating)
                                if (editingQuizPlaceId != null) editingQuizPlaceId = null
                            } else {
                                val updatedRatings = quizRatings + (place.id to rating)
                                quizRatings = updatedRatings
                                prefs.edit().putInt("quiz_rating_${place.id}", rating).apply()

                                if (elkGroveDeals.any { it.id == place.id } && rating in 1..5) {
                                    val vote = when (rating) {
                                        1, 2 -> "dislike"
                                        4, 5 -> "like"
                                        else -> "neutral"
                                    }
                                    votes = votes + (place.id to vote)
                                    prefs.edit().putString("vote_${place.id}", vote).apply()
                                }

                                if (editingQuizPlaceId != null) {
                                    editingQuizPlaceId = null
                                    quizTarget = updatedRatings.size
                                } else if (updatedRatings.size >= quizTarget) {
                                    quizTarget = updatedRatings.size
                                }
                            }
                        },
                        onEdit = { editingQuizPlaceId = it.id },
                        onBackToProfile = { editingQuizPlaceId = null },
                        onRateMore = {
                            quizTarget = minOf(foodQuizPlaces.size, quizRatings.size + 2)
                        },
                        onRemove = { place ->
                            if (previewingNewUser) {
                                previewRemoved = previewRemoved + place.id
                                previewRatings = previewRatings + (place.id to 0)
                                editingQuizPlaceId = null
                            } else {
                                saveRemoved(removed + place.id)
                                if (place.id !in quizRatings) {
                                    val updatedRatings = quizRatings + (place.id to 0)
                                    quizRatings = updatedRatings
                                    prefs.edit().putInt("quiz_rating_${place.id}", 0).apply()
                                    if (updatedRatings.size >= quizTarget) quizTarget = updatedRatings.size
                                }
                                editingQuizPlaceId = null
                            }
                        },
                        onSuppressRemoveInfo = {
                            removeInfoSuppressed = true
                            prefs.edit().putBoolean("hide_remove_info", true).apply()
                        },
                        onPreviewNewUser = {
                            previewRatings = emptyMap()
                            previewRemoved = emptySet()
                            editingQuizPlaceId = null
                            previewingNewUser = true
                        },
                        onExitPreview = {
                            previewingNewUser = false
                            editingQuizPlaceId = null
                            quizTarget = if (quizRatings.size < INITIAL_FOOD_QUIZ_SIZE) INITIAL_FOOD_QUIZ_SIZE else quizRatings.size
                        },
                        onSeePicks = {
                            previewingNewUser = false
                            editingQuizPlaceId = null
                            page = "Tonight's picks"
                        }
                    )
                    "Profile" -> ProfilePage(
                        familySize = familySize,
                        onFamilySize = { familySize = it },
                        kids = kids,
                        onKids = { kids = it },
                        ageGroups = ageGroups,
                        onAgeGroups = { ageGroups = it },
                        zip = zip,
                        onZip = { zip = it },
                        radius = radius,
                        onRadius = { radius = it },
                        editing = editingProfile,
                        onEdit = { editingProfile = true },
                        onSave = {
                            prefs.edit()
                                .putString("family_size", familySize)
                                .putString("kids", kids)
                                .putStringSet("ages", ageGroups)
                                .putString("zip", zip)
                                .putInt("radius", radius)
                                .apply()
                            profileSaved = true
                            savedKids = kids
                            savedAgeGroups = ageGroups
                            savedZip = zip
                            editingProfile = false
                        }
                    )
                    "Removed places" -> RemovedPage(removed) { id -> saveRemoved(removed - id) }
                }
            }
        }
    }
}

@Composable
private fun ChickLogo() {
    Image(
        painter = painterResource(R.drawable.walking_chick_logo),
        contentDescription = "Walking chick logo",
        modifier = Modifier.size(42.dp)
    )
}

@Composable
private fun PageColumn(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        content = content
    )
}

@Composable
private fun HomePage(
    profileSaved: Boolean,
    zip: String,
    onPick: () -> Unit,
    onProfile: () -> Unit,
    hasFoodProfile: Boolean,
    pendingCheckIn: PlaceDeal?,
    checkInReady: Boolean,
    checkInShows: Int,
    checkInMessage: String,
    onDealWorked: () -> Unit,
    onDealFailed: () -> Unit,
    onDealNotUsed: () -> Unit,
    onDismissCheckIn: () -> Unit,
    onQuiz: () -> Unit
) {
    PageColumn {
        ChickLogo()
        Text("What's for dinner?", style = MaterialTheme.typography.headlineMedium)
        Text("A smart little pick for you in Elk Grove.")
        if (pendingCheckIn != null && checkInReady) {
            CheckInCard(
                deal = pendingCheckIn,
                showNumber = checkInShows,
                onDealWorked = onDealWorked,
                onDealFailed = onDealFailed,
                onDealNotUsed = onDealNotUsed,
                onDismiss = onDismissCheckIn
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
        Button(onClick = onPick, modifier = Modifier.fillMaxWidth().height(64.dp)) {
            Text("Where should we eat tonight?")
        }
        Text("Starting area: Elk Grove • ZIP $zip")
        if (!profileSaved) {
            OutlinedButton(onClick = onProfile, modifier = Modifier.fillMaxWidth()) { Text("Set up your profile") }
        }
        OutlinedButton(onClick = onQuiz, modifier = Modifier.fillMaxWidth()) {
            Text(if (hasFoodProfile) "Review food preferences" else "Build your food profile")
        }
        Text("Deals are curated for this prototype. Check the offer before visiting.", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CheckInCard(
    deal: PlaceDeal,
    showNumber: Int,
    onDealWorked: () -> Unit,
    onDealFailed: () -> Unit,
    onDealNotUsed: () -> Unit,
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
            Text("Did you try this deal?")
            Button(onClick = onDealWorked, modifier = Modifier.fillMaxWidth()) { Text("Yes — the deal worked") }
            OutlinedButton(onClick = onDealFailed, modifier = Modifier.fillMaxWidth()) { Text("Yes — but the deal didn't work") }
            TextButton(onClick = onDealNotUsed, modifier = Modifier.fillMaxWidth()) { Text("No") }
            Text("Check-in ${showNumber.coerceAtLeast(1)} of 3", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun PicksPage(
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
private fun DetailPage(deal: PlaceDeal, removed: Boolean, onBack: () -> Unit, onRemove: () -> Unit, onSource: () -> Unit) {
    PageColumn {
        TextButton(onClick = onBack) { Text("← Back to picks") }
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

@Composable
private fun QuizPage(
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
    val currentPlace = editingPlaceId?.let { id -> foodQuizPlaces.find { it.id == id } }
        ?: if (ratings.size < targetCount) nextQuizPlace(ratings) else null
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
            } else if (ratings.size < foodQuizPlaces.size) {
                Button(onClick = onRateMore, modifier = Modifier.fillMaxWidth()) {
                    Text("Rate more places — ${foodQuizPlaces.size - ratings.size} available")
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
            val matchingPlaces = foodQuizPlaces.filter {
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

@Composable
private fun ProfilePage(
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
private fun RemovedPage(removed: Set<String>, onRestore: (String) -> Unit) {
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
