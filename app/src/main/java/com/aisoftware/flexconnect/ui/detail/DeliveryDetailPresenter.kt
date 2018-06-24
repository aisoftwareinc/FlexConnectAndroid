package com.aisoftware.flexconnect.ui.detail

import android.content.Intent
import android.graphics.Bitmap
import com.aisoftware.flexconnect.BuildConfig
import com.aisoftware.flexconnect.model.Delivered
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.network.request.DeliveredRequest
import com.aisoftware.flexconnect.network.request.EnRouteRequest
import com.aisoftware.flexconnect.network.request.PendingEnRouteRequest
import com.aisoftware.flexconnect.ui.ActivityBaseView
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.Logger
import com.google.android.gms.location.LocationRequest

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
    fun updateEnRouteStatus(isEnRoute: Boolean, delete: Boolean)
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
            view.getSharedPrefUtil().setIntervalProp(delivery.interval)
            val displayPhoneNumber = formatPhoneForDisplay(this.delivery.customerPhone) ?: ""
            isEnRoute = view.getSharedPrefUtil().getEnRouteStatus(delivery.guid, false)
            view.initializeView(delivery, displayPhoneNumber, isEnRoute)
        }
    }

    override fun checkLocationUpdate() {
        view.checkLocationUpdate()
    }

    override fun stopLocationUpdate() {
        if( view.isNetworkAvailable() ) {
            val phoneNumber = view.getSharedPrefUtil().getUserPref(false)
            val guid = delivery.guid
            val request = PendingEnRouteRequest(phoneNumber, guid)
            interactor.sendPendingEnRouteUpdate(request, object : EnRouteRequestCallback {
                override fun onEnRouteSuccess(data: String?) {
                    Logger.d(TAG, "Received success enroute response: $data")
                    updateEnRouteStatus(false, true)
                    view.stopLocationUpdate()
                }

                override fun onEnRouteFailure(data: String?) {
                    Logger.d(TAG, "Received failure enroute response: $data")
                    updateEnRouteStatus(false, true)
                    view.stopLocationUpdate()
                }
            })
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun updateEnRouteStatus(isEnRoute: Boolean, delete: Boolean) {
        view.getSharedPrefUtil().setEnRouteStatus(delivery.guid, isEnRoute)

        if( !isEnRoute && delete )
        {
            view.getSharedPrefUtil().getEnRouteStatus(delivery.guid, true)
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

    override fun locationPermissionPassed() {
        if( view.isNetworkAvailable() ) {
            val phoneNumber = view.getSharedPrefUtil().getUserPref(false)
            val guid = delivery.guid
            val request = EnRouteRequest(phoneNumber, guid)

            view.startLocationUpdate(getLocationRequest())
            interactor.sendEnRouteUpdate(request, object : EnRouteRequestCallback {
                override fun onEnRouteSuccess(data: String?) {
                    updateEnRouteStatus(true, false)
                    Logger.d(TAG, "Received success enroute response: $data")
                }

                override fun onEnRouteFailure(data: String?) {
                    updateEnRouteStatus(false, true)
                    view.getSharedPrefUtil().setEnRouteStatus(delivery.guid, false)
                    Logger.d(TAG, "Received failure enroute response: $data")
                }
            })
        }
        else {
            view.showNetworkAvailabilityError()
        }
    }

    override fun permissionFailed() {
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
        Logger.d(TAG, "Attempting to upload image in intent: $data")
        data?.let {
            val extras = data.extras
            bitmap = extras?.get("data") as Bitmap
            view.showImageUploadConfirmDialog(bitmap!!)
        }
    }

    override fun imageSendClicked() {
        if( view.isNetworkAvailable() ) {
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
            } else {
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
        val intervalProp = view.getSharedPrefUtil().getIntervalPref(false)
        var interval = if (intervalProp.isBlank()) {
            DEFAULT_UPDATE_INTERVAL
        } else {
            intervalProp.toLong() * 60000
        }

        if( BuildConfig.USE_DEFAULT_INTERVAL ) {
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