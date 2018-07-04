package com.aisoftware.flexconnect.ui.dashboard

import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.model.Delivery
import junit.framework.Assert.assertNotNull
import junit.framework.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
class DashboardPresenterTest {

    private lateinit var presenter: DashboardPresenterImpl
    private val deliveriesList = mutableListOf<Delivery>()

    @Mock
    private lateinit var view: DashboardView

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = DashboardPresenterImpl(view)
        assertNotNull(presenter)

        val delivery1 = getDelivery(1)
        deliveriesList.add(delivery1)

        val delivery2 = getDelivery(2)
        deliveriesList.add(delivery2)
        assertTrue(deliveriesList.size == 2)
    }

    @Test
    fun testInitialize() {
        presenter.initialize(deliveriesList)
        verify(view).initializeDeliveriesView(deliveriesList)
    }

    @Test
    fun testInitializeNoDeliveries() {
        presenter.initialize(null)
        verify(view).initializeNoDeliveriesView()
    }

    @Test
    fun testOnResumeEvent() {
        presenter.onResumeEvent()
        verify(view).checkGoogleApiAvailability()
    }

    @Test
    fun testOnBackPressedEvent() {
        presenter.onBottomNavPhoneClicked()
        verify(view).showLogoutDialog()
    }

    @Test
    fun testOnBottomNavPhoneClicked() {
        presenter.onBottomNavPhoneClicked()
        verify(view).showLogoutDialog()
    }

    private fun getDelivery(id: Long): Delivery
            = Delivery(
            id,
            "guid",
            "5",
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