package com.aisoftware.flexconnect.db.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.model.Delivery
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DeliveryDaoTest {

    private lateinit var database: AppDatabase

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
        val expected = getDelivery(1L)
        val id = expected.id?.toInt() ?: 1
        database.deliveryDao().insert(expected)
        val count = database.deliveryDao().deliveriesCount()
        assertTrue("Unexpected database count", count == 1)
    }

    @Test
    fun testInsertAll() {
        val deliveryList = ArrayList<Delivery>()

        val delivery1 = getDelivery(1L)
        deliveryList.add(delivery1)

        val delivery2 = getDelivery(2L)
        deliveryList.add(delivery2)

        database.deliveryDao().insertAll(deliveryList)
        val count = database.deliveryDao().deliveriesCount()
        assertTrue("Unexpected database count", count == 2)
    }

    @Test
    fun testLoadDelivery() {
        val expected = getDelivery(1L)
        val id = expected.id?.toInt() ?: 1
        database.deliveryDao().insert(expected)
        val actual = database.deliveryDao().loadDelivery(id)
        assertNotNull(actual)
    }

    @Test
    fun testLoadAllDeliveries() {
        val deliveryList = ArrayList<Delivery>()

        val delivery1 = getDelivery(1L)
        deliveryList.add(delivery1)

        val delivery2 = getDelivery(2L)
        deliveryList.add(delivery2)

        database.deliveryDao().insertAll(deliveryList)
        val count = database.deliveryDao().deliveriesCount()
        assertTrue("Unexpected database count", count == 2)

        val liveDataList = database.deliveryDao().loadAllDeliveries()
        assertNotNull(liveDataList)
    }

    private fun getDelivery(id: Long): Delivery
            = Delivery(
            id,
            "guid",
            "Status",
            "May 1 1985",
            "12:00 pm",
            "FlexConnect",
            "fc@email.com",
            "9999999999",
            "123 Main St",
            "Ste 1",
            "Phoenix",
            "AZ",
            "85086",
            "1",
            "2",
            "30 minutes",
            "12 miles",
            "These dudes are for real.")
}