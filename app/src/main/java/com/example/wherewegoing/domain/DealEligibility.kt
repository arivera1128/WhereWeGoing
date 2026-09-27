package com.example.wherewegoing.domain

import com.example.wherewegoing.model.HouseholdProfile
import com.example.wherewegoing.model.PlaceDeal

fun isDealEligibleForHousehold(deal: PlaceDeal, profile: HouseholdProfile): Boolean {
    if (!deal.forKids) return true
    return profile.childAges.any { age -> age != null && age <= 12 }
}
