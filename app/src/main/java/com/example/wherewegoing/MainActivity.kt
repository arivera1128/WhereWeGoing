package com.example.wherewegoing

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
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
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wherewegoing.data.DinnerJourneySnapshot
import com.example.wherewegoing.data.RoomDinnerJourneyRepository
import com.example.wherewegoing.data.RoomHouseholdProfileRepository
import com.example.wherewegoing.data.HouseholdProfileSnapshot
import com.example.wherewegoing.data.CatalogSnapshot
import com.example.wherewegoing.data.RoomCatalogRepository
import com.example.wherewegoing.data.SharedCatalogRepository
import com.example.wherewegoing.data.SupabaseCatalogSource
import com.example.wherewegoing.data.PreferenceSnapshot
import com.example.wherewegoing.data.RoomPreferenceRepository
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import com.example.wherewegoing.domain.isDealEligibleForHousehold
import com.example.wherewegoing.domain.RecommendationEngine
import com.example.wherewegoing.domain.daysUntilNextOffer
import com.example.wherewegoing.domain.dayName
import com.example.wherewegoing.model.HouseholdProfile
import com.example.wherewegoing.model.MealRecord
import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.ui.DetailPage
import com.example.wherewegoing.ui.ChickLogo
import com.example.wherewegoing.ui.HomePage
import com.example.wherewegoing.ui.OnboardingScreen
import com.example.wherewegoing.ui.PicksPage
import com.example.wherewegoing.ui.ProfilePage
import com.example.wherewegoing.ui.PrototypeToolsPage
import com.example.wherewegoing.ui.QuizPage
import com.example.wherewegoing.ui.RemovedPage
import com.example.wherewegoing.ui.theme.WhereWeGoingTheme
import java.util.Calendar
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException

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
    val isDebugBuild = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
    val prefs = remember {
        val current = context.getSharedPreferences("dinner_prototype", Context.MODE_PRIVATE)
        val previous = context.getSharedPreferences("profile", Context.MODE_PRIVATE)
        if (!current.contains("family_size") && previous.contains("familySize")) {
            current.edit()
                .putString("family_size", previous.getString("familySize", ""))
                .putString("kids", previous.getString("numberOfKids", ""))
                .apply()
        }
        current
    }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val householdDatabase = remember(context) { WhereWeGoingDatabase.getInstance(context) }
    val householdRepository = remember(householdDatabase, prefs) {
        RoomHouseholdProfileRepository(householdDatabase, prefs)
    }
    val preferenceRepository = remember(householdDatabase, prefs) {
        RoomPreferenceRepository(householdDatabase, prefs)
    }
    val dinnerJourneyRepository = remember(householdDatabase) {
        RoomDinnerJourneyRepository(householdDatabase)
    }
    var householdSnapshot by remember { mutableStateOf<HouseholdProfileSnapshot?>(null) }
    LaunchedEffect(householdRepository) {
        householdSnapshot = householdRepository.load()
    }
    val loadedHousehold = householdSnapshot
    if (loadedHousehold == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ChickLogo()
            Spacer(Modifier.height(16.dp))
            Text("Loading your profile…", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    val catalogRepository = remember(householdDatabase) {
        RoomCatalogRepository(householdDatabase, foodQuizPlaces, elkGroveDeals)
    }
    var page by remember { mutableStateOf("Home") }
    val sharedEnabled = BuildConfig.DEBUG && BuildConfig.PROOF_SUPABASE_URL.isNotBlank()
    val sharedRepository = remember(householdDatabase) {
        SharedCatalogRepository(householdDatabase, SupabaseCatalogSource(BuildConfig.PROOF_SUPABASE_URL, BuildConfig.PROOF_SUPABASE_KEY))
    }
    val testerRepository = remember(householdDatabase) { com.example.wherewegoing.data.TesterAccountRepository(context, householdDatabase, BuildConfig.PROOF_SUPABASE_URL, BuildConfig.PROOF_SUPABASE_KEY) }
    var refreshRequest by remember { mutableStateOf(0) }
    var catalogBusy by remember { mutableStateOf(sharedEnabled) }
    var catalogError by remember { mutableStateOf(false) }
    var catalogDiagnostic by remember { mutableStateOf("") }
    val lifecycleOwner=LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, sharedEnabled) {
        var stopped=false
        val observer=LifecycleEventObserver { _, event ->
            if(event==Lifecycle.Event.ON_STOP) stopped=true
            if(event==Lifecycle.Event.ON_START && stopped) {
                stopped=false
                // Freeze the snapshot while a dinner choice/detail flow is in progress.
                if(sharedEnabled && page !in setOf("Tonight's picks","Deal details")) refreshRequest += 1
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    var catalogSnapshot by remember { mutableStateOf<CatalogSnapshot?>(null) }
    LaunchedEffect(catalogRepository, refreshRequest) {
        catalogBusy=sharedEnabled
        try {
            // Seed/retain existing identities before the first shared import.
            catalogRepository.load()
            if(sharedEnabled) sharedRepository.refresh()
            catalogError=false
            catalogDiagnostic=if(sharedEnabled) "Shared catalog refreshed." else "Local prototype catalog."
        } catch(error: CancellationException) { throw error
        } catch(error: Exception) {
            catalogError=true
            catalogDiagnostic=error.message ?: "Catalog refresh failed."
        } finally {
            catalogSnapshot=catalogRepository.load()
            catalogBusy=false
        }
    }
    val catalog = catalogSnapshot
    if (catalog == null) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("Loading places…")
        }
        return
    }
    val places = catalog.places
    val deals = catalog.deals
    val canRecommend=!catalogBusy && !catalogError
    var preferenceSnapshot by remember { mutableStateOf<PreferenceSnapshot?>(null) }
    LaunchedEffect(loadedHousehold.userId) {
        preferenceSnapshot = preferenceRepository.load(loadedHousehold.userId, places)
    }
    val loadedPreferences = preferenceSnapshot
    if (loadedPreferences == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ChickLogo()
            Spacer(Modifier.height(16.dp))
            Text("Loading your food profile…", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    var dinnerSnapshot by remember { mutableStateOf<DinnerJourneySnapshot?>(null) }
    LaunchedEffect(loadedHousehold.userId) {
        dinnerSnapshot = dinnerJourneyRepository.load(loadedHousehold.userId)
    }
    val loadedDinner = dinnerSnapshot
    if (loadedDinner == null) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            ChickLogo()
            Spacer(Modifier.height(16.dp))
            Text("Loading your dinner plans…", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    val recommendationEngine = remember { RecommendationEngine() }
    var selectedDeal by remember { mutableStateOf<PlaceDeal?>(null) }
    var detailBackPage by remember { mutableStateOf("Tonight's picks") }
    var selectedTonightDealId by remember { mutableStateOf<String?>(null) }
    var savedHousehold by remember { mutableStateOf(loadedHousehold.profile) }
    var householdDraft by remember { mutableStateOf(savedHousehold) }
    var profileSaved by remember { mutableStateOf(loadedHousehold.hasProfile) }
    var onboardingComplete by remember {
        mutableStateOf(profileSaved)
    }
    var editingProfile by remember { mutableStateOf(!profileSaved) }
    var votes by remember {
        mutableStateOf(deals.associate { it.id to (prefs.getString("vote_${it.id}", "neutral") ?: "neutral") })
    }
    var quizRatings by remember { mutableStateOf(loadedPreferences.ratings) }
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
    var removed by remember { mutableStateOf(loadedPreferences.removedPlaceKeys) }
    var picksMessage by remember { mutableStateOf("") }
    var recommendationRefreshVersion by remember { mutableStateOf(0) }
    var pendingCheckInDealId by remember { mutableStateOf(loadedDinner.pendingPlaceKey.orEmpty()) }
    var savedPendingPlace by remember { mutableStateOf(loadedDinner.pendingPlace) }
    var pendingCheckInHadOffer by remember { mutableStateOf(loadedDinner.pendingHasOffer) }
    var pendingCheckInReady by remember { mutableStateOf(loadedDinner.checkInReady) }
    var pendingCheckInShows by remember { mutableStateOf(0) }
    var checkInMessage by remember { mutableStateOf("") }
    var checkInResult by remember { mutableStateOf("") }
    var mealHistory by remember { mutableStateOf(loadedDinner.mealHistory) }
    var simulatedDay by remember {
        mutableStateOf(prefs.getInt("prototype_simulated_day", 0).takeIf { it in 1..7 })
    }

    fun saveRemoved(newValue: Set<String>) {
        val added = newValue - removed
        val restored = removed - newValue
        removed = newValue
        scope.launch {
            added.forEach { preferenceRepository.setExcluded(loadedHousehold.userId, it, true) }
            restored.forEach { preferenceRepository.setExcluded(loadedHousehold.userId, it, false) }
        }
    }

    fun postponeCheckIn() {
        pendingCheckInReady = false
    }

    fun cancelPlan() {
        savedPendingPlace=null
        pendingCheckInDealId = ""
        pendingCheckInReady = false
        pendingCheckInHadOffer = true
        pendingCheckInShows = 0
        checkInMessage = "Plan canceled."
        checkInResult = "canceled"
        scope.launch { dinnerJourneyRepository.cancelCurrentPlan(loadedHousehold.userId) }
    }

    fun finishCheckIn(message: String, result: String) {
        savedPendingPlace=null
        val completedDealId = pendingCheckInDealId
        if (completedDealId.isNotBlank() && (result == "worked" || result == "did_not_work" || result == "visited")) {
            mealHistory = (listOf(MealRecord(completedDealId, result, System.currentTimeMillis())) + mealHistory).take(50)
        }
        pendingCheckInDealId = ""
        pendingCheckInReady = false
        pendingCheckInHadOffer = true
        pendingCheckInShows = 0
        checkInMessage = message
        checkInResult = result
        scope.launch { dinnerJourneyRepository.recordOutcome(loadedHousehold.userId, result) }
    }

    val supportedZip = savedHousehold.zip in setOf("95624", "95757", "95758")
    val visibleDeals = if (supportedZip && canRecommend) deals.filter { it.id !in removed } else emptyList()
    val today = simulatedDay ?: Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    val recommendationResult = remember(
        visibleDeals,
        savedHousehold,
        today,
        page == "Tonight's picks",
        recommendationRefreshVersion
    ) {
        recommendationEngine.recommend(
            deals = visibleDeals,
            quizPlaces = places,
            ratings = quizRatings,
            household = savedHousehold,
            today = today
        )
    }
    val recommendation = recommendationResult.featured
    val selectedTonightDeal = visibleDeals.find { it.id == selectedTonightDealId }
    val displayedPick = selectedTonightDeal ?: recommendation
    val hasTonightDeal = displayedPick?.let {
        today in it.days && it.verified && isDealEligibleForHousehold(it, savedHousehold)
    } == true
    val displayedAlternatives = if (selectedTonightDeal != null) {
        (listOfNotNull(recommendation) + recommendationResult.alternatives)
            .distinctBy { it.id }
            .filter { it.id != selectedTonightDeal.id }
            .take(3)
    } else recommendationResult.alternatives
    val upcomingDeal = visibleDeals
        .filter { isDealEligibleForHousehold(it, savedHousehold) }
        .mapNotNull { deal -> daysUntilNextOffer(deal, today)?.let { days -> deal to days } }
        .minWithOrNull(
            compareBy<Pair<PlaceDeal, Int>> { it.second }
                .thenByDescending { it.first.verified }
                .thenByDescending { it.first.savingsRank }
        )

    if (!onboardingComplete) {
        OnboardingScreen(places = places) { onboardingZip, adultCount, childAges, onboardingRatings ->
            val completedHousehold = HouseholdProfile(
                adults = adultCount,
                childAges = childAges,
                zip = onboardingZip,
                radiusMiles = 10
            )
            val editor = prefs.edit()

            onboardingRatings.forEach { (placeId, rating) ->
                if (rating in 1..5 && deals.any { it.id == placeId }) {
                    val vote = when (rating) {
                        1, 2 -> "dislike"
                        4, 5 -> "like"
                        else -> "neutral"
                    }
                    editor.putString("vote_$placeId", vote)
                    votes = votes + (placeId to vote)
                }
            }
            editor.apply()
            scope.launch {
                householdRepository.save(completedHousehold)
                onboardingRatings.forEach { (placeId, rating) ->
                    preferenceRepository.saveRating(loadedHousehold.userId, placeId, rating)
                }
                savedHousehold = completedHousehold
                householdDraft = completedHousehold
                profileSaved = true
                editingProfile = false
                quizRatings = onboardingRatings
                quizTarget = onboardingRatings.size
                onboardingComplete = true
                page = "Tonight's picks"
            }
        }
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Column(Modifier.fillMaxSize()) {
                    Spacer(Modifier.height(20.dp))
                    Text("  Dinner, decided.", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(16.dp))
                    (listOf("Home", "Tonight's picks", "Food profile", "Profile", "Removed places") + if(sharedEnabled) listOf("Share a deal", "My submissions") else emptyList()).forEach { item ->
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
                    Spacer(Modifier.weight(1f))
                    if (isDebugBuild) {
                        HorizontalDivider()
                        NavigationDrawerItem(
                            label = { Text("⚙ Prototype tools") },
                            selected = page == "Prototype tools",
                            colors = NavigationDrawerItemDefaults.colors(
                                unselectedTextColor = MaterialTheme.colorScheme.tertiary,
                                selectedTextColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            onClick = {
                                page = "Prototype tools"
                                scope.launch { drawerState.close() }
                            }
                        )
                        Spacer(Modifier.height(16.dp))
                    }
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
                if (isDebugBuild && simulatedDay != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Text(
                            "TESTING ${dayName(simulatedDay!!).uppercase()} · Change in Prototype tools",
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                if (sharedEnabled) Text("Development environment", style = MaterialTheme.typography.labelSmall)
                if(sharedEnabled && (catalogBusy || catalogError)) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(if(catalogBusy) "Refreshing places and deals…" else "We couldn’t refresh places and deals. Try again.")
                        if(catalogError && !catalogBusy) OutlinedButton(onClick={ refreshRequest += 1 }) { Text("Retry") }
                    }
                }
                when (page) {
                    "Home" -> HomePage(
                        deals = catalog.archivedDeals,
                        recommendationsReady = canRecommend,
                        planWarning = if(catalogError) "Couldn’t check the latest offer details."
                            else if(!catalogBusy && pendingCheckInHadOffer && savedPendingPlace?.dealVersionId != null &&
                                deals.none { it.id==pendingCheckInDealId && it.dealVersionId==savedPendingPlace?.dealVersionId })
                                "This offer is no longer available." else "",
                        profileSaved = profileSaved,
                        zip = savedHousehold.zip,
                        onPick = { page = "Tonight's picks" },
                        onProfile = { page = "Profile" },
                        hasFoodProfile = quizRatings.isNotEmpty() && quizRatings.size >= minOf(INITIAL_FOOD_QUIZ_SIZE, places.size),
                        pendingCheckIn = savedPendingPlace?.takeIf { it.id==pendingCheckInDealId }
                            ?: catalog.archivedDeals.find { it.id == pendingCheckInDealId },
                        checkInReady = pendingCheckInReady,
                        pendingCheckInHadOffer = pendingCheckInHadOffer,
                        checkInShows = pendingCheckInShows,
                        checkInMessage = checkInMessage,
                        checkInResult = checkInResult,
                        onClearCheckInMessage = {
                            checkInMessage = ""
                            checkInResult = ""
                        },
                        onDealWorked = { finishCheckIn("Thanks—this helps keep local deals reliable.", "worked") },
                        onDealFailed = { finishCheckIn("We'll treat this deal as needing confirmation.", "did_not_work") },
                        onDealNotUsed = { finishCheckIn("We won't count this as a meal.", "not_used") },
                        onRestaurantVisited = { finishCheckIn("That meal was added to your history.", "visited") },
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
                        upcomingDeal = upcomingDeal?.first,
                        upcomingDaysAway = upcomingDeal?.second,
                        currentDay = today,
                        onViewUpcoming = { deal ->
                            selectedDeal = deal
                            detailBackPage = "Home"
                            page = "Deal details"
                        },
                        onQuiz = {
                            editingQuizPlaceId = null
                            previewingNewUser = false
                            quizTarget = if (quizRatings.size < INITIAL_FOOD_QUIZ_SIZE) INITIAL_FOOD_QUIZ_SIZE else quizRatings.size
                            page = "Food profile"
                        }
                    )
                    "Tonight's picks" -> if(!canRecommend) {
                        Column(Modifier.padding(24.dp)) { Text("New recommendations will be available after places and deals refresh.") }
                    } else PicksPage(
                        displayedPick = displayedPick,
                        isUserSelected = selectedTonightDeal != null,
                        hasTonightDeal = hasTonightDeal,
                        alternatives = displayedAlternatives,
                        today = today,
                        zip = savedHousehold.zip,
                        ratings = quizRatings,
                        picksMessage = picksMessage,
                        plannedDealId = pendingCheckInDealId,
                        onRate = { deal, rating ->
                            quizRatings = quizRatings + (deal.id to rating)
                            scope.launch {
                                preferenceRepository.saveRating(loadedHousehold.userId, deal.id, rating)
                            }
                            val vote = when (rating) {
                                1, 2 -> "dislike"
                                4, 5 -> "like"
                                else -> "neutral"
                            }
                            votes = votes + (deal.id to vote)
                            prefs.edit().putString("vote_${deal.id}", vote).apply()
                            if (rating == 1) {
                                if (selectedTonightDealId == deal.id) {
                                    selectedTonightDealId = null
                                }
                                recommendationRefreshVersion += 1
                                picksMessage = "Your picks were updated."
                            } else {
                                picksMessage = ""
                            }
                        },
                        onPlanToTry = { deal ->
                            savedPendingPlace=deal
                            val planHasOffer = today in deal.days && isDealEligibleForHousehold(deal, savedHousehold)
                            val wasFeatured = recommendation?.id == deal.id && selectedTonightDealId == null
                            selectedTonightDealId = deal.id
                            pendingCheckInDealId = deal.id
                            pendingCheckInHadOffer = planHasOffer
                            pendingCheckInReady = false
                            pendingCheckInShows = 0
                            checkInMessage = ""
                            checkInResult = ""
                            scope.launch {
                                dinnerJourneyRepository.selectPlan(
                                    userId = loadedHousehold.userId,
                                    householdId = loadedHousehold.householdId,
                                    placeKey = deal.id,
                                    hasOffer = planHasOffer,
                                    featured = wasFeatured,
                                    selection = deal
                                )
                            }
                            page = "Home"
                        },
                        onUpdateLocation = { page = "Profile" },
                        onReviewRemoved = { page = "Removed places" },
                        onChoose = {
                            selectedTonightDealId = it.id
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
                        places = places,
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
                                scope.launch {
                                    preferenceRepository.saveRating(loadedHousehold.userId, place.id, rating)
                                }

                                if (deals.any { it.id == place.id } && rating in 1..5) {
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
                            quizTarget = minOf(places.size, quizRatings.size + 2)
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
                                    scope.launch {
                                        preferenceRepository.saveRating(loadedHousehold.userId, place.id, 0)
                                    }
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
                        profile = householdDraft,
                        onProfileChange = { householdDraft = it },
                        editing = editingProfile,
                        onEdit = {
                            householdDraft = savedHousehold
                            editingProfile = true
                        },
                        onSave = {
                            scope.launch {
                                householdRepository.save(householdDraft)
                                savedHousehold = householdDraft
                                profileSaved = true
                                editingProfile = false
                            }
                        }
                    )
                    "Share a deal" -> if(sharedEnabled) com.example.wherewegoing.ui.DealSubmissionPage(loadedHousehold.userId,testerRepository) { page = "My submissions" }
                    "My submissions" -> if(sharedEnabled) com.example.wherewegoing.ui.MySubmissionsPage(loadedHousehold.userId,testerRepository) { page = "Share a deal" }
                    "Removed places" -> RemovedPage(removed, catalog.archivedDeals) { id -> saveRemoved(removed - id) }
                    "Prototype tools" -> if (isDebugBuild) PrototypeToolsPage(
                        catalogStatus = catalogDiagnostic,
                        simulatedDay = simulatedDay,
                        household = savedHousehold,
                        ratedPlaceCount = quizRatings.count { it.value in 1..5 },
                        onSelectDay = { selectedDay ->
                            simulatedDay = selectedDay
                            if (selectedDay == null) {
                                prefs.edit().remove("prototype_simulated_day").apply()
                            } else {
                                prefs.edit().putInt("prototype_simulated_day", selectedDay).apply()
                            }
                            recommendationRefreshVersion += 1
                        },
                        onPreviewNewUser = {
                            previewRatings = emptyMap()
                            previewRemoved = emptySet()
                            editingQuizPlaceId = null
                            previewingNewUser = true
                            page = "Food profile"
                        },
                        onReset = {
                            scope.launch {
                                testerRepository.reset(loadedHousehold.userId)
                                householdRepository.reset()
                                prefs.edit().clear().commit()
                                val freshSnapshot = householdRepository.load()
                                if(sharedEnabled) {
                                    catalogBusy=true
                                    try { catalogRepository.load(); sharedRepository.refresh(); catalogError=false
                                    } catch(error: CancellationException) { throw error
                                    } catch(error: Exception) { catalogError=true
                                    } finally { catalogBusy=false }
                                }
                                val freshCatalog = catalogRepository.load()
                                catalogSnapshot = freshCatalog
                                val freshPreferences = preferenceRepository.load(
                                    freshSnapshot.userId,
                                    freshCatalog.places
                                )
                                val freshDinner = dinnerJourneyRepository.load(freshSnapshot.userId)
                                val freshHousehold = freshSnapshot.profile
                                householdSnapshot = freshSnapshot
                                preferenceSnapshot = freshPreferences
                                dinnerSnapshot = freshDinner
                                savedHousehold = freshHousehold
                                householdDraft = freshHousehold
                                profileSaved = false
                                onboardingComplete = false
                                editingProfile = true
                                votes = deals.associate { it.id to "neutral" }
                                quizRatings = emptyMap()
                                quizTarget = INITIAL_FOOD_QUIZ_SIZE
                                editingQuizPlaceId = null
                                previewingNewUser = false
                                previewRatings = emptyMap()
                                previewRemoved = emptySet()
                                removeInfoSuppressed = false
                                removed = emptySet()
                                picksMessage = ""
                                pendingCheckInDealId = ""
                                savedPendingPlace=null
                                pendingCheckInHadOffer = true
                                pendingCheckInReady = false
                                pendingCheckInShows = 0
                                checkInMessage = ""
                                checkInResult = ""
                                mealHistory = emptyList()
                                selectedTonightDealId = null
                                selectedDeal = null
                                simulatedDay = null
                                recommendationRefreshVersion += 1
                                page = "Home"
                            }
                        }
                    )
                }
            }
        }
    }
}
