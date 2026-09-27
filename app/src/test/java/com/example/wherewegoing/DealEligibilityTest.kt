package com.example.wherewegoing

import com.example.wherewegoing.domain.isDealEligibleForHousehold
import com.example.wherewegoing.model.HouseholdProfile
import com.example.wherewegoing.model.PlaceDeal
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DealEligibilityTest {
    private val kidsDeal = testDeal(forKids = true)

    @Test
    fun childAtMaximumAgeQualifies() {
        assertTrue(isDealEligibleForHousehold(kidsDeal, HouseholdProfile(childAges = listOf(12))))
    }

    @Test
    fun olderChildDoesNotQualify() {
        assertFalse(isDealEligibleForHousehold(kidsDeal, HouseholdProfile(childAges = listOf(13))))
    }

    @Test
    fun missingAgeDoesNotAssumeEligibility() {
        assertFalse(isDealEligibleForHousehold(kidsDeal, HouseholdProfile(childAges = listOf(null))))
    }

    @Test
    fun nonChildDealDoesNotRequireChildren() {
        assertTrue(isDealEligibleForHousehold(testDeal(forKids = false), HouseholdProfile()))
    }

    private fun testDeal(forKids: Boolean) = PlaceDeal(
        id = "test",
        name = "Test",
        category = "Test",
        offer = "Test",
        days = emptySet(),
        forKids = forKids,
        savingsRank = 0,
        verified = true,
        address = "",
        terms = "",
        source = "",
        checked = ""
    )
}
