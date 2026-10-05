package com.example.wherewegoing

import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.model.QuizPlace
import java.util.Calendar

// Curated prototype data. Check each offer with the location before visiting.
val elkGroveDeals = listOf(
    PlaceDeal(
        "smashburger", "Smashburger", "Quick burgers",
        "Kids eat free on Wednesdays", setOf(Calendar.WEDNESDAY), true, 2, true,
        "7701 Laguna Blvd, Elk Grove",
        "Kids 12 and under. One kids' meal with an adult meal (entrée, side, and drink). Dine in only.",
        "https://smashburger.com/kids-eat-free-details", "September 2026"
    ),
    PlaceDeal(
        "chevys", "Chevy's", "Sit-down Mexican",
        "Kids eat for $1 on Wednesdays", setOf(Calendar.WEDNESDAY), true, 1, true,
        "7401 Laguna Blvd, Elk Grove",
        "Kids 12 and under. One $1 kids' meal per regularly priced adult entrée. Dine in only.",
        "https://www.chevys.com/kids-eat-1dollar/", "September 2026"
    ),
    PlaceDeal(
        "dennys", "Denny's", "Family diner",
        "Kids may eat free on Tuesdays", setOf(Calendar.TUESDAY), true, 1, false,
        "8707 Elk Grove Blvd, Elk Grove",
        "Days vary by location. Call the Elk Grove restaurant to confirm before visiting.",
        "https://dennys.com/faqs-frequently-asked-questions", "September 2026"
    ),
    PlaceDeal(
        "pattys", "Patty's Pizza Shack", "Local pizza",
        "30% off a large one-topping pizza on Mondays and Tuesdays", setOf(Calendar.MONDAY, Calendar.TUESDAY), false, 1, true,
        "8591 Elk Grove Blvd, Elk Grove",
        "Pickup or dine in. Mention the deal when ordering. Limit one per order.",
        "https://www.pattyspizzashack.com/deals", "September 2026"
    ),
    PlaceDeal(
        "chuckecheese", "Chuck E. Cheese", "Family pizza",
        "Check current family deals", emptySet(), false, 0, false,
        "Elk Grove, California",
        "Offers change. Check the location's current coupons before visiting.",
        "https://www.chuckecheese.com/elk-grove-ca/coupons-and-deals/", "September 2026"
    )
)

val foodQuizPlaces = listOf(
    QuizPlace(
        "mcdonalds", "McDonald's", "Fast, familiar burgers and fries",
        setOf("Burgers", "Quick service", "Familiar favorites", "Value", "Kid friendly")
    ),
    QuizPlace(
        "innout", "In-N-Out Burger", "A focused burger menu with drive-through convenience",
        setOf("Burgers", "Quick service", "Familiar favorites", "Value")
    ),
    QuizPlace(
        "chevys", "Chevy's", "Sit-down Mexican food for the family",
        setOf("Mexican", "Sit-down", "Familiar favorites", "Kid friendly")
    ),
    QuizPlace(
        "pattys", "Patty's Pizza Shack", "A local, casual pizza place",
        setOf("Pizza", "Local places", "Casual", "Takeout", "Kid friendly")
    ),
    QuizPlace(
        "mikuni", "Mikuni", "Sushi and Japanese dishes in a polished setting",
        setOf("Japanese", "Sushi", "Seafood", "Adventurous", "Sit-down")
    ),
    QuizPlace(
        "oz", "Oz Korean BBQ", "Interactive all-you-can-eat Korean barbecue",
        setOf("Korean", "Adventurous", "Sit-down", "Group dining")
    ),
    QuizPlace(
        "dennys", "Denny's", "A familiar family diner with breakfast all day",
        setOf("Breakfast", "Sit-down", "Familiar favorites", "Value", "Kid friendly")
    ),
    QuizPlace(
        "smashburger", "Smashburger", "Fast-casual burgers, chicken, and shakes",
        setOf("Burgers", "Quick service", "Casual", "Kid friendly")
    ),
    QuizPlace(
        "tacobell", "Taco Bell", "Quick tacos, burritos, and customizable value meals",
        setOf("Mexican", "Quick service", "Value", "Takeout")
    ),
    QuizPlace(
        "chuckecheese", "Chuck E. Cheese", "Pizza with games and family entertainment",
        setOf("Pizza", "Kid friendly", "Group dining", "Casual")
    ),
    QuizPlace(
        "chipotle", "Chipotle", "Customizable burritos, bowls, tacos, and salads",
        setOf("Mexican", "Quick service", "Customizable", "Takeout")
    ),
    QuizPlace(
        "chickfila", "Chick-fil-A", "Chicken sandwiches, nuggets, salads, and waffle fries",
        setOf("Chicken", "Quick service", "Familiar favorites", "Kid friendly")
    ),
    QuizPlace(
        "raisingcanes", "Raising Cane's", "Chicken fingers, fries, toast, and dipping sauce",
        setOf("Chicken", "Quick service", "Familiar favorites", "Takeout")
    ),
    QuizPlace(
        "pandaexpress", "Panda Express", "American Chinese bowls and family-style meals",
        setOf("Chinese", "Quick service", "Familiar favorites", "Takeout")
    ),
    QuizPlace(
        "ihop", "IHOP", "Pancakes, breakfast favorites, burgers, and diner meals",
        setOf("Breakfast", "Sit-down", "Familiar favorites", "Kid friendly")
    )
)

const val INITIAL_FOOD_QUIZ_SIZE = 5

fun foodTraitScores(ratings: Map<String, Int>, places: List<QuizPlace> = foodQuizPlaces): Map<String, Int> {
    return places
        .filter { (ratings[it.id] ?: 0) in 1..5 }
        .flatMap { place -> place.traits.map { trait -> trait to ((ratings[place.id] ?: 3) - 3) } }
        .groupingBy { it.first }
        .fold(0) { total, pair -> total + pair.second }
}

fun nextQuizPlace(ratings: Map<String, Int>, places: List<QuizPlace> = foodQuizPlaces): QuizPlace? {
    val unanswered = places.filter { it.id !in ratings }
    if (unanswered.isEmpty()) return null
    if (ratings.isEmpty()) return places.first()

    val traitScores = foodTraitScores(ratings, places)
    val lastPlace = ratings.keys.lastOrNull()?.let { lastId -> places.find { it.id == lastId } }
    val lastRating = lastPlace?.let { ratings[it.id] } ?: 0

    return unanswered.maxByOrNull { candidate ->
        val uncertainTraits = candidate.traits.count { kotlin.math.abs(traitScores[it] ?: 0) <= 1 }
        val relatedComparison = if (lastRating in 4..5 && lastPlace != null) {
            candidate.traits.intersect(lastPlace.traits).size
        } else 0
        uncertainTraits * 3 + relatedComparison
    }
}

