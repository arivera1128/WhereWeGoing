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
import com.example.wherewegoing.data.readMealHistory
import com.example.wherewegoing.data.writeMealHistory
import com.example.wherewegoing.model.MealRecord
import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.ui.DetailPage
import com.example.wherewegoing.ui.ChickLogo
import com.example.wherewegoing.ui.HomePage
import com.example.wherewegoing.ui.PicksPage
import com.example.wherewegoing.ui.ProfilePage
import com.example.wherewegoing.ui.QuizPage
import com.example.wherewegoing.ui.RemovedPage
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
    var detailBackPage by remember { mutableStateOf("Tonight's picks") }
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
    var mealHistory by remember {
        mutableStateOf(readMealHistory(prefs.getString("meal_history", "").orEmpty()))
    }

    fun saveRemoved(newValue: Set<String>) {
        removed = newValue
        prefs.edit().putStringSet("removed", newValue).apply()
    }

    fun postponeCheckIn() {
        pendingCheckInReady = false
        prefs.edit().putBoolean("pending_checkin_ready", false).apply()
    }

    fun cancelPlan() {
        pendingCheckInDealId = ""
        pendingCheckInReady = false
        pendingCheckInShows = 0
        checkInMessage = "Plan canceled."
        prefs.edit()
            .remove("pending_checkin_deal_id")
            .remove("pending_checkin_stage")
            .remove("pending_checkin_ready")
            .remove("pending_checkin_shows")
            .apply()
    }

    fun finishCheckIn(message: String, result: String) {
        val completedDealId = pendingCheckInDealId
        if (completedDealId.isNotBlank()) {
            prefs.edit().putString("deal_checkin_result_$completedDealId", result).apply()
            if (result == "worked" || result == "did_not_work") {
                val updatedHistory = (listOf(MealRecord(completedDealId, result, System.currentTimeMillis())) + mealHistory)
                    .take(50)
                mealHistory = updatedHistory
                prefs.edit().putString("meal_history", writeMealHistory(updatedHistory)).apply()
            }
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
                        onViewPlan = { deal ->
                            selectedDeal = deal
                            detailBackPage = "Home"
                            page = "Deal details"
                        },
                        onChangePlan = { deal ->
                            selectedTonightDealId = deal.id
                            page = "Tonight's picks"
                        },
                        onCancelPlan = { cancelPlan() },
                        mealHistory = mealHistory,
                        ratedPlaceCount = quizRatings.count { it.value in 1..5 },
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
                            selectedTonightDealId = deal.id
                            pendingCheckInDealId = deal.id
                            pendingCheckInReady = false
                            pendingCheckInShows = 0
                            checkInMessage = ""
                            prefs.edit()
                                .putString("selected_tonight_deal_id", deal.id)
                                .putString("pending_checkin_deal_id", deal.id)
                                .putBoolean("pending_checkin_ready", false)
                                .putInt("pending_checkin_shows", 0)
                                .apply()
                            page = "Home"
                        },
                        onUpdateLocation = { page = "Profile" },
                        onReviewRemoved = { page = "Removed places" },
                        onChoose = {
                            selectedTonightDealId = it.id
                            prefs.edit().putString("selected_tonight_deal_id", it.id).apply()
                        },
                        onOpen = {
                            selectedDeal = it
                            detailBackPage = "Tonight's picks"
                            page = "Deal details"
                        }
                    )
                    "Deal details" -> selectedDeal?.let { deal ->
                        DetailPage(
                            deal = deal,
                            removed = deal.id in removed,
                            backLabel = if (detailBackPage == "Home") "← Back to home" else "← Back to picks",
                            onBack = { page = detailBackPage },
                            onRemove = {
                                saveRemoved(removed + deal.id)
                                if (pendingCheckInDealId == deal.id) cancelPlan()
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
