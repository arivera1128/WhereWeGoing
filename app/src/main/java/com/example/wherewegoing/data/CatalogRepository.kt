package com.example.wherewegoing.data

import androidx.room.withTransaction
import com.example.wherewegoing.data.local.*
import com.example.wherewegoing.model.PlaceDeal
import com.example.wherewegoing.model.QuizPlace
import java.nio.charset.StandardCharsets
import java.util.UUID

data class CatalogSnapshot(val places: List<QuizPlace>, val deals: List<PlaceDeal>, val archivedDeals: List<PlaceDeal> = deals)
interface CatalogRepository { suspend fun load(): CatalogSnapshot }

class RoomCatalogRepository(
    private val database: WhereWeGoingDatabase,
    private val seedPlaces: List<QuizPlace>,
    private val seedDeals: List<PlaceDeal>
) : CatalogRepository {
    override suspend fun load(): CatalogSnapshot = database.withTransaction {
        val catalog = database.catalogDao()
        if (catalog.metadataCount() == 0) {
            val dao = database.preferenceDao()
            val places = seedPlaces
            val deals = seedDeals
            dao.insertRestaurants(
                places.map { place ->
                    RestaurantEntity(
                        restaurantId = restaurantId(place.id),
                        prototypeKey = place.id,
                        name = place.name
                    )
                }
            )
            val publishedAt = System.currentTimeMillis()
            dao.insertLocations(deals.map { deal ->
                LocationEntity(
                    locationId = stableId("location", deal.id),
                    restaurantId = restaurantId(deal.id),
                    prototypeKey = deal.id,
                    address = deal.address,
                    timeZone = "America/Los_Angeles"
                )
            })
            dao.insertDeals(deals.map { deal ->
                DealEntity(
                    dealId = stableId("deal", deal.id),
                    restaurantId = restaurantId(deal.id),
                    prototypeKey = deal.id
                )
            })
            dao.insertDealVersions(deals.map { deal ->
                DealVersionEntity(
                    dealVersionId = stableId("deal-version", "${deal.id}:1"),
                    dealId = stableId("deal", deal.id),
                    versionNumber = 1,
                    offer = deal.offer,
                    terms = deal.terms,
                    status = "PUBLISHED",
                    publishedAt = publishedAt
                )
            })

            catalog.insertMetadata(places.mapIndexed { index, place ->
                val deal = deals.find { it.id == place.id }
                CatalogMetadataEntity(place.id, deal?.category.orEmpty(), place.description,
                    deal?.forKids ?: false, deal?.savingsRank ?: 0, deal?.verified ?: false,
                    deal?.source.orEmpty(), deal?.checked.orEmpty(), index,
                    deals.indexOfFirst { it.id == place.id })
            })
            catalog.insertTraits(places.flatMap { place -> place.traits.map { CatalogTraitEntity(place.id, it) } })
            catalog.insertSchedules(deals.flatMap { deal -> deal.days.map {
                DealScheduleEntity(stableId("deal-version", "${deal.id}:1"), it)
            } })
        }
        val traits = catalog.traits().groupBy { it.key }
        val days = catalog.schedules().groupBy { it.versionId }
        val shared=database.catalogSyncDao().state()!=null
        val oldDeals=catalog.archivedDeals().map { PlaceDeal(it.key, it.name, it.category, it.offer,
                days[it.versionId].orEmpty().map { row -> row.weekday }.toSet(), it.forKids,
                it.savingsRank, it.verified, it.address, it.terms, it.source, it.checked) }
        val currentDeals=if(shared) sharedCandidates(database) else catalog.deals().map { PlaceDeal(it.key, it.name, it.category, it.offer,
            days[it.versionId].orEmpty().map { row -> row.weekday }.toSet(),it.forKids,it.savingsRank,it.verified,it.address,it.terms,it.source,it.checked) }
        CatalogSnapshot(
            catalog.places().map { QuizPlace(it.key, it.name, it.description, traits[it.key].orEmpty().map { row -> row.trait }.toSet()) },
            currentDeals,
            if(shared) (currentDeals+oldDeals).distinctBy { it.id } else currentDeals
        )
    }
    private fun restaurantId(key: String) = stableId("restaurant", key)
    private fun stableId(type: String, key: String): String = UUID.nameUUIDFromBytes(
        "where-we-going:$type:$key".toByteArray(StandardCharsets.UTF_8)
    ).toString()
}
