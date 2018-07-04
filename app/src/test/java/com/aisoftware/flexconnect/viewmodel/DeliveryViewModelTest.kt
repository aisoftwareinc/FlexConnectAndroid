package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.network.NetworkService
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
class DeliveryViewModelTest {

    @Mock private lateinit var observer: Observer<List<Delivery>>
    @Mock private lateinit var dataRepository: DataRepository
    @Mock private lateinit var application: Application
    @Mock private lateinit var networkService: NetworkService

    private lateinit var liveData: MutableLiveData<List<Delivery>>
    private lateinit var model: DeliveryViewModel

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)

        liveData = MutableLiveData()
        model = DeliveryViewModel(application, dataRepository, networkService)
    }

    @Test
    @Ignore
    fun testPostAfterUpdate() {
        val delivery = getDelivery()
        val deliveryList = ArrayList<Delivery>()
        deliveryList.add(delivery)
        liveData.value = deliveryList
        model.getDeliveries("9999999999", true).observeForever(observer)
        verify(observer).onChanged(listOf(Delivery()))
    }

    private fun getDelivery(): Delivery
            = Delivery(
            1L,
            "guid",
            "Status",
            "5",
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