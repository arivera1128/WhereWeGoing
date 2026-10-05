package com.example.wherewegoing

import androidx.room.Room
import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wherewegoing.data.RoomCatalogRepository
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.json.JSONObject
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class CatalogRepositoryTest {
    @Test fun upgradeFromSchemaFourPreservesExistingRestaurant() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val name = "migration-test-${UUID.randomUUID()}.db"
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        val schema = JSONObject(instrumentation.context.assets.open(
            "com.example.wherewegoing.data.local.WhereWeGoingDatabase/4.json"
        ).bufferedReader().use { it.readText() }).getJSONObject("database")
        SQLiteDatabase.openOrCreateDatabase(file, null).use { old ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                old.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                val indices = entity.optJSONArray("indices") ?: org.json.JSONArray()
                for (j in 0 until indices.length()) {
                    old.execSQL(indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) old.execSQL(setup.getString(i))
            val place = foodQuizPlaces.first()
            val id = UUID.nameUUIDFromBytes("where-we-going:restaurant:${place.id}".toByteArray()).toString()
            old.execSQL("INSERT INTO restaurant VALUES (?, ?, ?, 'ACTIVE')",
                arrayOf(id, place.id, "Existing saved place"))
            old.version = 4
        }
        val upgraded = Room.databaseBuilder(context, WhereWeGoingDatabase::class.java, name).build()
        try {
            val catalog = RoomCatalogRepository(upgraded, foodQuizPlaces, elkGroveDeals).load()
            assertEquals(7, upgraded.openHelper.writableDatabase.version)
            assertEquals("Existing saved place", catalog.places.first().name)
            assertEquals(elkGroveDeals.size, catalog.deals.size)
        } finally {
            upgraded.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun catalogReadsSavedValuesAndDoesNotOverwritePublishedOffers() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, WhereWeGoingDatabase::class.java).build()
        try {
            val repository = RoomCatalogRepository(database, foodQuizPlaces, elkGroveDeals)
            val initial = repository.load()
            assertEquals(foodQuizPlaces, initial.places)
            assertEquals(elkGroveDeals, initial.deals)

            val place = foodQuizPlaces.first()
            database.openHelper.writableDatabase.execSQL(
                "UPDATE restaurant SET name = ? WHERE prototype_key = ?",
                arrayOf("Saved catalog name", place.id)
            )
            val changedSeeds = RoomCatalogRepository(database,
                foodQuizPlaces.map { it.copy(name = "Different seed name") },
                elkGroveDeals.map { it.copy(offer = "Different seed offer") })
            val reloaded = changedSeeds.load()
            assertEquals("Saved catalog name", reloaded.places.first { it.id == place.id }.name)
            assertEquals(initial.deals.map { it.offer }, reloaded.deals.map { it.offer })

            // A newer published version replaces the displayed offer and uses its own schedule.
            val deal = elkGroveDeals.first()
            database.openHelper.writableDatabase.execSQL(
                """INSERT INTO deal_version (deal_version_id, deal_id, version_number, offer,
                    terms, status, published_at)
                    SELECT 'test-version-2', deal_id, 2, 'New published offer', 'New terms',
                    'PUBLISHED', 1 FROM deal WHERE prototype_key = ?""",
                arrayOf(deal.id)
            )
            database.openHelper.writableDatabase.execSQL(
                "INSERT INTO deal_schedule (deal_version_id, weekday) VALUES ('test-version-2', 3)"
            )
            val updated = repository.load().deals.first { it.id == deal.id }
            assertEquals("New published offer", updated.offer)
            assertEquals(setOf(3), updated.days)
            database.clearAllTables()
            assertEquals(initial, repository.load())
        } finally {
            database.close()
        }
    }
}
