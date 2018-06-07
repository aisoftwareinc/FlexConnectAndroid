package com.aisoftware.flexconnect.ui.main

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.R.id.authCodeEditText
import com.aisoftware.flexconnect.R.id.phoneEditText
import com.aisoftware.flexconnect.R.id.submitButton
import okhttp3.mockwebserver.MockWebServer
import org.hamcrest.Matchers.not
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    private lateinit var server: MockWebServer

    @Rule
    @JvmField
    var activityActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Before
    fun setUp() {
        server = MockWebServer()
    }

    @Test
    fun testInitialViewState() {
        onView(withId(phoneEditText)).check(matches(isDisplayed()))
        onView(withId(authCodeEditText)).check(matches(not(isDisplayed())))
        onView(withId(submitButton)).check(matches(isDisplayed()))
    }

    @Test
    fun testNoEntryAuthErrorDialog() {
        onView(withId(submitButton)).perform(click())
        onView(withText("Authentication Error")).check(matches(isDisplayed()))
    }

//    @Test
//    fun testSubmit() {
//        val responseBody = "{\"authCode\": \"762967\"}"
//        server.enqueue(
//                MockResponse()
//                        .setBody(responseBody)
//                        .setResponseCode(200)
//        )
//        server.start()
//
//        val baseUrl = server.url("/")
//
//        onView(withId(phoneEditText)).check(matches(isDisplayed()))
//        onView(withId(phoneEditText)).perform(typeText("2084900557"), click())
//        onView(withId(authCodeEditText)).check(matches(isDisplayed()))
//    }
}