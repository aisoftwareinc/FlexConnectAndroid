package com.aisoftware.flexconnect.db.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.model.LastUpdate
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class LastUpdateDaoTest {

    private lateinit var database: AppDatabase
    private val expectedTime = System.currentTimeMillis().toString()

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), AppDatabase::class.java).build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testInsert() {
        val lastUpdate = getLastUpdate(expectedTime)
        database.lastUpdateDao().insert(lastUpdate)
        val count = database.lastUpdateDao().lastUpdateCount()
        assertTrue("Unexpected count", count == 1)
    }

    @Test
    fun testLoadLastUpdate() {
        val lastUpdate = getLastUpdate(expectedTime)
        database.lastUpdateDao().insert(lastUpdate)

        val lastUpdateLiveData = database.lastUpdateDao().loadLastUpdate()
        assertNotNull(lastUpdateLiveData)
    }

    @Test
    fun testDeleteAll() {
        val lastUpdate = getLastUpdate(expectedTime)
        database.lastUpdateDao().insert(lastUpdate)

        database.lastUpdateDao().deleteAll()
        val count = database.lastUpdateDao().lastUpdateCount()
        assertTrue("Unexpected count", count == 0)
    }


    private fun getLastUpdate(time: String): LastUpdate =
            LastUpdate(1, time)

}