package com.aisoftware.flexconnect.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import com.aisoftware.flexconnect.BuildConfig
import com.aisoftware.flexconnect.model.Delivered
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.network.request.DeliveredRequest
import com.aisoftware.flexconnect.network.request.EnRouteRequest
import com.aisoftware.flexconnect.ui.ActivityBaseView
import com.aisoftware.flexconnect.util.ConverterUtil
import com.google.android.gms.location.LocationRequest

interface DeliveryDetailView : ActivityBaseView {
    fun initializeView(delivery: Delivery, formattedPhone: String)
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
    fun toggleDeliveredCheckbox(clicked: Boolean)
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
}

class DeliveryDetailPresenterImpl(val view: DeliveryDetailView, val interactor: DeliveryDetailInteractor) : DeliveryDetailPresenter {

    private val TAG = DeliveryDetailPresenterImpl::class.java.simpleName
    private val DEFAULT_UPDATE_INTERVAL: Long = 60000 // every 60 seconds
    private val DEFAULT_FAST_UPDATE_INTERVAL: Long = 30000 // every 30 seconds
    private val MAX_WAIT_TIME: Long = DEFAULT_UPDATE_INTERVAL * 10 // 10 minutes
    private lateinit var delivery: Delivery
    private var bitmap: Bitmap? = null

    override fun initialize(delivery: Delivery?) {
        if (delivery == null) {
            view.showInitializationErrorDialog()
        } else {
            this.delivery = delivery
            val displayPhoneNumber = formatPhoneForDisplay(this.delivery.customerPhone) ?: ""
            view.initializeView(delivery, displayPhoneNumber)
        }
    }

    override fun checkLocationUpdate() {
        Log.d(TAG, "Requesting location update permission check")
        view.checkLocationUpdate()
    }

    override fun stopLocationUpdate() {
        view.stopLocationUpdate()
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

    override fun locationPermissionPassed() {
        Log.d(TAG, "Location permission passed.")
        view.startLocationUpdate(getLocationRequest())

        if( view.isNetworkAvailable() ) {
            val phoneNumber = view.getSharedPrefUtil().getUserPref(false)
            val guid = delivery.guid
            val request = EnRouteRequest(phoneNumber, guid)
            interactor.sendEnRouteUpdate(request, object : EnRouteRequestCallback {
                override fun onEnRouteSuccess(data: String?) {
                    Log.d(TAG, "Received success enroute response: $data")
                }

                override fun onEnRouteFailure(data: String?) {
                    Log.d(TAG, "Received failure enroute response: $data")
                }
            })
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun permissionFailed() {
        view.toggleDeliveredCheckbox(false)
        view.toggleEnRouteCheckbox(false)
        view.navigateToSettings()
    }

    override fun deliveryCaptureImageClicked(captureImage: Boolean) {
        if (captureImage) {
            view.checkShowCamera()
        } else {
            bitmap = null
            imageSendClicked()
        }
    }

    override fun imageDataReceived(data: Intent?) {
        Log.d(TAG, "Attempting to upload image in intent: $data")
        data?.let {
            val extras = data.extras
            bitmap = extras?.get("data") as Bitmap
            view.showImageUploadConfirmDialog(bitmap!!)
        }
    }

    override fun imageSendClicked() {
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
                    Log.d(TAG, "Delivered request success: $delivered")
                    if (delivered != null) {
                        if (delivered.result != "Success") {
                            onDeliveredFailure(delivered.result)
                        } else {
                            view.showDeliveredRequestSuccess()
                        }
                    }
                }

                override fun onDeliveredFailure(data: String?) {
                    Log.d(TAG, "Delivered request failure: $data")
                    view.toggleDeliveredCheckbox (false)
                    view.showDeliveredRequestFailure()
                }
            })
        }
        else {
            view.showDeliveredRequestSuccess()
        }
    }

    override fun imageRetryClicked() {
        bitmap = null
        view.navigateToCamera()
    }

    override fun imageCancelClicked() {
        bitmap = null
        view.toggleDeliveredCheckbox(false)
    }

    override fun onResultCancelled(data: Intent?) {
        // If null, this is cancel from camera, else map
        if( data != null ) {
            view.toggleDeliveredCheckbox(false)
        }
    }

    private fun formatPhoneForDisplay(rawPhone: String): String? {
        var formatted: String? = null

        try {
            if (!rawPhone.isNullOrBlank() && rawPhone.length == 10) {
                if (rawPhone.toLongOrNull() != null) {
                    var areaCode = rawPhone.substring(0..2)
                    var pref = rawPhone.subSequence(3..5)
                    var post = rawPhone.subSequence(6..9)
                    formatted = "( $areaCode ) $pref-$post"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Unable to format phone number", e)
        }
        return formatted
    }

    private fun getLocationRequest(): LocationRequest {
        val intervalProp = view.getSharedPrefUtil().getIntervalPref(false)
        var interval = if (intervalProp.isNullOrBlank()) {
            DEFAULT_UPDATE_INTERVAL
        } else {
            intervalProp.toLong() * 60000
        }

        Log.d(TAG, "Computed location update interval: $interval")
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = interval
        locationRequest.fastestInterval = DEFAULT_FAST_UPDATE_INTERVAL
        locationRequest.maxWaitTime = MAX_WAIT_TIME
        return locationRequest
    }
}