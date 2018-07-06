package com.aisoftware.flexconnect.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import com.aisoftware.flexconnect.BuildConfig
import com.aisoftware.flexconnect.model.Delivered
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.model.EnRouteState
import com.aisoftware.flexconnect.network.request.DeliveredRequest
import com.aisoftware.flexconnect.network.request.EnRouteRequest
import com.aisoftware.flexconnect.network.request.PendingEnRouteRequest
import com.aisoftware.flexconnect.network.request.ReportLocationRequest
import com.aisoftware.flexconnect.ui.ActivityBaseView
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.Logger
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult

interface DeliveryDetailView : ActivityBaseView {
    fun initializeView(delivery: Delivery, formattedPhone: String, isEnRoute: Boolean)
    fun showInitializationErrorDialog()
    fun checkShowCamera()
    fun checkLocationUpdate()
    fun stopLocationUpdate()
    fun showCameraDialog()
    fun navigateToMapView(delivery: Delivery)
    fun navigateToCamera()
    fun navigateToSettings()
    fun startLocationUpdate(locationUpdateRequest: LocationRequest)
    fun showDeliveredRequestSuccess()
    fun showDeliveredRequestFailure()
    fun showImageUploadConfirmDialog(bitmap: Bitmap)
    fun toggleEnRouteCheckbox(clicked: Boolean)
}

interface DeliveryDetailPresenter {
    fun initialize(delivery: Delivery?)
    fun checkLocationUpdate()
    fun stopLocationUpdate()
    fun detailDeliveredChecked()
    fun detailDirectionsClicked()
    fun cameraPermissionPassed()
    fun locationPermissionPassed()
    fun permissionFailed()
    fun imageDataReceived(data: Intent?)
    fun imageSendClicked()
    fun imageRetryClicked()
    fun imageCancelClicked()
    fun deliveryCaptureImageClicked(captureImage: Boolean)
    fun onResultCancelled(data: Intent?)
    fun onBackPressed()
    fun locationResultReceived(locationResult: LocationResult)
}

class DeliveryDetailPresenterImpl(val view: DeliveryDetailView, private val interactor: DeliveryDetailInteractor) : DeliveryDetailPresenter {

    private val TAG = DeliveryDetailPresenterImpl::class.java.simpleName
    private val DEFAULT_UPDATE_INTERVAL: Long = 60000 // every 60 seconds
    private val DEFAULT_FAST_UPDATE_INTERVAL: Long = 30000 // every 30 seconds
    private val MAX_WAIT_TIME: Long = DEFAULT_UPDATE_INTERVAL * 10 // 10 minutes
    private lateinit var delivery: Delivery
    private var bitmap: Bitmap? = null
    private var isEnRoute: Boolean = false

    override fun initialize(delivery: Delivery?) {
        if (delivery == null) {
            view.showInitializationErrorDialog()
        }
        else {
            this.delivery = delivery
            val displayPhoneNumber = formatPhoneForDisplay(this.delivery.customerPhone) ?: ""
            isEnRoute = delivery.status == EnRouteState.ENROUTE.state
            view.initializeView(delivery, displayPhoneNumber, isEnRoute)
        }
    }

    override fun stopLocationUpdate() {
        if (view.isNetworkAvailable()) {
            val phoneNumber = view.getSharedPrefUtil().getUserPref(false)
            val guid = delivery.guid
            val request = PendingEnRouteRequest(phoneNumber, guid)
            interactor.sendPendingEnRouteUpdate(request, object : EnRouteRequestCallback {
                override fun onEnRouteSuccess(data: String?) {
                    Logger.d(TAG, "Received success enroute response: $data")
                    Logger.d(TAG, "Attempting to stop location update.  Found enroute count of: ${view.getEnRouteCount()}")
                    if (view.getEnRouteCount() == 1) {
                        view.getSharedPrefUtil().setLocationClientRunning(false)
                        view.stopLocationUpdate()
                    }
                }

                override fun onEnRouteFailure(data: String?) {
                    Logger.d(TAG, "Received failure enroute response: $data")
                    view.getSharedPrefUtil().setLocationClientRunning(false)
                    view.stopLocationUpdate()
                }
            })
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun locationPermissionPassed() {
        if (view.isNetworkAvailable()) {
            val phoneNumber = view.getSharedPrefUtil().getUserPref(false)
            val guid = delivery.guid
            interactor.sendEnRouteUpdate(EnRouteRequest(phoneNumber, guid), object : EnRouteRequestCallback {
                override fun onEnRouteSuccess(data: String?) {
                    val count = view.getEnRouteCount()
                    val isRunning = view.getSharedPrefUtil().getLocationClientRunning()

                    Logger.d(TAG, "Attempting to start location update with current enroute count: $count")
                    // Case: no deliveries are set to enroute
                    if ( count == 0) {
                        view.incrementEnRouteCount()
                        view.getSharedPrefUtil().setLocationClientRunning(true)
                        view.startLocationUpdate(getLocationRequest())
                    }
                    // Case: deliveries set to enroute, but no location client running
                    else if( count > 0 && !isRunning ) {
                        view.getSharedPrefUtil().setLocationClientRunning(true)
                        view.startLocationUpdate(getLocationRequest())
                    }
                }

                override fun onEnRouteFailure(data: String?) {
                    Logger.d(TAG, "Received failure enroute response: $data")
                    view.showInitializationErrorDialog()
                }
            })
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun detailDeliveredChecked() {
        view.toggleEnRouteCheckbox(false)
        view.stopLocationUpdate()
        view.showCameraDialog()
    }

    override fun detailDirectionsClicked() {
        view.navigateToMapView(delivery)
    }

    override fun cameraPermissionPassed() {
        view.navigateToCamera()
    }

    override fun checkLocationUpdate() {
        view.checkLocationUpdate()
    }

    override fun permissionFailed() {
        view.toggleEnRouteCheckbox(false)
        view.navigateToSettings()
    }

    override fun locationResultReceived(locationResult: LocationResult) {
        val location = locationResult.locations.first()
        val lat = location.latitude
        val long = location.longitude
        val phoneNumber = view.getSharedPrefUtil().getUserPref(false)

        // Update service
        val reportLocationRequest = ReportLocationRequest(phoneNumber, lat.toString(), long.toString())
        interactor.sendLocationUpdate(reportLocationRequest, object : ReportLocationCallback {
            override fun onReportLocationSuccess(data: String?) {
                Logger.d(TAG, "Success report location update: $data")
            }

            override fun onReportLocationFailure(data: String?) {
                Logger.d(TAG, "Failed report location update: $data")
            }
        })
        interactor.updateLastUpdateTime(System.currentTimeMillis().toString())
    }

    override fun deliveryCaptureImageClicked(captureImage: Boolean) {
        if (captureImage) {
            view.checkShowCamera()
        }
        else {
            bitmap = null
            imageSendClicked()
        }
    }

    override fun imageDataReceived(data: Intent?) {
        Logger.d(TAG, "Attempting to upload image in intent: $data")
        data?.let {
            val extras = data.extras
            bitmap = extras?.get("data") as Bitmap
            view.showImageUploadConfirmDialog(bitmap!!)
        }
    }

    override fun imageSendClicked() {
        if (view.isNetworkAvailable()) {
            var imageString = ""
            if (bitmap != null) {
                imageString = ConverterUtil.convertImage(bitmap!!)
            }

            val request = DeliveredRequest(view.getSharedPrefUtil().getUserPref(false),
                    delivery.guid,
                    delivery.comments,
                    imageString)

            if (BuildConfig.SEND_DELIVERED_UPDATE) {
                interactor.sendDeliveredUpdate(request, object : DeliveryDetailRequestCallback {
                    override fun onDeliveredSuccess(delivered: Delivered?) {
                        Logger.d(TAG, "Delivered request success: $delivered")
                        if (delivered != null) {
                            if (delivered.result != "Success") {
                                onDeliveredFailure(delivered.result)
                            } else {
                                view.showDeliveredRequestSuccess()
                            }
                        }
                    }

                    override fun onDeliveredFailure(data: String?) {
                        Logger.d(TAG, "Delivered request failure: $data")
                        view.showDeliveredRequestFailure()
                    }
                })
            }
            else {
                view.showDeliveredRequestSuccess()
            }
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun imageRetryClicked() {
        bitmap = null
        view.navigateToCamera()
    }

    override fun imageCancelClicked() {
        bitmap = null
    }

    override fun onResultCancelled(data: Intent?) {
        // If null, this is cancel from camera, else map
    }

    override fun onBackPressed() {
        view.navigateToDashboard()
    }

    private fun formatPhoneForDisplay(rawPhone: String): String? {
        var formatted: String? = null

        try {
            if (!rawPhone.isBlank() && rawPhone.length == 10) {
                if (rawPhone.toLongOrNull() != null) {
                    val areaCode = rawPhone.substring(0..2)
                    val pref = rawPhone.subSequence(3..5)
                    val post = rawPhone.subSequence(6..9)
                    formatted = "( $areaCode ) $pref-$post"
                }
            }
        } catch (e: Exception) {
            Logger.e(TAG, "Unable to format phone number", e)
        }
        return formatted
    }

    private fun getLocationRequest(): LocationRequest {
        var interval = if (delivery.interval.isNullOrBlank()) {
            DEFAULT_UPDATE_INTERVAL
        }
        else {
            delivery.interval.toLong() * 60000
        }

        if (BuildConfig.USE_DEFAULT_INTERVAL) {
            interval = DEFAULT_UPDATE_INTERVAL
        }

        Logger.d(TAG, "Computed location update interval: $interval")
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = interval
        locationRequest.fastestInterval = DEFAULT_FAST_UPDATE_INTERVAL
        locationRequest.maxWaitTime = MAX_WAIT_TIME
        return locationRequest
    }
}