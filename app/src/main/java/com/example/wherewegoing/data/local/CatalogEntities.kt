package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "location",
    primaryKeys = ["location_id"],
    foreignKeys = [ForeignKey(
        entity = RestaurantEntity::class,
        parentColumns = ["restaurant_id"],
        childColumns = ["restaurant_id"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("restaurant_id"), Index(value = ["prototype_key"], unique = true)]
)
data class LocationEntity(
    @ColumnInfo(name = "location_id") val locationId: String,
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    val address: String,
    @ColumnInfo(name = "time_zone") val timeZone: String,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "deal",
    primaryKeys = ["deal_id"],
    foreignKeys = [ForeignKey(
        entity = RestaurantEntity::class,
        parentColumns = ["restaurant_id"],
        childColumns = ["restaurant_id"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("restaurant_id"), Index(value = ["prototype_key"], unique = true)]
)
data class DealEntity(
    @ColumnInfo(name = "deal_id") val dealId: String,
    @ColumnInfo(name = "restaurant_id") val restaurantId: String,
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "deal_version",
    primaryKeys = ["deal_version_id"],
    foreignKeys = [ForeignKey(
        entity = DealEntity::class,
        parentColumns = ["deal_id"],
        childColumns = ["deal_id"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("deal_id")]
)
data class DealVersionEntity(
    @ColumnInfo(name = "deal_version_id") val dealVersionId: String,
    @ColumnInfo(name = "deal_id") val dealId: String,
    @ColumnInfo(name = "version_number") val versionNumber: Int,
    val offer: String,
    val terms: String,
    val status: String,
    @ColumnInfo(name = "published_at") val publishedAt: Long
)
