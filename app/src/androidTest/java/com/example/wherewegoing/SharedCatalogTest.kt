package com.example.wherewegoing

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wherewegoing.data.*
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.Assume.assumeTrue
import java.util.UUID

class SharedCatalogTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    @Test fun refreshPreservesPersonalDataAndHistoricalPlanAndRollsBackConflicts() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,WhereWeGoingDatabase::class.java).build()
        val prefs=context.getSharedPreferences("catalog-test-${UUID.randomUUID()}",0)
        try {
            val catalog=RoomCatalogRepository(db,foodQuizPlaces,elkGroveDeals)
            catalog.load()
            val household=RoomHouseholdProfileRepository(db,prefs)
            val person=household.load()
            val preferences=RoomPreferenceRepository(db,prefs)
            preferences.saveRating(person.userId,"chevys",4)
            preferences.setExcluded(person.userId,"dennys",true)
            val journey=RoomDinnerJourneyRepository(db)
            journey.selectPlan(person.userId,person.householdId,"chevys",true,true)
            val original=journey.load(person.userId).pendingPlace!!
            val localId=db.preferenceDao().findRestaurant("chevys")!!.restaurantId
            val remote="10000000-0000-4000-8000-000000000001"
            var snapshot=SharedCatalog(listOf(SharedRestaurant(remote,"chevys","Chevy's",true)),
                listOf(SharedLocation("20000000-0000-4000-8000-000000000001",remote,"chevys","7401 Laguna Blvd","America/Los_Angeles",true)),emptyList())
            val source=object:SharedCatalogSource { override val identity="fixture"; override suspend fun fetch()=snapshot }
            val sync=SharedCatalogRepository(db,source)
            sync.refresh();sync.refresh()
            assertEquals(localId,db.preferenceDao().findRestaurant("chevys")!!.restaurantId)
            assertEquals(person,household.load())
            val saved=preferences.load(person.userId,foodQuizPlaces)
            assertEquals(4,saved.ratings["chevys"]);assertTrue("dennys" in saved.removedPlaceKeys)
            assertEquals(original.copy(address="7401 Laguna Blvd"),journey.load(person.userId).pendingPlace)
            assertEquals(1,catalog.load().deals.size)
            assertNull(catalog.load().deals.single().dealVersionId)
            val failed=object:SharedCatalogSource { override val identity="fixture"; override suspend fun fetch():SharedCatalog=error("Network unavailable") }
            try { SharedCatalogRepository(db,failed).refresh();fail("Expected failure") } catch (_: IllegalStateException) { }
            assertEquals(1,catalog.load().deals.size)
            val valid=snapshot
            snapshot=snapshot.copy(restaurants=listOf(snapshot.restaurants.single().copy(key="changed-key")))
            try { sync.refresh();fail("Expected identity rejection") } catch (_: IllegalArgumentException) { }
            assertEquals(1,catalog.load().deals.size)
            snapshot=valid.copy(locations=emptyList())
            sync.refresh()
            assertTrue(catalog.load().deals.isEmpty())
            assertEquals(original.copy(address="7401 Laguna Blvd"),journey.load(person.userId).pendingPlace)
        } finally { db.close();prefs.edit().clear().commit() }
    }

    @Test fun publishedSharedOfferKeepsExactVersionWhenWithdrawn() = runBlocking {
        val db=Room.inMemoryDatabaseBuilder(context,WhereWeGoingDatabase::class.java).build()
        val prefs=context.getSharedPreferences("offer-test-"+UUID.randomUUID(),0)
        try {
            val catalog=RoomCatalogRepository(db,foodQuizPlaces,elkGroveDeals);catalog.load()
            val household=RoomHouseholdProfileRepository(db,prefs).load()
            val restaurant="10000000-0000-4000-8000-000000000001"
            val location="20000000-0000-4000-8000-000000000001"
            val offer=SharedOffer("40000000-0000-4000-8000-000000000009","30000000-0000-4000-8000-000000000009",restaurant,location,1,1L,"TEST ONLY — development fixture","Fixture terms",setOf(2,4),false,1,false,"Fixture source","2026-10-04")
            var snapshot=SharedCatalog(listOf(SharedRestaurant(restaurant,"chevys","Chevy's",true)),listOf(SharedLocation(location,restaurant,"chevys","Fixture address","America/Los_Angeles",true)),listOf(offer))
            val source=object:SharedCatalogSource{override val identity="fixture";override suspend fun fetch()=snapshot}
            val sync=SharedCatalogRepository(db,source);sync.refresh()
            val place=catalog.load().deals.single();assertEquals(offer.id,place.dealVersionId);assertEquals(setOf(2,4),place.days);assertFalse(place.verified)
            val journey=RoomDinnerJourneyRepository(db)
            journey.selectPlan(household.userId,household.householdId,place.id,true,true,place)
            snapshot=snapshot.copy(offers=emptyList());sync.refresh()
            assertNull(catalog.load().deals.single().dealVersionId)
            assertEquals(offer.offer,journey.load(household.userId).pendingPlace!!.offer)
            assertEquals(offer.id,journey.load(household.userId).pendingPlace!!.dealVersionId)
        } finally {db.close();prefs.edit().clear().commit()}
    }

    @Test fun hostedDevelopmentCatalogAcceptsReviewedOffers() = runBlocking {
        assumeTrue(BuildConfig.PROOF_SUPABASE_URL.isNotBlank())
        val source=SupabaseCatalogSource(BuildConfig.PROOF_SUPABASE_URL,BuildConfig.PROOF_SUPABASE_KEY)
        val snapshot=source.fetch()
        assertTrue(snapshot.restaurants.isNotEmpty());assertTrue(snapshot.locations.isNotEmpty())
        snapshot.validate()
        val db=Room.inMemoryDatabaseBuilder(context,WhereWeGoingDatabase::class.java).build()
        try {
            val catalog=RoomCatalogRepository(db,foodQuizPlaces,elkGroveDeals)
            catalog.load()
            val stable=object:SharedCatalogSource{override val identity=source.identity;override suspend fun fetch()=snapshot}
            SharedCatalogRepository(db,stable).refresh()
            assertEquals(snapshot.locations.count { it.active && snapshot.restaurants.any { r -> r.id==it.restaurantId && r.active } },catalog.load().deals.size)
        } finally { db.close() }
    }
}

