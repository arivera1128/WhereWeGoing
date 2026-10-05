package com.example.wherewegoing.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "app_user")
data class AppUserEntity(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String,
    @ColumnInfo(name = "onboarding_status")
    val onboardingStatus: String,
    val status: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
)

@Entity(
    tableName = "app_installation",
    foreignKeys = [
        ForeignKey(
            entity = AppUserEntity::class,
            parentColumns = ["user_id"],
            childColumns = ["user_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("user_id")]
)
data class AppInstallationEntity(
    @PrimaryKey
    @ColumnInfo(name = "installation_id")
    val installationId: String,
    @ColumnInfo(name = "user_id")
    val userId: String,
    val platform: String = "ANDROID",
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "last_seen_at")
    val lastSeenAt: Long
)
