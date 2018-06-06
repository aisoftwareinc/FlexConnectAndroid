package com.aisoftware.flexconnect.ui.detail

import android.content.Intent
import android.support.test.filters.SmallTest
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.util.SharedPrefUtil
import junit.framework.Assert.assertNotNull
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
class DeliveryDetailPresenterTest {

    private lateinit var presenter: DeliveryDetailPresenterImpl

    @Mock
    private lateinit var view: DeliveryDetailView

    @Mock
    private lateinit var interactor: DeliveryDetailInteractor

    @Mock
    private lateinit var sharedPrefUtil: SharedPrefUtil

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        presenter = DeliveryDetailPresenterImpl(view, interactor)
        assertNotNull(presenter)
    }

    @Test
    fun testInitialize() {
        val delivery = getDelivery(1)
        presenter.initialize(delivery)
        verify(view).initializeView(delivery, "( 999 ) 999-9999")
    }

    @Test
    fun testInitializeFailure() {
        presenter.initialize(null)
        verify(view).showInitializationErrorDialog()
    }

    @Test
    fun testCheckLocationUpdate() {
        presenter.checkLocationUpdate()
        verify(view).checkLocationUpdate()
    }

    @Test
    fun testStopLocationUpdate() {
        presenter.stopLocationUpdate()
        verify(view).stopLocationUpdate()
    }

    @Test
    fun testDetailDeliveredChecked() {
        presenter.detailDeliveredChecked()
        verify(view).toggleEnRouteCheckbox(false)
        verify(view).stopLocationUpdate()
        verify(view).showCameraDialog()
    }

    @Test
    fun testDetailDirectionsClicked() {
        val delivery = getDelivery(1)
        presenter.initialize(delivery)

        presenter.detailDirectionsClicked()
        verify(view).navigateToMapView(getDelivery(1L))
    }

    @Test
    fun testCameraPermissionPassed() {
        presenter.cameraPermissionPassed()
        verify(view).navigateToCamera()
    }

    @Test
    fun testPermissionFailed() {
        presenter.permissionFailed()
        verify(view).toggleDeliveredCheckbox(false)
        verify(view).toggleEnRouteCheckbox(false)
        verify(view).navigateToSettings()
    }

    @Test
    fun testDeliveryCaptureImageClicked() {
        presenter.deliveryCaptureImageClicked(true)
        verify(view).checkShowCamera()
    }

    @Test
    @Ignore
    fun testDeliveryCaptureImageClickedNoCapture() {
        val phoneNumber = "9999999999"
        `when`(view.getSharedPrefUtil()).thenReturn(sharedPrefUtil)
        `when`(sharedPrefUtil.getUserPref(false)).thenReturn(phoneNumber)

        val delivery = getDelivery(1)
        presenter.initialize(delivery)
        presenter.deliveryCaptureImageClicked(false)
        verify(view).showDeliveredRequestSuccess()
    }

    @Test
    fun testImageRetryClicked() {
        presenter.imageRetryClicked()
        verify(view).navigateToCamera()
    }

    @Test
    fun testImageCancelClicked() {
        presenter.imageCancelClicked()
        verify(view).toggleDeliveredCheckbox(false)
    }

    @Test
    fun testOnResultCancelled() {
        val intent = Mockito.mock(Intent::class.java)
        presenter.onResultCancelled(intent)
        verify(view).toggleDeliveredCheckbox(false)
    }

    @Test
    fun testOnBackPressed() {
        presenter.onBackPressed()
        verify(view).navigateToDashboard()
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