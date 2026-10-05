package com.example.wherewegoing.data.local

import androidx.room.*

// Maps shared IDs onto stable local IDs; never replaces keys referenced by personal/history rows.
@Entity(tableName = "catalog_sync_link", primaryKeys = ["kind", "remote_id"],
    indices = [Index(value = ["kind", "local_id"], unique = true)])
data class CatalogSyncLink(
    val kind: String,
    @ColumnInfo(name = "remote_id") val remoteId: String,
    @ColumnInfo(name = "local_id") val localId: String
)
@Entity(tableName = "catalog_sync_state", primaryKeys = ["id"])
data class CatalogSyncState(val id: Int = 1, val source: String, val refreshedAt: Long)

@Entity(tableName = "catalog_offer_location", primaryKeys = ["version_id", "location_id"],
    foreignKeys = [
        ForeignKey(entity=DealVersionEntity::class, parentColumns=["deal_version_id"], childColumns=["version_id"], onDelete=ForeignKey.RESTRICT),
        ForeignKey(entity=LocationEntity::class, parentColumns=["location_id"], childColumns=["location_id"], onDelete=ForeignKey.RESTRICT)
    ], indices=[Index("location_id")])
data class CatalogOfferLocation(
    @ColumnInfo(name="version_id") val versionId: String,
    @ColumnInfo(name="location_id") val locationId: String,
    val forKids: Boolean, val savingsRank: Int, val verified: Boolean,
    val source: String, val checked: String
)
data class SharedOfferRow(val versionId: String, val locationId: String, val offer: String,
    val terms: String, val forKids: Boolean, val savingsRank: Int, val verified: Boolean,
    val source: String, val checked: String)

@Dao
interface CatalogSyncDao {
    @Query("SELECT * FROM catalog_sync_link WHERE kind=:kind AND remote_id=:id")
    suspend fun link(kind: String, id: String): CatalogSyncLink?
    @Query("SELECT remote_id FROM catalog_sync_link WHERE kind=:kind AND local_id=:id") suspend fun remoteId(kind:String,id:String):String?
    @Insert suspend fun insertLink(link: CatalogSyncLink)
    @Upsert suspend fun saveState(state: CatalogSyncState)
    @Query("SELECT * FROM catalog_sync_state WHERE id=1") suspend fun state(): CatalogSyncState?
    @Query("SELECT * FROM location WHERE prototype_key=:key") suspend fun locationByKey(key: String): LocationEntity?
    @Query("SELECT * FROM location WHERE location_id=:id") suspend fun location(id: String): LocationEntity?
    @Query("SELECT * FROM restaurant WHERE restaurant_id=:id") suspend fun restaurant(id: String): RestaurantEntity?
    @Query("SELECT * FROM deal_version WHERE deal_version_id=:id") suspend fun version(id: String): DealVersionEntity?
    @Upsert suspend fun saveRestaurant(value: RestaurantEntity)
    @Upsert suspend fun saveLocation(value: LocationEntity)
    @Upsert suspend fun saveMetadata(value: CatalogMetadataEntity)
    @Query("SELECT * FROM catalog_metadata WHERE prototype_key=:key") suspend fun metadata(key: String): CatalogMetadataEntity?
    @Query("UPDATE restaurant SET status='INACTIVE'") suspend fun deactivateRestaurants()
    @Query("UPDATE location SET status='INACTIVE'") suspend fun deactivateLocations()
    @Query("UPDATE deal SET status='INACTIVE'") suspend fun deactivateDeals()
    @Query("UPDATE deal_version SET status='RETIRED' WHERE status='PUBLISHED'") suspend fun retireVersions()
    @Query("UPDATE deal_version SET status='PUBLISHED' WHERE deal_version_id=:id") suspend fun activateVersion(id: String)
    @Query("UPDATE deal SET status='ACTIVE' WHERE deal_id=:id") suspend fun activateDeal(id: String)
    @Query("DELETE FROM catalog_offer_location") suspend fun clearOfferLocations()
    @Query("SELECT * FROM deal WHERE deal_id=:id") suspend fun deal(id: String): DealEntity?
    @Upsert suspend fun saveOfferLocation(value: CatalogOfferLocation)
    @Query("SELECT * FROM restaurant WHERE status='ACTIVE' ORDER BY prototype_key") suspend fun activeRestaurants(): List<RestaurantEntity>
    @Query("SELECT * FROM location WHERE status='ACTIVE' ORDER BY location_id") suspend fun activeLocations(): List<LocationEntity>
    @Query("""SELECT v.deal_version_id AS versionId, a.location_id AS locationId, v.offer, v.terms,
        a.forKids, a.savingsRank, a.verified, a.source, a.checked
        FROM catalog_offer_location a JOIN deal_version v ON v.deal_version_id=a.version_id
        JOIN deal d ON d.deal_id=v.deal_id WHERE v.status='PUBLISHED' AND d.status='ACTIVE'""")
    suspend fun offers(): List<SharedOfferRow>
}
