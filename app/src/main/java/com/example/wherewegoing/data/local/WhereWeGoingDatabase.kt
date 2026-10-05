package com.example.wherewegoing.data.local

import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AppUserEntity::class,
        UserAuthIdentity::class,
        AppInstallationEntity::class,
        HouseholdEntity::class,
        HouseholdUserEntity::class,
        HouseholdChildEntity::class,
        RestaurantEntity::class,
        LocationEntity::class,
        DealEntity::class,
        DealVersionEntity::class,
        CatalogMetadataEntity::class,
        CatalogSyncLink::class,
        CatalogSyncState::class,
        CatalogOfferLocation::class,
        CatalogTraitEntity::class,
        DealScheduleEntity::class,
        MealOccasionEntity::class,
        RecommendationRunEntity::class,
        RecommendationOptionEntity::class,
        PlanIntentEntity::class,
        PlanCheckInEntity::class,
        PlanOutcomeEntity::class,
        UserRestaurantRatingEntity::class,
        UserRestaurantExclusionEntity::class
    ],
    version = 7,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7)
    ],
    exportSchema = true
)
abstract class WhereWeGoingDatabase : RoomDatabase() {
    abstract fun userAuthIdentityDao(): UserAuthIdentityDao
    abstract fun catalogSyncDao(): CatalogSyncDao
    abstract fun catalogDao(): CatalogDao
    abstract fun householdDao(): HouseholdDao
    abstract fun preferenceDao(): PreferenceDao
    abstract fun dinnerJourneyDao(): DinnerJourneyDao

    companion object {
        @Volatile
        private var instance: WhereWeGoingDatabase? = null

        fun getInstance(context: Context): WhereWeGoingDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    WhereWeGoingDatabase::class.java,
                    "where_we_going.db"
                ).build().also { instance = it }
            }
    }
}

