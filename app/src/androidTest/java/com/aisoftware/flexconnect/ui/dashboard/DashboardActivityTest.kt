package com.aisoftware.flexconnect.ui.dashboard

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.filters.LargeTest
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.ui.DashboardActivity
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DashboardActivityTest {

    @Rule
    @JvmField
    var activityActivityTestRule = ActivityTestRule(DashboardActivity::class.java)

    @Test
    @Ignore
    fun testInitialViewState() {
        onView(withId(R.id.dashboardRecyclerView)).check(matches(isDisplayed()))
    }
}