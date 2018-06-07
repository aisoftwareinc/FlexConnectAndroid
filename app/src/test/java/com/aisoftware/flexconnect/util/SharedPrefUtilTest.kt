package com.aisoftware.flexconnect.util

import android.content.Context
import android.content.SharedPreferences
import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.R
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations

@SmallTest
class SharedPrefUtilTest {

    private val SHARED_PREF_FILE = "com.aisoftware.flexconnect.PREFERENCE_FILE"
    @Mock
    private lateinit var sharedPrefUtil: SharedPrefUtil

    @Mock
    private lateinit var sharedPreferences: SharedPreferences

    @Mock
    private lateinit var context: Context

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        sharedPrefUtil = SharedPrefUtilImpl(context)
        assertNotNull(sharedPrefUtil)

        `when`(context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)).thenReturn(sharedPreferences)
        `when`(context.getString(R.string.phone_shared_pref_key)).thenReturn("")
    }

    @Test
    fun testUserPrefExists() {
        `when`(sharedPreferences.getString(context.getString(R.string.phone_shared_pref_key), "")).thenReturn("phoneNum")
        val exists = sharedPrefUtil.userPrefExists()
        assertTrue(exists)
    }

    @Test
    fun testUserPrefExistsNoPreference() {
        `when`(sharedPreferences.getString(context.getString(R.string.phone_shared_pref_key), "")).thenReturn("")
        val exists = sharedPrefUtil.userPrefExists()
        assertFalse(exists)
    }

    @Test
    fun testGetuserPref() {
        val expected = "phoneNumber"
        `when`(sharedPreferences.getString(context.getString(R.string.phone_shared_pref_key), "")).thenReturn(expected)
        val actual = sharedPrefUtil.getUserPref(false)
        assertNotNull(actual)
        assertEquals("Unexpected phone number value", expected, actual)
    }

    @Test
    fun testGetIntervalPref() {
        val expected = "intervalPref"
        `when`(sharedPreferences.getString(context.getString(R.string.interval_pref_key), "")).thenReturn(expected)
        val actual = sharedPrefUtil.getIntervalPref(false)
        assertNotNull(actual)
        assertEquals("Unexpected interval value", expected, actual)
    }
}