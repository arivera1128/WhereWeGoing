package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation

@Entity(tableName = "household", primaryKeys = ["household_id"])
data class HouseholdEntity(
    @ColumnInfo(name = "household_id")
    val householdId: String,
    @ColumnInfo(name = "adult_count")
    val adultCount: Int,
    @ColumnInfo(name = "home_postal_code")
    val homePostalCode: String?,
    @ColumnInfo(name = "preferred_radius_miles")
    val preferredRadiusMiles: Double,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(
    tableName = "household_user",
    primaryKeys = ["household_id", "user_id"],
    foreignKeys = [
        ForeignKey(
            entity = HouseholdEntity::class,
            parentColumns = ["household_id"],
            childColumns = ["household_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AppUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("user_id")]
)
data class HouseholdUserEntity(
    @ColumnInfo(name = "household_id")
    val householdId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val role: String,
    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean,
    @ColumnInfo(name = "joined_at")
    val joinedAt: Long
)

@Entity(
    tableName = "household_child",
    primaryKeys = ["household_child_id"],
    foreignKeys = [
        ForeignKey(
            entity = HouseholdEntity::class,
            parentColumns = ["household_id"],
            childColumns = ["household_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("household_id")]
)
data class HouseholdChildEntity(
    @ColumnInfo(name = "household_child_id")
    val householdChildId: String,
    @ColumnInfo(name = "household_id")
    val householdId: String,
    @ColumnInfo(name = "display_order")
    val displayOrder: Int,
    @ColumnInfo(name = "birth_date")
    val birthDate: String?,
    @ColumnInfo(name = "entered_age_years")
    val enteredAgeYears: Int?,
    @ColumnInfo(name = "age_as_of_date")
    val ageAsOfDate: String?,
    @ColumnInfo(name = "age_reconfirm_after")
    val ageReconfirmAfter: String?,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

data class HouseholdWithChildren(
    @Embedded
    val household: HouseholdEntity,
    @Relation(
        parentColumn = "household_id",
        entityColumn = "household_id"
    )
    val children: List<HouseholdChildEntity>
)
