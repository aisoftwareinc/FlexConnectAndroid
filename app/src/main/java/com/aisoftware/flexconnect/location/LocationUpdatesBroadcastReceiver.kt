package com.aisoftware.flexconnect.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.google.android.gms.location.LocationResult

const val ACTION_PROCESS_UPDATES = "com.aisoftware.flexconnect.location.locationupdatespendingintent.action.PROCESS_UPDATES"

class LocationUpdatesBroadcastReceiver: BroadcastReceiver() {

    private val TAG = LocationUpdatesBroadcastReceiver::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        if( intent != null ) {
            val action = intent.action
            if( action == ACTION_PROCESS_UPDATES ) {
                val result = LocationResult.extractResult(intent)

                if( result != null ) {
                    val locations = result.locations
                    val location = locations.first()
                    val latitude = location.latitude
                    val longitude = location.longitude

                    Logger.d(TAG, "Location Broadcast Receiver, latitude: $latitude longitude: $longitude")
                    try
                    {
                        val locationUpdatesInteractor = LocationUpdatesInteractorImpl(context!!, object: LocationUpdatesCallback {
                            override fun onSuccess(data: String?) {
                                Logger.d(TAG, "Received report location success response: $data")
                            }

                            override fun onFailure(data: String?) {
                                Logger.d(TAG, "Received report location failure response: $data")

                                try {
                                    CrashLogger.logException(1, TAG, "Received report location failure, data: $data", Exception("Unable to report location, with data: $data"))
                                }
                                catch(e: Exception) {
                                    Logger.e(TAG, "Unable to send crashlytics report.  CrashLogger is null", e)
                                }
                            }
                        })
                        locationUpdatesInteractor.reportLocation(latitude, longitude)
                    }
                    catch(e: Exception) {
                        Logger.e(TAG, "Unable to complete report location operation", e)
                        try {
                            CrashLogger.logException(1, TAG, "Unable to complete report location operation", e)
                        }
                        catch(e: Exception) {
                            Logger.e(TAG, "Unable to send crashlytics report.  CrashLogger is null", e)
                        }
                    }
                }
            }
        }
    }
}