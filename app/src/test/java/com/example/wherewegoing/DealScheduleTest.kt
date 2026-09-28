package com.example.wherewegoing

import com.example.wherewegoing.domain.daysUntilNextOffer
import com.example.wherewegoing.domain.nextOfferDay
import com.example.wherewegoing.model.PlaceDeal
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DealScheduleTest {
    @Test
    fun findsTomorrowBeforeADealLaterInTheWeek() {
        val deal = deal(setOf(Calendar.TUESDAY, Calendar.FRIDAY))
        assertEquals(1, daysUntilNextOffer(deal, Calendar.MONDAY))
        assertEquals(Calendar.TUESDAY, nextOfferDay(deal, Calendar.MONDAY))
    }

    @Test
    fun todayIsNotTreatedAsAnUpcomingOffer() {
        val deal = deal(setOf(Calendar.MONDAY))
        assertNull(daysUntilNextOffer(deal, Calendar.MONDAY))
    }

    private fun deal(days: Set<Int>) = PlaceDeal(
        "test", "Test", "Test", "Offer", days, false, 1, true,
        "Address", "Terms", "Source", "Today"
    )
}
