package com.example.wherewegoing.data

import androidx.room.withTransaction
import com.example.wherewegoing.data.local.*
import com.example.wherewegoing.model.MealRecord
import com.example.wherewegoing.model.PlaceDeal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

data class DinnerJourneySnapshot(
    val pendingPlaceKey: String?,
    val pendingHasOffer: Boolean,
    val checkInReady: Boolean,
    val mealHistory: List<MealRecord>,
    val pendingPlace: PlaceDeal? = null
)

class RoomDinnerJourneyRepository(private val database: WhereWeGoingDatabase) {
    private val dao = database.dinnerJourneyDao()
    private val zone = TimeZone.getTimeZone("America/Los_Angeles")

    suspend fun load(userId: String): DinnerJourneySnapshot = database.withTransaction {
        val now = System.currentTimeMillis()
        var pending = dao.findPendingPlan(userId)
        if (pending != null && now > pending.expiresAt) {
            val checkIn = dao.findCheckInById(pending.planCheckInId)!!
            dao.updateCheckIn(checkIn.copy(status = "EXPIRED"))
            dao.findOccasion(pending.mealOccasionId)?.let {
                dao.updateOccasion(it.copy(status = "EXPIRED", closedAt = now))
            }
            pending = null
        } else if (pending != null && now >= pending.eligibleAt && pending.checkInStatus == "PENDING") {
            val checkIn = dao.findCheckInById(pending.planCheckInId)!!
            dao.updateCheckIn(checkIn.copy(status = "PRESENTED", firstPresentedAt = now))
            pending = pending.copy(checkInStatus = "PRESENTED")
        }
        DinnerJourneySnapshot(
            pendingPlaceKey = pending?.prototypeKey,
            pendingHasOffer = pending?.dealVersionId != null,
            checkInReady = pending?.checkInStatus == "PRESENTED",
            pendingPlace = pending?.let { row ->
                val days=database.catalogDao().schedules().filter { it.versionId==row.dealVersionId }.map { it.weekday }.toSet()
                PlaceDeal(row.prototypeKey,row.name,row.category,row.offer,days,false,0,false,row.address,row.terms,"","",
                    dealVersionId=row.dealVersionId)
            },
            mealHistory = dao.findMealHistory(userId).map { row ->
                MealRecord(
                    dealId = row.prototypeKey,
                    result = when (row.outcomeType) {
                        "DEAL_USED_WORKED" -> "worked"
                        "DEAL_USED_FAILED" -> "did_not_work"
                        else -> "visited"
                    },
                    recordedAt = row.reportedAt
                )
            }
        )
    }

    suspend fun selectPlan(
        userId: String,
        householdId: String,
        placeKey: String,
        hasOffer: Boolean,
        featured: Boolean,
        selection: PlaceDeal? = null
    ) = database.withTransaction {
        val catalog = if (selection?.restaurantId != null && selection.locationId != null) {
            CatalogSelection(selection.restaurantId,selection.locationId,selection.dealVersionId)
        } else dao.findCatalogSelection(placeKey) ?: return@withTransaction
        val now = System.currentTimeMillis()
        val serviceDate = localDate(now)
        val occasion = dao.findActiveOccasion(householdId, userId, serviceDate)
            ?: MealOccasionEntity(
                mealOccasionId = UUID.randomUUID().toString(), householdId = householdId,
                createdByUserId = userId, mealType = "DINNER", serviceDate = serviceDate,
                timeZone = zone.id, status = "PLANNING", startedAt = now, closedAt = null,
                createdFrom = "HOME"
            ).also { dao.insertOccasion(it) }
        val runId = UUID.randomUUID().toString()
        dao.insertRun(RecommendationRunEntity(runId, occasion.mealOccasionId, "LOCAL_V1", now, now))
        val optionId = UUID.randomUUID().toString()
        dao.insertOption(
            RecommendationOptionEntity(
                optionId, runId, catalog.restaurantId, catalog.locationId,
                catalog.dealVersionId.takeIf { hasOffer }, 1, 0.0, featured, now
            )
        )
        val newIntentId = UUID.randomUUID().toString()
        dao.findCurrentIntent(occasion.mealOccasionId)?.let { old ->
            dao.updateIntent(old.copy(status = "SUPERSEDED", endedAt = now, supersededByIntentId = newIntentId))
            dao.findCheckIn(old.planIntentId)?.let { dao.updateCheckIn(it.copy(status = "CANCELED")) }
        }
        dao.insertIntent(
            PlanIntentEntity(
                newIntentId, occasion.mealOccasionId, userId, optionId, "CURRENT",
                if (featured) "FEATURED_PICK" else "ALTERNATIVE", now, null, null
            )
        )
        dao.insertCheckIn(
            PlanCheckInEntity(
                UUID.randomUUID().toString(), newIntentId, "PENDING", now + 60_000,
                expirationFor(serviceDate), null, null, now
            )
        )
        dao.updateOccasion(occasion.copy(status = "PLANNED"))
    }

    suspend fun cancelCurrentPlan(userId: String) = database.withTransaction {
        val pending = dao.findPendingPlan(userId) ?: return@withTransaction
        val now = System.currentTimeMillis()
        val intent = dao.findCurrentIntent(pending.mealOccasionId) ?: return@withTransaction
        dao.updateIntent(intent.copy(status = "RETRACTED", endedAt = now))
        dao.findCheckIn(intent.planIntentId)?.let { dao.updateCheckIn(it.copy(status = "CANCELED")) }
        dao.findOccasion(pending.mealOccasionId)?.let {
            dao.updateOccasion(it.copy(status = "CANCELED", closedAt = now))
        }
    }

    suspend fun recordOutcome(userId: String, result: String) = database.withTransaction {
        val pending = dao.findPendingPlan(userId) ?: return@withTransaction
        val now = System.currentTimeMillis()
        val type = if (pending.dealVersionId != null) {
            when (result) {
                "worked" -> "DEAL_USED_WORKED"
                "did_not_work" -> "DEAL_USED_FAILED"
                else -> "DEAL_NOT_USED"
            }
        } else if (result == "visited") "PLACE_VISITED" else "PLACE_NOT_VISITED"
        dao.insertOutcome(
            PlanOutcomeEntity(
                UUID.randomUUID().toString(), pending.planCheckInId, userId, type,
                "IN_APP_SELF_REPORT", now, null
            )
        )
        dao.findCheckInById(pending.planCheckInId)?.let {
            dao.updateCheckIn(it.copy(status = "ANSWERED", respondedAt = now))
        }
        dao.findOccasion(pending.mealOccasionId)?.let {
            dao.updateOccasion(it.copy(status = "RESOLVED", closedAt = now))
        }
    }

    private fun localDate(value: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = zone }.format(value)

    private fun expirationFor(serviceDate: String): Long {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { timeZone = zone }.parse(serviceDate)!!
        return Calendar.getInstance(zone).apply {
            time = parsed
            add(Calendar.DAY_OF_MONTH, 2)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis
    }
}
