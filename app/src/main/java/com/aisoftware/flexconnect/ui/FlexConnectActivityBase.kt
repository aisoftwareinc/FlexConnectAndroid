package com.aisoftware.flexconnect.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.SharedPrefUtil

interface ActivityBaseView {
    fun isNetworkAvailable(): Boolean
    fun getSharedPrefUtil(): SharedPrefUtil
    fun showNetworkAvailabilityError()
}

open class FlexConnectActivityBase: AppCompatActivity(), ActivityBaseView {

    private lateinit var sharedPrefsUtil: SharedPrefUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefsUtil = SharedPrefUtil(this)
    }

    override fun getSharedPrefUtil(): SharedPrefUtil = sharedPrefsUtil

    override fun isNetworkAvailable(): Boolean =
        ((getApplication() as FlexConnectApplication).isNetworkAvailable())

    override fun showNetworkAvailabilityError() {
        if( !isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.network_availability_error_title))
                    .setMessage(getString(R.string.network_availability_error_message))
                    .setPositiveButton(Constants.POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }
}