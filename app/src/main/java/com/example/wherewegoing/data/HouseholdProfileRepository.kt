package com.example.wherewegoing.data

import android.content.SharedPreferences
import com.example.wherewegoing.model.HouseholdProfile

interface HouseholdProfileRepository {
    fun hasProfile(): Boolean
    fun load(): HouseholdProfile
    fun save(profile: HouseholdProfile)
}

class SharedPreferencesHouseholdProfileRepository(
    private val preferences: SharedPreferences
) : HouseholdProfileRepository {
    override fun hasProfile(): Boolean =
        preferences.getBoolean("onboarding_complete", false) || preferences.contains("family_size")

    override fun load(): HouseholdProfile {
        val legacyChildren = preferences.getString("kids", "0")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val total = preferences.getString("family_size", "")?.toIntOrNull()
        val adults = if (preferences.contains("adult_count")) {
            preferences.getInt("adult_count", 1)
        } else {
            ((total ?: 1) - legacyChildren).coerceAtLeast(1)
        }
        val storedAges = preferences.getString("child_ages", "").orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .map { it.toIntOrNull() }
        val childAges = List(legacyChildren) { index -> storedAges.getOrNull(index) }

        return HouseholdProfile(
            adults = adults,
            childAges = childAges,
            zip = preferences.getString("zip", "95758") ?: "95758",
            radiusMiles = preferences.getInt("radius", 10)
        )
    }

    override fun save(profile: HouseholdProfile) {
        val ageGroups = profile.childAges.filterNotNull().mapTo(mutableSetOf()) { age ->
            when (age) {
                in 0..4 -> "0–4"
                in 5..12 -> "5–12"
                else -> "13–17"
            }
        }
        preferences.edit()
            .putBoolean("onboarding_complete", true)
            .putInt("adult_count", profile.adults)
            .putString("child_ages", profile.childAges.joinToString(",") { it?.toString().orEmpty() })
            .putString("family_size", profile.householdSize.toString())
            .putString("kids", profile.childCount.toString())
            .putStringSet("ages", ageGroups)
            .putString("zip", profile.zip)
            .putInt("radius", profile.radiusMiles)
            .apply()
    }
}
