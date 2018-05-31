package com.aisoftware.flexconnect.ui.main

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.R.id.authCodeEditText
import com.aisoftware.flexconnect.R.id.phoneEditText
import com.aisoftware.flexconnect.R.id.submitButton
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    @Rule
    @JvmField
    var activityActivityTestRule = ActivityTestRule(MainActivity::class.java)

    @Test
    fun testMainViewInitialState() {
        onView(withId(phoneEditText)).check(matches(isDisplayed()))
        onView(withId(authCodeEditText)).check(matches(not(isDisplayed())))
        onView(withId(submitButton)).check(matches(isDisplayed()))
    }
}