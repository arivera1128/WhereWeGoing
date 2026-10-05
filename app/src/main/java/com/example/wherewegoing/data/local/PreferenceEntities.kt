package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "restaurant",
    primaryKeys = ["restaurant_id"],
    indices = [Index(value = ["prototype_key"], unique = true)]
)
data class RestaurantEntity(
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    val name: String,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "user_restaurant_rating",
    primaryKeys = ["user_id", "restaurant_id"],
    foreignKeys = [
        ForeignKey(
            entity = AppUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["restaurant_id"],
            childColumns = ["restaurant_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("restaurant_id")]
)
data class UserRestaurantRatingEntity(
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "response_type") val responseType: String,
    val rating: Int?,
    @ColumnInfo(name = "response_source") val responseSource: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

@Entity(
    tableName = "user_restaurant_exclusion",
    primaryKeys = ["user_restaurant_exclusion_id"],
    foreignKeys = [
        ForeignKey(
            entity = AppUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = RestaurantEntity::class,
            parentColumns = ["restaurant_id"],
            childColumns = ["restaurant_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("user_id"),
        Index("restaurant_id"),
        Index(value = ["user_id", "restaurant_id", "effective_through"])
    ]
)
data class UserRestaurantExclusionEntity(
    @ColumnInfo(name = "user_restaurant_exclusion_id") val exclusionId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "effective_from") val effectiveFrom: Long,
    @ColumnInfo(name = "effective_through") val effectiveThrough: Long?
)
