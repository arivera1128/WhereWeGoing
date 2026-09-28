package com.example.wherewegoing

import com.example.wherewegoing.domain.RecommendationEngine
import com.example.wherewegoing.model.HouseholdProfile
import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.model.QuizPlace
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecommendationEngineTest {
    private val engine = RecommendationEngine()
    private val today = Calendar.MONDAY

    @Test
    fun oneStarPlaceIsNeverFeaturedButRemainsAlternative() {
        val disliked = deal("disliked", 2)
        val neutral = deal("neutral", 1)
        val result = engine.recommend(
            listOf(disliked, neutral), places("disliked", "neutral"),
            mapOf("disliked" to 1), HouseholdProfile(), today
        )
        assertEquals("neutral", result.featured?.id)
        assertTrue(result.alternatives.any { it.id == "disliked" })
    }

    @Test
    fun onlyOneStarPlaceProducesNoFeaturedPick() {
        val result = engine.recommend(
            listOf(deal("disliked", 2)), places("disliked"),
            mapOf("disliked" to 1), HouseholdProfile(), today
        )
        assertNull(result.featured)
        assertEquals("disliked", result.alternatives.single().id)
    }

    @Test
    fun strongDealOutranksSlightlyBetterFoodFit() {
        val strong = deal("strong", 2)
        val weakFavorite = deal("favorite", 1)
        val result = engine.recommend(
            listOf(strong, weakFavorite), places("strong", "favorite"),
            mapOf("strong" to 4, "favorite" to 5), HouseholdProfile(), today
        )
        assertEquals("strong", result.featured?.id)
    }

    @Test
    fun fourConsistentNegativeTraitRatingsBlockFeaturedPick() {
        val quiz = (1..4).map { QuizPlace("fish$it", "Fish $it", "", setOf("Seafood")) } +
            QuizPlace("market", "Fish Market", "", setOf("Seafood"))
        val ratings = (1..4).associate { "fish$it" to 1 }
        val market = deal("market", 2)
        val result = engine.recommend(listOf(market), quiz, ratings, HouseholdProfile(), today)
        assertNull(result.featured)
        assertTrue(result.alternatives.contains(market))
    }

    @Test
    fun returnsThreeAlternativesWithDifferentCategoryWhenAvailable() {
        val deals = listOf(
            deal("winner", 2, "Burgers"), deal("second", 2, "Burgers"),
            deal("third", 1, "Burgers"), deal("different", 1, "Mexican"),
            deal("extra", 0, "Pizza")
        )
        val result = engine.recommend(deals, places(*deals.map { it.id }.toTypedArray()), emptyMap(), HouseholdProfile(), today)
        assertEquals(3, result.alternatives.size)
        assertTrue(result.alternatives.any { it.category == "Mexican" })
    }

    @Test
    fun twoStarStrongDealIsUsedOnlyWhenNoBetterRatedActiveDealExists() {
        val lowRated = deal("low", 2)
        val neutral = deal("neutral", 1)

        val withNeutral = engine.recommend(
            listOf(lowRated, neutral), places("low", "neutral"),
            mapOf("low" to 2), HouseholdProfile(), today
        )
        val onlyLowRated = engine.recommend(
            listOf(lowRated), places("low"),
            mapOf("low" to 2), HouseholdProfile(), today
        )

        assertEquals("neutral", withNeutral.featured?.id)
        assertEquals("low", onlyLowRated.featured?.id)
    }

    @Test
    fun threePositiveTraitRatingsBoostAnUntriedMatchingPlace() {
        val quiz = (1..3).map { QuizPlace("steak$it", "Steak $it", "", setOf("Steak")) } +
            QuizPlace("newSteak", "New Steakhouse", "", setOf("Steak")) +
            QuizPlace("other", "Other", "", setOf("Other"))
        val ratings = (1..3).associate { "steak$it" to 5 }

        val result = engine.recommend(
            listOf(deal("newSteak", 1), deal("other", 1)),
            quiz, ratings, HouseholdProfile(), today
        )

        assertEquals("newSteak", result.featured?.id)
    }

    @Test
    fun childEligibilityChangesThePickForFamilyAndSoloProfiles() {
        val kidsDeal = deal("kids", 2, forKids = true)
        val generalDeal = deal("general", 1)

        val familyResult = engine.recommend(
            listOf(kidsDeal, generalDeal), places("kids", "general"), emptyMap(),
            HouseholdProfile(adults = 2, childAges = listOf(8, 9)), today
        )
        val soloResult = engine.recommend(
            listOf(kidsDeal, generalDeal), places("kids", "general"), emptyMap(),
            HouseholdProfile(adults = 1), today
        )

        assertEquals("kids", familyResult.featured?.id)
        assertEquals("general", soloResult.featured?.id)
    }

    @Test
    fun possibleDealGetsLessStrengthThanVerifiedDeal() {
        val verified = deal("verified", 1)
        val possible = deal("possible", 1, verified = false)
        val result = engine.recommend(
            listOf(possible, verified), places("possible", "verified"),
            emptyMap(), HouseholdProfile(), today
        )

        assertEquals("verified", result.featured?.id)
        assertTrue(
            result.scoreBreakdown.getValue("possible").dealStrength <
                result.scoreBreakdown.getValue("verified").dealStrength
        )
    }

    @Test
    fun futureOfferDoesNotBoostTonightRecommendation() {
        val future = deal("future", 2, days = setOf(Calendar.TUESDAY))
        val preferred = deal("preferred", 0)
        val result = engine.recommend(
            listOf(future, preferred), places("future", "preferred"),
            mapOf("preferred" to 5), HouseholdProfile(), today
        )

        assertEquals("preferred", result.featured?.id)
        assertEquals(0.0, result.scoreBreakdown.getValue("future").dealStrength, 0.0)
    }

    private fun deal(
        id: String,
        rank: Int,
        category: String = "Test",
        forKids: Boolean = false,
        verified: Boolean = true,
        days: Set<Int> = setOf(today)
    ) = PlaceDeal(
        id, id, category, "Offer", days, forKids, rank, verified,
        "Address", "Terms", "Source", "Today"
    )

    private fun places(vararg ids: String) = ids.map { QuizPlace(it, it, "", setOf("General")) }
}
