package com.example.wherewegoing.model

data class PlaceDeal(
    val id: String,
    val name: String,
    val category: String,
    val offer: String,
    val days: Set<Int>,
    val forKids: Boolean,
    val savingsRank: Int,
    val verified: Boolean,
    val address: String,
    val terms: String,
    val source: String,
    val checked: String,
    val restaurantId: String? = null,
    val locationId: String? = null,
    val dealVersionId: String? = null
)
