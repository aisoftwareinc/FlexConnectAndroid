package com.aisoftware.flexconnect.location

import android.content.Context
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.ReportLocationRequest
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.aisoftware.flexconnect.util.SharedPrefUtilImpl

interface LocationUpdatesCallback {
    fun onSuccess(data: String?)
    fun onFailure(data: String?)
}

interface LocationUpdatesInteractor {
    fun reportLocation(latitude: Double, longitude: Double, phoneNumber: String)
}

class LocationUpdatesInteractorImpl(val context: Context, val callback: LocationUpdatesCallback): LocationUpdatesInteractor{

    val TAG = LocationUpdatesInteractorImpl::class.java.simpleName
    val REPORT_LOCATION_REQUEST_CODE = "reportLocationRequestCode"

    override fun reportLocation(latitude: Double, longitude: Double, phoneNumber: String) {
        Logger.d(TAG, "Reporting location with latitude: $latitude and longitude: $longitude with phone: $phoneNumber")
        val sharedPreferences = SharedPrefUtilImpl(context)
//        val phoneNumber = sharedPreferences.getUserPref(false)
        if( phoneNumber.isNullOrBlank() ) {
            callback.onFailure("Unable to determine phone number from preferences")
        }

        try {
            val reportLocationRequest = ReportLocationRequest(phoneNumber, latitude.toString(), longitude.toString())
            val networkService = NetworkServiceDefault.Builder().build()
            networkService.startRequest(reportLocationRequest, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    callback.onSuccess(data)
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    CrashLogger.log(1, TAG, "Unable to process report location response: $data")
                    callback.onFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, REPORT_LOCATION_REQUEST_CODE)
        }
        catch(e: Exception) {
            Logger.e(TAG, "Unable to complete report location request", e)
            try {
                CrashLogger.logException(1, TAG, "Unable to complete report location request", e)
            }
            catch(ex: Exception) {
                Logger.e(TAG, "Unable to complete report location processing" ,e)
            }
            callback.onFailure("Unable to complete report location request: ${e.localizedMessage}")
        }
    }
}