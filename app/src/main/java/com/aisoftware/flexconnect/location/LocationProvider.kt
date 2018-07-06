package com.aisoftware.flexconnect.location

import android.app.PendingIntent
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest

interface LocationProviderCallback {

}

interface LocationProvider {
    fun requestLocationUpdates(locationRequest: LocationRequest, pendingIntent: PendingIntent)
    fun removeLocationUpdates(pendingIntent: PendingIntent)
}

class LocationProviderImpl(val context: Context, val callback: LocationProviderCallback): LocationProvider {

    private val TAG = LocationProviderImpl::class.java.simpleName
    private val DEFAULT_FAST_UPDATE_INTERVAL: Long = 30000 // every 30 seconds
    private val DEFAULT_UPDATE_INTERVAL: Long = 60000 // every 60 seconds
    private val MAX_WAIT_TIME: Long = DEFAULT_UPDATE_INTERVAL * 10 // 10 minutes
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun requestLocationUpdates(locationRequest: LocationRequest, pendingIntent: PendingIntent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun removeLocationUpdates(pendingIntent: PendingIntent) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun checkDeviceConfig() {
        val locationRequest = LocationRequest()
        with(locationRequest) {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
            fastestInterval = DEFAULT_FAST_UPDATE_INTERVAL
            maxWaitTime = MAX_WAIT_TIME
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val resultTask = LocationServices.getSettingsClient(context).checkLocationSettings(builder.build())


    }
}