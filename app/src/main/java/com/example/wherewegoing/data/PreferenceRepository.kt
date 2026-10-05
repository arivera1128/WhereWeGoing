package com.example.wherewegoing.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.wherewegoing.data.local.RestaurantEntity
import com.example.wherewegoing.data.local.LocationEntity
import com.example.wherewegoing.data.local.DealEntity
import com.example.wherewegoing.data.local.DealVersionEntity
import com.example.wherewegoing.data.local.UserRestaurantExclusionEntity
import com.example.wherewegoing.data.local.UserRestaurantRatingEntity
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import com.example.wherewegoing.model.QuizPlace
import com.example.wherewegoing.model.PlaceDeal
import java.nio.charset.StandardCharsets
import java.util.UUID

data class PreferenceSnapshot(
    val ratings: Map<String, Int>,
    val removedPlaceKeys: Set<String>
)

interface PreferenceRepository {
    suspend fun load(userId: String, places: List<QuizPlace>): PreferenceSnapshot
    suspend fun saveRating(userId: String, placeKey: String, rating: Int)
    suspend fun setExcluded(userId: String, placeKey: String, excluded: Boolean)
}

class RoomPreferenceRepository(
    private val database: WhereWeGoingDatabase,
    private val legacyPreferences: SharedPreferences
) : PreferenceRepository {
    private val dao = database.preferenceDao()

    override suspend fun load(
        userId: String,
        places: List<QuizPlace>
    ): PreferenceSnapshot =
        database.withTransaction {
            val marker = "room_preferences_imported_$userId"
            if (!legacyPreferences.getBoolean(marker, false)) {
                val now = System.currentTimeMillis()
                places.forEach { place ->
                    val key = "quiz_rating_${place.id}"
                    if (legacyPreferences.contains(key)) {
                        val rating = legacyPreferences.getInt(key, 0)
                        dao.upsertRating(
                            UserRestaurantRatingEntity(
                                userId = userId,
                                restaurantId = dao.findRestaurant(place.id)?.restaurantId ?: restaurantId(place.id),
                                responseType = if (rating in 1..5) "RATED" else "NOT_TRIED",
                                rating = rating.takeIf { it in 1..5 },
                                responseSource = "LEGACY_IMPORT",
                                createdAt = now,
                                updatedAt = now
                            )
                        )
                    }
                }
                legacyPreferences.getStringSet("removed", emptySet()).orEmpty().forEach { placeKey ->
                    dao.findRestaurant(placeKey)?.let { restaurant ->
                        dao.upsertExclusion(
                            UserRestaurantExclusionEntity(
                                exclusionId = UUID.randomUUID().toString(),
                                userId = userId,
                                restaurantId = restaurant.restaurantId,
                                effectiveFrom = now,
                                effectiveThrough = null
                            )
                        )
                    }
                }
                legacyPreferences.edit().putBoolean(marker, true).commit()
            }

            PreferenceSnapshot(
                ratings = dao.findRatings(userId).associate { row ->
                    row.prototypeKey to (row.rating ?: 0)
                },
                removedPlaceKeys = dao.findCurrentExclusionKeys(userId).toSet()
            )
        }

    override suspend fun saveRating(userId: String, placeKey: String, rating: Int) {
        val restaurant = dao.findRestaurant(placeKey) ?: return
        val now = System.currentTimeMillis()
        dao.upsertRating(
            UserRestaurantRatingEntity(
                userId = userId,
                restaurantId = restaurant.restaurantId,
                responseType = if (rating in 1..5) "RATED" else "NOT_TRIED",
                rating = rating.takeIf { it in 1..5 },
                responseSource = "APP",
                createdAt = now,
                updatedAt = now
            )
        )
    }

    override suspend fun setExcluded(userId: String, placeKey: String, excluded: Boolean) {
        val restaurant = dao.findRestaurant(placeKey) ?: return
        val now = System.currentTimeMillis()
        if (excluded) {
            if (dao.hasCurrentExclusion(userId, restaurant.restaurantId)) return
            dao.upsertExclusion(
                UserRestaurantExclusionEntity(
                    exclusionId = UUID.randomUUID().toString(),
                    userId = userId,
                    restaurantId = restaurant.restaurantId,
                    effectiveFrom = now,
                    effectiveThrough = null
                )
            )
        } else {
            dao.endExclusion(userId, restaurant.restaurantId, now)
        }
    }

    private fun restaurantId(prototypeKey: String): String = stableId("restaurant", prototypeKey)

    private fun stableId(type: String, key: String): String = UUID.nameUUIDFromBytes(
        "where-we-going:$type:$key".toByteArray(StandardCharsets.UTF_8)
    ).toString()
}

