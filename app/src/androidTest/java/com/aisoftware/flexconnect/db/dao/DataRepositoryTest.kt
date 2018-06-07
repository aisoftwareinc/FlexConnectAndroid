package com.aisoftware.flexconnect.db.dao

import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.Delivery
import junit.framework.Assert
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class DataRepositoryTest {

    private lateinit var database: AppDatabase
    private lateinit var dataRepository: DataRepository

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(), AppDatabase::class.java).build()
        assertNotNull(database)

        val deliveryList = ArrayList<Delivery>()

        val delivery1 = getDelivery(1L)
        deliveryList.add(delivery1)

        val delivery2 = getDelivery(2L)
        deliveryList.add(delivery2)

        database.deliveryDao().insertAll(deliveryList)
        val count = database.deliveryDao().deliveriesCount()
        Assert.assertTrue("Unexpected database count", count == 2)

        dataRepository = DataRepository.getInstance(database)
        assertNotNull(dataRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testFetchDelivery() {
        val deliveryLiveData = dataRepository.fetchDelivery(1)
        assertNotNull(deliveryLiveData)
    }

    @Test
    fun testFetchAllDeliveries() {
        val deliveryLiveData = dataRepository.fetchAllDeliveries()
        assertNotNull(deliveryLiveData)
    }

    @Test
    @Ignore
    fun testFetchDeliveriesCount() {
        val count = dataRepository.fetchDeliveriesCount()
        assertEquals("Unexpected count", 2, count)
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