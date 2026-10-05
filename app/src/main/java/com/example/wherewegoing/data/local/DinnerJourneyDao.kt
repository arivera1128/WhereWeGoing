package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

data class CatalogSelection(
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "deal_version_id") val dealVersionId: String?
)

data class PendingPlanRow(
    @ColumnInfo(name = "meal_occasion_id") val mealOccasionId: String,
    @ColumnInfo(name = "plan_intent_id") val planIntentId: String,
    @ColumnInfo(name = "plan_check_in_id") val planCheckInId: String,
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    @ColumnInfo(name = "deal_version_id") val dealVersionId: String?,
    @ColumnInfo(name = "eligible_at") val eligibleAt: Long,
    @ColumnInfo(name = "expires_at") val expiresAt: Long,
    @ColumnInfo(name = "check_in_status") val checkInStatus: String,
    val name: String, val address: String, val offer: String, val terms: String, val category: String
)

data class MealOutcomeRow(
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    @ColumnInfo(name = "outcome_type") val outcomeType: String,
    @ColumnInfo(name = "reported_at") val reportedAt: Long
)

@Dao
interface DinnerJourneyDao {
    @Query(
        """
        SELECT restaurant.restaurant_id, location.location_id, deal_version.deal_version_id
        FROM restaurant
        JOIN location ON location.restaurant_id = restaurant.restaurant_id
        LEFT JOIN deal ON deal.restaurant_id = restaurant.restaurant_id
        LEFT JOIN deal_version ON deal_version.deal_id = deal.deal_id AND deal_version.status = 'PUBLISHED'
        WHERE restaurant.prototype_key = :prototypeKey
        LIMIT 1
        """
    )
    suspend fun findCatalogSelection(prototypeKey: String): CatalogSelection?

    @Query("SELECT * FROM meal_occasion WHERE household_id = :householdId AND created_by_user_id = :userId AND meal_type = 'DINNER' AND service_date = :serviceDate AND status IN ('PLANNING','PLANNED') LIMIT 1")
    suspend fun findActiveOccasion(householdId: String, userId: String, serviceDate: String): MealOccasionEntity?

    @Query("SELECT * FROM plan_intent WHERE meal_occasion_id = :occasionId AND status = 'CURRENT' LIMIT 1")
    suspend fun findCurrentIntent(occasionId: String): PlanIntentEntity?

    @Query("SELECT * FROM plan_check_in WHERE plan_intent_id = :intentId LIMIT 1")
    suspend fun findCheckIn(intentId: String): PlanCheckInEntity?

    @Query("SELECT * FROM meal_occasion WHERE meal_occasion_id = :occasionId")
    suspend fun findOccasion(occasionId: String): MealOccasionEntity?

    @Query("SELECT * FROM plan_check_in WHERE plan_check_in_id = :checkInId")
    suspend fun findCheckInById(checkInId: String): PlanCheckInEntity?

    @Query(
        """
        SELECT meal_occasion.meal_occasion_id, plan_intent.plan_intent_id,
               plan_check_in.plan_check_in_id, restaurant.prototype_key,
               recommendation_option.deal_version_id, plan_check_in.eligible_at,
               plan_check_in.expires_at, plan_check_in.status AS check_in_status,
               restaurant.name, location.address, COALESCE(deal_version.offer,'Restaurant pick') AS offer,
               COALESCE(deal_version.terms,'') AS terms, COALESCE(catalog_metadata.category,'Place to eat') AS category
        FROM meal_occasion
        JOIN plan_intent ON plan_intent.meal_occasion_id = meal_occasion.meal_occasion_id AND plan_intent.status = 'CURRENT'
        JOIN plan_check_in ON plan_check_in.plan_intent_id = plan_intent.plan_intent_id
        JOIN recommendation_option ON recommendation_option.recommendation_option_id = plan_intent.recommendation_option_id
        JOIN restaurant ON restaurant.restaurant_id = recommendation_option.restaurant_id
        JOIN location ON location.location_id=recommendation_option.location_id
        LEFT JOIN deal_version ON deal_version.deal_version_id=recommendation_option.deal_version_id
        LEFT JOIN catalog_metadata ON catalog_metadata.prototype_key=restaurant.prototype_key
        WHERE meal_occasion.created_by_user_id = :userId
          AND meal_occasion.status = 'PLANNED'
          AND plan_check_in.status IN ('PENDING','PRESENTED')
        ORDER BY plan_intent.declared_at DESC LIMIT 1
        """
    )
    suspend fun findPendingPlan(userId: String): PendingPlanRow?

    @Query(
        """
        SELECT restaurant.prototype_key, plan_outcome.outcome_type, plan_outcome.reported_at
        FROM plan_outcome
        JOIN plan_check_in USING (plan_check_in_id)
        JOIN plan_intent USING (plan_intent_id)
        JOIN recommendation_option USING (recommendation_option_id)
        JOIN restaurant USING (restaurant_id)
        WHERE plan_outcome.reported_by_user_id = :userId
          AND plan_outcome.supersedes_outcome_id IS NULL
          AND plan_outcome.outcome_type IN ('DEAL_USED_WORKED','DEAL_USED_FAILED','PLACE_VISITED')
        ORDER BY plan_outcome.reported_at DESC LIMIT 50
        """
    )
    suspend fun findMealHistory(userId: String): List<MealOutcomeRow>

    @Insert suspend fun insertOccasion(value: MealOccasionEntity)
    @Insert suspend fun insertRun(value: RecommendationRunEntity)
    @Insert suspend fun insertOption(value: RecommendationOptionEntity)
    @Insert suspend fun insertIntent(value: PlanIntentEntity)
    @Insert suspend fun insertCheckIn(value: PlanCheckInEntity)
    @Insert suspend fun insertOutcome(value: PlanOutcomeEntity)
    @Update suspend fun updateOccasion(value: MealOccasionEntity)
    @Update suspend fun updateIntent(value: PlanIntentEntity)
    @Update suspend fun updateCheckIn(value: PlanCheckInEntity)
}
