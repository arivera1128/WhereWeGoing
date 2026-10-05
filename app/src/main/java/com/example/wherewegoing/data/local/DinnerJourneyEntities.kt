package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "meal_occasion",
    primaryKeys = ["meal_occasion_id"],
    foreignKeys = [
        ForeignKey(entity = HouseholdEntity::class, parentColumns = ["household_id"], childColumns = ["household_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AppUserEntity::class, parentColumns = ["user_id"], childColumns = ["created_by_user_id"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("household_id"), Index("created_by_user_id"), Index(value = ["household_id", "created_by_user_id", "meal_type", "service_date"])]
)
data class MealOccasionEntity(
    @ColumnInfo(name = "meal_occasion_id") val mealOccasionId: String,
    @ColumnInfo(name = "household_id") val householdId: String,
    @ColumnInfo(name = "created_by_user_id") val createdByUserId: String,
    @ColumnInfo(name = "meal_type") val mealType: String,
    @ColumnInfo(name = "service_date") val serviceDate: String,
    @ColumnInfo(name = "time_zone") val timeZone: String,
    val status: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "closed_at") val closedAt: Long?,
    @ColumnInfo(name = "created_from") val createdFrom: String
)

@Entity(
    tableName = "recommendation_run",
    primaryKeys = ["recommendation_run_id"],
    foreignKeys = [ForeignKey(entity = MealOccasionEntity::class, parentColumns = ["meal_occasion_id"], childColumns = ["meal_occasion_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index("meal_occasion_id")]
)
data class RecommendationRunEntity(
    @ColumnInfo(name = "recommendation_run_id") val recommendationRunId: String,
    @ColumnInfo(name = "meal_occasion_id") val mealOccasionId: String,
    @ColumnInfo(name = "engine_version") val engineVersion: String,
    @ColumnInfo(name = "requested_at") val requestedAt: Long,
    @ColumnInfo(name = "completed_at") val completedAt: Long
)

@Entity(
    tableName = "recommendation_option",
    primaryKeys = ["recommendation_option_id"],
    foreignKeys = [
        ForeignKey(entity = RecommendationRunEntity::class, parentColumns = ["recommendation_run_id"], childColumns = ["recommendation_run_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = RestaurantEntity::class, parentColumns = ["restaurant_id"], childColumns = ["restaurant_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = LocationEntity::class, parentColumns = ["location_id"], childColumns = ["location_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = DealVersionEntity::class, parentColumns = ["deal_version_id"], childColumns = ["deal_version_id"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("recommendation_run_id"), Index("restaurant_id"), Index("location_id"), Index("deal_version_id")]
)
data class RecommendationOptionEntity(
    @ColumnInfo(name = "recommendation_option_id") val recommendationOptionId: String,
    @ColumnInfo(name = "recommendation_run_id") val recommendationRunId: String,
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "deal_version_id") val dealVersionId: String?,
    @ColumnInfo(name = "displayed_rank") val displayedRank: Int,
    @ColumnInfo(name = "total_score") val totalScore: Double,
    @ColumnInfo(name = "was_featured") val wasFeatured: Boolean,
    @ColumnInfo(name = "prepared_at") val preparedAt: Long
)

@Entity(
    tableName = "plan_intent",
    primaryKeys = ["plan_intent_id"],
    foreignKeys = [
        ForeignKey(entity = MealOccasionEntity::class, parentColumns = ["meal_occasion_id"], childColumns = ["meal_occasion_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AppUserEntity::class, parentColumns = ["user_id"], childColumns = ["user_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = RecommendationOptionEntity::class, parentColumns = ["recommendation_option_id"], childColumns = ["recommendation_option_id"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("meal_occasion_id"), Index("user_id"), Index("recommendation_option_id")]
)
data class PlanIntentEntity(
    @ColumnInfo(name = "plan_intent_id") val planIntentId: String,
    @ColumnInfo(name = "meal_occasion_id") val mealOccasionId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "recommendation_option_id") val recommendationOptionId: String,
    val status: String,
    val source: String,
    @ColumnInfo(name = "declared_at") val declaredAt: Long,
    @ColumnInfo(name = "ended_at") val endedAt: Long?,
    @ColumnInfo(name = "superseded_by_intent_id") val supersededByIntentId: String?
)

@Entity(
    tableName = "plan_check_in",
    primaryKeys = ["plan_check_in_id"],
    foreignKeys = [ForeignKey(entity = PlanIntentEntity::class, parentColumns = ["plan_intent_id"], childColumns = ["plan_intent_id"], onDelete = ForeignKey.RESTRICT)],
    indices = [Index(value = ["plan_intent_id"], unique = true)]
)
data class PlanCheckInEntity(
    @ColumnInfo(name = "plan_check_in_id") val planCheckInId: String,
    @ColumnInfo(name = "plan_intent_id") val planIntentId: String,
    val status: String,
    @ColumnInfo(name = "eligible_at") val eligibleAt: Long,
    @ColumnInfo(name = "expires_at") val expiresAt: Long,
    @ColumnInfo(name = "first_presented_at") val firstPresentedAt: Long?,
    @ColumnInfo(name = "responded_at") val respondedAt: Long?,
    @ColumnInfo(name = "created_at") val createdAt: Long
)

@Entity(
    tableName = "plan_outcome",
    primaryKeys = ["plan_outcome_id"],
    foreignKeys = [
        ForeignKey(entity = PlanCheckInEntity::class, parentColumns = ["plan_check_in_id"], childColumns = ["plan_check_in_id"], onDelete = ForeignKey.RESTRICT),
        ForeignKey(entity = AppUserEntity::class, parentColumns = ["user_id"], childColumns = ["reported_by_user_id"], onDelete = ForeignKey.RESTRICT)
    ],
    indices = [Index("plan_check_in_id"), Index("reported_by_user_id")]
)
data class PlanOutcomeEntity(
    @ColumnInfo(name = "plan_outcome_id") val planOutcomeId: String,
    @ColumnInfo(name = "plan_check_in_id") val planCheckInId: String,
    @ColumnInfo(name = "reported_by_user_id") val reportedByUserId: String,
    @ColumnInfo(name = "outcome_type") val outcomeType: String,
    @ColumnInfo(name = "reporting_method") val reportingMethod: String,
    @ColumnInfo(name = "reported_at") val reportedAt: Long,
    @ColumnInfo(name = "supersedes_outcome_id") val supersedesOutcomeId: String?
)
