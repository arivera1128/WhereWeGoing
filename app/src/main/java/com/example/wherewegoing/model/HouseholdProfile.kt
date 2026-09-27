package com.example.wherewegoing.model

data class HouseholdProfile(
    val adults: Int = 1,
    val childAges: List<Int?> = emptyList(),
    val zip: String = "95758",
    val radiusMiles: Int = 10
) {
    val childCount: Int get() = childAges.size
    val householdSize: Int get() = adults + childCount
}
