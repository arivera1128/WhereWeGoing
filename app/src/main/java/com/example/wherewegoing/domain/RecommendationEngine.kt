package com.example.wherewegoing.domain

import com.example.wherewegoing.model.HouseholdProfile
import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.model.QuizPlace

data class RecommendationResult(
    val featured: PlaceDeal?,
    val alternatives: List<PlaceDeal>,
    val hasActiveVerifiedDeal: Boolean,
    val scoreBreakdown: Map<String, RecommendationScore>
)

data class RecommendationScore(
    val dealStrength: Double,
    val foodFit: Double,
    val total: Double,
    val blockedFoodTraits: Set<String>
)

class RecommendationEngine {
    fun recommend(
        deals: List<PlaceDeal>,
        quizPlaces: List<QuizPlace>,
        ratings: Map<String, Int>,
        household: HouseholdProfile,
        today: Int
    ): RecommendationResult {
        val traitsByPlace = quizPlaces.associate { it.id to it.traits }
        val traitEvidence = buildTraitEvidence(quizPlaces, ratings)
        val blockedTraits = traitEvidence.filterValues { evidence ->
            evidence.size >= 4 && evidence.count { it <= 2 }.toDouble() / evidence.size >= 0.75
        }.keys

        val scores = deals.associate { deal ->
            val traits = traitsByPlace[deal.id].orEmpty()
            val directRating = ratings[deal.id]?.takeIf { it in 1..5 }
            val dealStrength = deal.savingsRank.coerceIn(0, 2) / 2.0 * 65.0
            val directFit = when (directRating) {
                1 -> -10.0
                2 -> -6.0
                4 -> 5.0
                5 -> 10.0
                else -> 0.0
            }
            val learnedFit = if (traits.isEmpty()) 0.0 else traits.map { trait ->
                val values = traitEvidence[trait].orEmpty()
                if (values.isEmpty()) 0.0 else {
                    val average = values.map { (it - 3) / 2.0 }.average()
                    val confidence = (values.size / 3.0).coerceAtMost(1.0)
                    average * confidence * 25.0
                }
            }.average()
            val foodFit = (directFit + learnedFit).coerceIn(-35.0, 35.0)
            deal.id to RecommendationScore(
                dealStrength = dealStrength,
                foodFit = foodFit,
                total = dealStrength + foodFit,
                blockedFoodTraits = traits.intersect(blockedTraits)
            )
        }

        val active = deals.filter { deal ->
            today in deal.days && deal.verified && isDealEligibleForHousehold(deal, household)
        }
        fun rating(deal: PlaceDeal): Int? = ratings[deal.id]?.takeIf { it in 1..5 }
        fun canFeature(deal: PlaceDeal): Boolean =
            rating(deal) != 1 && scores.getValue(deal.id).blockedFoodTraits.isEmpty()

        val acceptableActive = active.filter { canFeature(it) && rating(it) != 2 }
        val featured = when {
            acceptableActive.isNotEmpty() -> acceptableActive.maxBy { scores.getValue(it.id).total }
            else -> active.filter { canFeature(it) && rating(it) == 2 && it.savingsRank >= 2 }
                .maxByOrNull { scores.getValue(it.id).total }
                ?: deals.filter { canFeature(it) && rating(it) != 2 }
                    .maxByOrNull { scores.getValue(it.id).foodFit }
        }

        val orderedOthers = deals.filter { it != featured }
            .sortedByDescending { scores.getValue(it.id).total }
        val alternatives = selectAlternatives(featured, orderedOthers)

        return RecommendationResult(
            featured = featured,
            alternatives = alternatives,
            hasActiveVerifiedDeal = featured in active,
            scoreBreakdown = scores
        )
    }

    private fun buildTraitEvidence(
        quizPlaces: List<QuizPlace>,
        ratings: Map<String, Int>
    ): Map<String, List<Int>> = quizPlaces
        .mapNotNull { place -> ratings[place.id]?.takeIf { it in 1..5 }?.let { place to it } }
        .flatMap { (place, rating) -> place.traits.map { trait -> trait to rating } }
        .groupBy({ it.first }, { it.second })

    private fun selectAlternatives(featured: PlaceDeal?, ordered: List<PlaceDeal>): List<PlaceDeal> {
        if (ordered.size <= 3) return ordered
        val strongest = ordered.take(2).toMutableList()
        val referenceCategories = (listOfNotNull(featured) + strongest).map { it.category }.toSet()
        val varied = ordered.drop(2).firstOrNull { it.category !in referenceCategories }
            ?: ordered[2]
        strongest += varied
        return strongest
    }
}
