package com.example.wherewegoing.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface HouseholdDao {
    @Upsert
    suspend fun upsertUser(user: AppUserEntity)

    @Upsert
    suspend fun upsertInstallation(installation: AppInstallationEntity)

    @Upsert
    suspend fun upsertHousehold(household: HouseholdEntity)

    @Upsert
    suspend fun upsertHouseholdUser(householdUser: HouseholdUserEntity)

    @Upsert
    suspend fun upsertChildren(children: List<HouseholdChildEntity>)

    @Query("SELECT * FROM app_user WHERE user_id = :userId")
    suspend fun findUser(userId: String): AppUserEntity?

    @Query("SELECT user_id FROM app_installation WHERE installation_id = :installationId")
    suspend fun findUserIdForInstallation(installationId: String): String?

    @Query("DELETE FROM household_child WHERE household_id = :householdId")
    suspend fun deleteChildrenForHousehold(householdId: String)

    @Transaction
    @Query("SELECT * FROM household WHERE household_id = :householdId")
    fun observeHousehold(householdId: String): Flow<HouseholdWithChildren?>

    @Transaction
    @Query("SELECT * FROM household WHERE household_id = :householdId")
    suspend fun findHousehold(householdId: String): HouseholdWithChildren?

    @Query(
        """
        SELECT household_id
        FROM household_user
        WHERE user_id = :userId AND is_primary = 1
        LIMIT 1
        """
    )
    suspend fun findPrimaryHouseholdId(userId: String): String?
}
