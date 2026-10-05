package com.example.wherewegoing.data

import android.content.SharedPreferences
import androidx.room.withTransaction
import com.example.wherewegoing.data.local.AppInstallationEntity
import com.example.wherewegoing.data.local.AppUserEntity
import com.example.wherewegoing.data.local.HouseholdChildEntity
import com.example.wherewegoing.data.local.HouseholdEntity
import com.example.wherewegoing.data.local.HouseholdUserEntity
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import com.example.wherewegoing.model.HouseholdProfile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface HouseholdProfileRepository {
    suspend fun load(): HouseholdProfileSnapshot
    suspend fun save(profile: HouseholdProfile)
    suspend fun reset()
}

data class HouseholdProfileSnapshot(
    val profile: HouseholdProfile,
    val hasProfile: Boolean,
    val userId: String,
    val householdId: String
)

class RoomHouseholdProfileRepository(
    private val database: WhereWeGoingDatabase,
    private val preferences: SharedPreferences
) : HouseholdProfileRepository {
    private val dao = database.householdDao()

    override suspend fun load(): HouseholdProfileSnapshot = database.withTransaction {
        val identity = ensureLocalIdentity()
        val user = dao.findUser(identity.userId) ?: error("Local user was not created")
        val household = dao.findHousehold(identity.householdId) ?: error("Local household was not created")

        HouseholdProfileSnapshot(
            profile = household.toProfile(),
            hasProfile = user.onboardingStatus == ONBOARDING_COMPLETE,
            userId = identity.userId,
            householdId = identity.householdId
        )
    }

    override suspend fun save(profile: HouseholdProfile) = database.withTransaction {
        val identity = ensureLocalIdentity()
        val now = System.currentTimeMillis()
        val existingUser = dao.findUser(identity.userId) ?: error("Local user was not created")
        val existingHousehold = dao.findHousehold(identity.householdId) ?: error("Local household was not created")

        dao.upsertUser(
            existingUser.copy(
                onboardingStatus = ONBOARDING_COMPLETE,
                updatedAt = now
            )
        )
        dao.upsertHousehold(
            existingHousehold.household.copy(
                adultCount = profile.adults,
                homePostalCode = profile.zip,
                preferredRadiusMiles = profile.radiusMiles.toDouble(),
                updatedAt = now
            )
        )
        dao.deleteChildrenForHousehold(identity.householdId)
        dao.upsertChildren(
            profile.childAges.mapIndexed { index, age ->
                HouseholdChildEntity(
                    householdChildId = existingHousehold.children.getOrNull(index)?.householdChildId
                        ?: UUID.randomUUID().toString(),
                    householdId = identity.householdId,
                    displayOrder = index,
                    birthDate = null,
                    enteredAgeYears = age,
                    ageAsOfDate = age?.let { currentDate() },
                    ageReconfirmAfter = null,
                    createdAt = existingHousehold.children.getOrNull(index)?.createdAt ?: now,
                    updatedAt = now
                )
            }
        )
    }

    override suspend fun reset() {
        withContext(Dispatchers.IO) { database.clearAllTables() }
        preferences.edit().remove(INSTALLATION_ID_KEY).commit()
    }

    private suspend fun ensureLocalIdentity(): LocalIdentity {
        val installationId = installationId()
        val existingUserId = dao.findUserIdForInstallation(installationId)
        if (existingUserId != null) {
            val householdId = dao.findPrimaryHouseholdId(existingUserId)
                ?: error("Local user has no primary household")
            return LocalIdentity(existingUserId, householdId)
        }

        val now = System.currentTimeMillis()
        val userId = UUID.randomUUID().toString()
        val householdId = UUID.randomUUID().toString()
        val legacyProfile = readLegacyProfile()
        val legacyHasProfile = hasLegacyProfile()

        dao.upsertUser(
            AppUserEntity(
                userId = userId,
                onboardingStatus = if (legacyHasProfile) ONBOARDING_COMPLETE else ONBOARDING_NOT_STARTED,
                status = "ACTIVE",
                createdAt = now,
                updatedAt = now
            )
        )
        dao.upsertInstallation(
            AppInstallationEntity(
                installationId = installationId,
                userId = userId,
                createdAt = now,
                lastSeenAt = now
            )
        )
        dao.upsertHousehold(
            HouseholdEntity(
                householdId = householdId,
                adultCount = legacyProfile.adults,
                homePostalCode = legacyProfile.zip,
                preferredRadiusMiles = legacyProfile.radiusMiles.toDouble(),
                createdAt = now,
                updatedAt = now
            )
        )
        dao.upsertHouseholdUser(
            HouseholdUserEntity(
                householdId = householdId,
                userId = userId,
                role = "OWNER",
                isPrimary = true,
                joinedAt = now
            )
        )
        dao.upsertChildren(
            legacyProfile.childAges.mapIndexed { index, age ->
                HouseholdChildEntity(
                    householdChildId = UUID.randomUUID().toString(),
                    householdId = householdId,
                    displayOrder = index,
                    birthDate = null,
                    enteredAgeYears = age,
                    ageAsOfDate = age?.let { currentDate() },
                    ageReconfirmAfter = null,
                    createdAt = now,
                    updatedAt = now
                )
            }
        )

        return LocalIdentity(userId, householdId)
    }

    private fun installationId(): String {
        preferences.getString(INSTALLATION_ID_KEY, null)?.let { return it }
        return UUID.randomUUID().toString().also { generated ->
            preferences.edit().putString(INSTALLATION_ID_KEY, generated).commit()
        }
    }

    private fun hasLegacyProfile(): Boolean =
        preferences.getBoolean("onboarding_complete", false) || preferences.contains("family_size")

    private fun readLegacyProfile(): HouseholdProfile {
        val legacyChildren = preferences.getString("kids", "0")?.toIntOrNull()?.coerceAtLeast(0) ?: 0
        val total = preferences.getString("family_size", "")?.toIntOrNull()
        val adults = if (preferences.contains("adult_count")) {
            preferences.getInt("adult_count", 1)
        } else {
            ((total ?: 1) - legacyChildren).coerceAtLeast(1)
        }
        val storedAges = preferences.getString("child_ages", "").orEmpty()
            .split(",")
            .filter { it.isNotBlank() }
            .map { it.toIntOrNull() }

        return HouseholdProfile(
            adults = adults,
            childAges = List(legacyChildren) { index -> storedAges.getOrNull(index) },
            zip = preferences.getString("zip", "95758") ?: "95758",
            radiusMiles = preferences.getInt("radius", 10)
        )
    }

    private fun com.example.wherewegoing.data.local.HouseholdWithChildren.toProfile() = HouseholdProfile(
        adults = household.adultCount,
        childAges = children.sortedBy { it.displayOrder }.map { it.enteredAgeYears },
        zip = household.homePostalCode ?: "95758",
        radiusMiles = household.preferredRadiusMiles.toInt()
    )

    private fun currentDate(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private data class LocalIdentity(val userId: String, val householdId: String)

    private companion object {
        const val INSTALLATION_ID_KEY = "room_installation_id"
        const val ONBOARDING_COMPLETE = "COMPLETE"
        const val ONBOARDING_NOT_STARTED = "NOT_STARTED"
    }
}
