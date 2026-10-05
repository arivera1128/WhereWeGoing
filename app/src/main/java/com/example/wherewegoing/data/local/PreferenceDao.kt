package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

data class RatingByPrototypeKey(
    @ColumnInfo(name = "prototype_key") val prototypeKey: String,
    val rating: Int?
)

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertLocations(locations: List<LocationEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDeals(deals: List<DealEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDealVersions(versions: List<DealVersionEntity>)

    @Query("SELECT * FROM restaurant WHERE prototype_key = :prototypeKey")
    suspend fun findRestaurant(prototypeKey: String): RestaurantEntity?

    @Upsert
    suspend fun upsertRating(rating: UserRestaurantRatingEntity)

    @Query(
        """
        SELECT restaurant.prototype_key, user_restaurant_rating.rating
        FROM user_restaurant_rating
        JOIN restaurant USING (restaurant_id)
        WHERE user_restaurant_rating.user_id = :userId
        """
    )
    suspend fun findRatings(userId: String): List<RatingByPrototypeKey>

    @Upsert
    suspend fun upsertExclusion(exclusion: UserRestaurantExclusionEntity)

    @Query(
        """
        UPDATE user_restaurant_exclusion
        SET effective_through = :endedAt
        WHERE user_id = :userId
          AND restaurant_id = :restaurantId
          AND effective_through IS NULL
        """
    )
    suspend fun endExclusion(userId: String, restaurantId: String, endedAt: Long)

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM user_restaurant_exclusion
            WHERE user_id = :userId
              AND restaurant_id = :restaurantId
              AND effective_through IS NULL
        )
        """
    )
    suspend fun hasCurrentExclusion(userId: String, restaurantId: String): Boolean

    @Query(
        """
        SELECT restaurant.prototype_key
        FROM user_restaurant_exclusion
        JOIN restaurant USING (restaurant_id)
        WHERE user_restaurant_exclusion.user_id = :userId
          AND user_restaurant_exclusion.effective_through IS NULL
        """
    )
    suspend fun findCurrentExclusionKeys(userId: String): List<String>
}

