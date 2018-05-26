package com.aisoftware.flexconnect.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
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

                    // Update API side
                    Log.d(TAG, " Location update latitude: $latitude longitude: $longitude")

                }
            }
        }
    }
}