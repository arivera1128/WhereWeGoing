package com.example.wherewegoing

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.wherewegoing.data.TesterAccountRepository
import com.example.wherewegoing.data.local.WhereWeGoingDatabase
import com.example.wherewegoing.ui.DealSubmissionPage
import com.example.wherewegoing.ui.theme.WhereWeGoingTheme
import org.junit.Rule
import org.junit.Test

class SubmissionFormTest {
 @get:Rule val compose=createComposeRule()
 @Test fun missingPlaceFlowKeepsDescriptionWhenReturningToChoosePlace(){
  val context=InstrumentationRegistry.getInstrumentation().targetContext
  val db=Room.inMemoryDatabaseBuilder(context,WhereWeGoingDatabase::class.java).build()
  try {
   val repo=TesterAccountRepository(context,db,"https://fixture.supabase.co","sb_publishable_fixture")
   compose.setContent { WhereWeGoingTheme { DealSubmissionPage("fixture",repo,{}) } }
   compose.onNodeWithText("Can’t find this place?").performScrollTo().performClick()
   compose.onNodeWithText("Continue").assertIsNotEnabled()
   compose.onNodeWithText("Place name and address").performTextInput("Fixture diner, Elk Grove")
   compose.onNodeWithText("Continue").performScrollTo().performClick()
   compose.onNodeWithText("What’s the deal?").assertIsDisplayed()
   compose.onNodeWithText("Tell us about the deal").performTextInput("Fixture description")
   compose.onNodeWithText("Change place").performScrollTo().performClick()
   compose.onNodeWithText("Place name and address").assertTextContains("Fixture diner, Elk Grove")
   compose.onNodeWithText("Continue").performScrollTo().performClick()
   compose.onNodeWithText("Tell us about the deal").assertTextContains("Fixture description")
   compose.onNodeWithText("Send for review").performScrollTo().assertIsEnabled()
  } finally { db.close() }
 }
}
