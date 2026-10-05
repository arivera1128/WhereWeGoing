package com.example.wherewegoing

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wherewegoing.data.RoomHouseholdProfileRepository
import com.example.wherewegoing.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.util.UUID

class TesterIdentityTest {
 @Test fun identityLinkDoesNotReplaceTheLocalProfileAndResetRemovesIt()=runBlocking {
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val db=Room.inMemoryDatabaseBuilder(context,WhereWeGoingDatabase::class.java).build()
  val prefs=context.getSharedPreferences("identity-test-${UUID.randomUUID()}",0)
  try {
   val household=RoomHouseholdProfileRepository(db,prefs)
   val original=household.load()
   val link=UserAuthIdentity(original.userId,"https://fixture.supabase.co",UUID.randomUUID().toString())
   db.userAuthIdentityDao().insert(link)
   assertEquals(link,db.userAuthIdentityDao().find(original.userId,link.project))
   assertEquals(original,household.load())
   assertNull(db.userAuthIdentityDao().find(original.userId,"https://other.supabase.co"))
   household.reset()
   assertNull(db.userAuthIdentityDao().find(original.userId,link.project))
   assertNotEquals(original.userId,household.load().userId)
  } finally { db.close();prefs.edit().clear().commit() }
 }
}
