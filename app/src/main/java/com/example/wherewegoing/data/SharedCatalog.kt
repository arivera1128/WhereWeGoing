package com.example.wherewegoing.data

import androidx.room.withTransaction
import com.example.wherewegoing.data.local.*
import com.example.wherewegoing.model.PlaceDeal
import java.util.UUID
import java.util.TimeZone
import java.util.Calendar

data class SharedRestaurant(val id: String, val key: String, val name: String, val active: Boolean)
data class SharedLocation(val id: String, val restaurantId: String, val legacyKey: String?,
    val address: String, val timeZone: String, val active: Boolean)
data class SharedOffer(val id: String, val dealId: String, val restaurantId: String, val locationId: String,
    val version: Int, val publishedAt: Long, val offer: String, val terms: String, val days: Set<Int>,
    val forKids: Boolean, val savingsRank: Int, val verified: Boolean, val source: String, val checked: String)
data class SharedCatalog(val restaurants: List<SharedRestaurant>, val locations: List<SharedLocation>, val offers: List<SharedOffer>) {
    fun validate() {
        fun uuid(id: String) { require(UUID.fromString(id).toString() == id.lowercase()) { "Invalid catalog identity" } }
        require(restaurants.map { it.id }.distinct().size == restaurants.size &&
            restaurants.map { it.key }.distinct().size == restaurants.size &&
            locations.map { it.id }.distinct().size == locations.size &&
            offers.map { it.id to it.locationId }.distinct().size == offers.size) { "Duplicate catalog identity" }
        val parents = restaurants.associateBy { it.id }
        val outlets = locations.associateBy { it.id }
        restaurants.forEach { uuid(it.id); require(it.key.isNotBlank() && it.name.isNotBlank()) }
        locations.forEach {
            uuid(it.id); require(it.restaurantId in parents && it.address.isNotBlank() &&
                it.timeZone in TimeZone.getAvailableIDs()) { "Invalid location" }
        }
        offers.forEach {
            uuid(it.id); uuid(it.dealId)
            require(outlets[it.locationId]?.restaurantId == it.restaurantId && it.restaurantId in parents)
            require(it.version > 0 && it.publishedAt > 0 && it.savingsRank in 0..2 && it.days.all { day -> day in 1..7 })
            require(it.offer.isNotBlank()) { "Offer wording is required" }
        }
        offers.groupBy { it.dealId }.values.forEach { rows ->
            require(rows.map { it.id }.distinct().size == 1 && rows.map { it.restaurantId }.distinct().size == 1) { "Conflicting current deal versions" }
        }
        offers.groupBy { it.id }.values.forEach { rows ->
            val first=rows.first()
            require(rows.all { it.copy(locationId=first.locationId, verified=first.verified, source=first.source, checked=first.checked) == first }) { "Conflicting version data" }
        }
    }
}

interface SharedCatalogSource {
    val identity: String
    suspend fun fetch(): SharedCatalog
}

/** Network fetch completes before the transaction; any import conflict rolls back the entire refresh. */
class SharedCatalogRepository(private val database: WhereWeGoingDatabase, private val source: SharedCatalogSource) {
    suspend fun refresh() {
        val snapshot = source.fetch()
        snapshot.validate()
        database.withTransaction {
            val sync=database.catalogSyncDao()
            require(sync.state()?.source.let { it == null || it == source.identity }) { "Catalog project changed; explicit identity migration is required" }
            val preference=database.preferenceDao()
            val localRestaurants=mutableMapOf<String,String>()
            val localLocations=mutableMapOf<String,String>()
            sync.deactivateRestaurants(); sync.deactivateLocations(); sync.deactivateDeals(); sync.retireVersions(); sync.clearOfferLocations()
            snapshot.restaurants.forEachIndexed { index, row ->
                val link=sync.link("RESTAURANT",row.id)
                val existing=link?.let { sync.restaurant(it.localId) } ?: preference.findRestaurant(row.key)
                require(existing == null || existing.prototypeKey == row.key) { "Restaurant identity changed" }
                val id=existing?.restaurantId ?: row.id
                if (link == null) sync.insertLink(CatalogSyncLink("RESTAURANT",row.id,id))
                sync.saveRestaurant(RestaurantEntity(id,row.key,row.name,if(row.active) "ACTIVE" else "INACTIVE"))
                localRestaurants[row.id]=id
                val metadata=sync.metadata(row.key) ?: CatalogMetadataEntity(row.key,"Place to eat","",false,0,false,"","",index,index)
                sync.saveMetadata(metadata)
            }
            snapshot.locations.forEach { row ->
                val parent=localRestaurants.getValue(row.restaurantId)
                val link=sync.link("LOCATION",row.id)
                val existing=link?.let { sync.location(it.localId) } ?: row.legacyKey?.let { sync.locationByKey(it) }
                require(existing == null || existing.restaurantId == parent) { "Location identity changed" }
                val id=existing?.locationId ?: row.id
                if (link == null) sync.insertLink(CatalogSyncLink("LOCATION",row.id,id))
                sync.saveLocation(LocationEntity(id,parent,existing?.prototypeKey ?: "shared:${row.id}",row.address,row.timeZone,if(row.active) "ACTIVE" else "INACTIVE"))
                localLocations[row.id]=id
            }
            snapshot.offers.forEach { row ->
                val deal=DealEntity(row.dealId,localRestaurants.getValue(row.restaurantId),"shared:${row.dealId}")
                require(sync.deal(row.dealId)?.restaurantId.let { it == null || it == deal.restaurantId }) { "Deal parent changed" }
                preference.insertDeals(listOf(deal))
                val version=DealVersionEntity(row.id,row.dealId,row.version,row.offer,row.terms,"PUBLISHED",row.publishedAt)
                val existing=sync.version(row.id)
                require(existing == null || existing.copy(status="PUBLISHED") == version) { "Published offer changed without a new version" }
                preference.insertDealVersions(listOf(version))
                val existingDays=database.catalogDao().schedules().filter { it.versionId==row.id }.map { it.weekday }.toSet()
                require(existing == null || existingDays == row.days) { "Published schedule changed without a new version" }
                database.catalogDao().insertSchedules(row.days.map { DealScheduleEntity(row.id,it) })
                sync.activateDeal(row.dealId); sync.activateVersion(row.id)
                sync.saveOfferLocation(CatalogOfferLocation(row.id,localLocations.getValue(row.locationId),row.forKids,row.savingsRank,row.verified,row.source,row.checked))
            }
            sync.saveState(CatalogSyncState(source=source.identity,refreshedAt=System.currentTimeMillis()))
        }
    }
}

suspend fun sharedCandidates(database: WhereWeGoingDatabase): List<PlaceDeal> {
    val sync=database.catalogSyncDao()
    val locations=sync.activeLocations()
    val offers=sync.offers()
    val schedules=database.catalogDao().schedules().groupBy { it.versionId }
    val today=Calendar.getInstance(TimeZone.getTimeZone("America/Los_Angeles")).get(Calendar.DAY_OF_WEEK)
    return sync.activeRestaurants().mapNotNull { restaurant ->
        val outlets=locations.filter { it.restaurantId==restaurant.restaurantId }
        val usable=offers.filter { offer -> outlets.any { it.locationId==offer.locationId } }
        val selected=usable.sortedWith(compareByDescending<SharedOfferRow> {
            if(schedules[it.versionId].orEmpty().any { day -> day.weekday==today }) it.savingsRank * (if(it.verified) 1.0 else 0.4) else 0.0
        }.thenBy { it.versionId }).firstOrNull()
        val outlet=outlets.find { it.locationId==selected?.locationId } ?: outlets.firstOrNull() ?: return@mapNotNull null
        val metadata=sync.metadata(restaurant.prototypeKey)!!
        PlaceDeal(restaurant.prototypeKey,restaurant.name,metadata.category,selected?.offer ?: "Restaurant pick",
            selected?.let { schedules[it.versionId].orEmpty().map { day -> day.weekday }.toSet() }.orEmpty(),
            selected?.forKids ?: false,selected?.savingsRank ?: 0,selected?.verified ?: false,
            outlet.address,selected?.terms ?: "No current published offer.",selected?.source.orEmpty(),selected?.checked.orEmpty(),
            restaurant.restaurantId,outlet.locationId,selected?.versionId)
    }
}
