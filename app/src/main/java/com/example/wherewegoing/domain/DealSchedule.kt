package com.example.wherewegoing.domain

import com.example.wherewegoing.model.PlaceDeal
import java.util.Calendar

fun daysUntilNextOffer(deal: PlaceDeal, today: Int): Int? = deal.days
    .map { offerDay -> (offerDay - today + 7) % 7 }
    .filter { it > 0 }
    .minOrNull()

fun dayName(day: Int): String = when (day) {
    Calendar.SUNDAY -> "Sunday"
    Calendar.MONDAY -> "Monday"
    Calendar.TUESDAY -> "Tuesday"
    Calendar.WEDNESDAY -> "Wednesday"
    Calendar.THURSDAY -> "Thursday"
    Calendar.FRIDAY -> "Friday"
    Calendar.SATURDAY -> "Saturday"
    else -> "Upcoming"
}

fun nextOfferDay(deal: PlaceDeal, today: Int): Int? = deal.days
    .map { offerDay -> offerDay to ((offerDay - today + 7) % 7) }
    .filter { (_, distance) -> distance > 0 }
    .minByOrNull { (_, distance) -> distance }
    ?.first
