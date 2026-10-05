package com.example.wherewegoing.data.local

import androidx.room.*

// MVP display and scoring inputs; offer wording stays in immutable deal_version.
@Entity(tableName = "catalog_metadata", primaryKeys = ["prototype_key"], foreignKeys = [
    ForeignKey(entity = RestaurantEntity::class, parentColumns = ["prototype_key"],
        childColumns = ["prototype_key"], onDelete = ForeignKey.RESTRICT)
])
data class CatalogMetadataEntity(
    @ColumnInfo(name = "prototype_key") val key: String,
    val category: String,
    val description: String,
    val forKids: Boolean,
    val savingsRank: Int,
    val verified: Boolean,
    val source: String,
    val checked: String,
    val displayOrder: Int,
    val dealDisplayOrder: Int
)

@Entity(tableName = "catalog_trait", primaryKeys = ["prototype_key", "trait"], foreignKeys = [
    ForeignKey(entity = RestaurantEntity::class, parentColumns = ["prototype_key"],
        childColumns = ["prototype_key"], onDelete = ForeignKey.RESTRICT)
])
data class CatalogTraitEntity(@ColumnInfo(name = "prototype_key") val key: String, val trait: String)

@Entity(tableName = "deal_schedule", primaryKeys = ["deal_version_id", "weekday"], foreignKeys = [
    ForeignKey(entity = DealVersionEntity::class, parentColumns = ["deal_version_id"],
        childColumns = ["deal_version_id"], onDelete = ForeignKey.RESTRICT)
])
data class DealScheduleEntity(@ColumnInfo(name = "deal_version_id") val versionId: String, val weekday: Int)

data class CatalogDealRow(
    val key: String, val name: String, val category: String, val offer: String,
    val forKids: Boolean, val savingsRank: Int, val verified: Boolean,
    val address: String, val terms: String, val source: String, val checked: String,
    val versionId: String
)
data class CatalogPlaceRow(val key: String, val name: String, val description: String)

@Dao
interface CatalogDao {
    @Query("""SELECT r.prototype_key AS `key`,r.name,m.category,v.offer,m.forKids,m.savingsRank,m.verified,
        l.address,v.terms,m.source,m.checked,v.deal_version_id AS versionId
        FROM restaurant r JOIN catalog_metadata m ON m.prototype_key=r.prototype_key
        JOIN location l ON l.restaurant_id=r.restaurant_id
        JOIN deal d ON d.restaurant_id=r.restaurant_id JOIN deal_version v ON v.deal_id=d.deal_id
        ORDER BY v.published_at DESC, v.version_number DESC""")
    suspend fun archivedDeals(): List<CatalogDealRow>
    @Query("SELECT COUNT(*) FROM catalog_metadata") suspend fun metadataCount(): Int
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertMetadata(rows: List<CatalogMetadataEntity>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertTraits(rows: List<CatalogTraitEntity>)
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insertSchedules(rows: List<DealScheduleEntity>)
    @Query("SELECT * FROM catalog_trait") suspend fun traits(): List<CatalogTraitEntity>
    @Query("SELECT * FROM deal_schedule") suspend fun schedules(): List<DealScheduleEntity>
    @Query("""
        SELECT r.prototype_key AS `key`, r.name, m.description
        FROM restaurant r JOIN catalog_metadata m ON m.prototype_key = r.prototype_key
        WHERE r.status = 'ACTIVE' ORDER BY m.displayOrder
    """) suspend fun places(): List<CatalogPlaceRow>
    @Query("""
        SELECT r.prototype_key AS `key`, r.name, m.category, v.offer, m.forKids,
               m.savingsRank, m.verified, l.address, v.terms, m.source, m.checked,
               v.deal_version_id AS versionId
        FROM restaurant r
        JOIN catalog_metadata m ON m.prototype_key = r.prototype_key
        JOIN location l ON l.restaurant_id = r.restaurant_id
        JOIN deal d ON d.restaurant_id = r.restaurant_id
        JOIN deal_version v ON v.deal_id = d.deal_id
        WHERE r.status = 'ACTIVE' AND l.status = 'ACTIVE' AND d.status = 'ACTIVE'
          AND v.status = 'PUBLISHED'
          AND v.version_number = (SELECT MAX(v2.version_number) FROM deal_version v2
              WHERE v2.deal_id = d.deal_id AND v2.status = 'PUBLISHED')
        ORDER BY m.dealDisplayOrder
    """) suspend fun deals(): List<CatalogDealRow>
}
