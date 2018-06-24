package com.aisoftware.flexconnect.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.ui.main.MainActivity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.SharedPrefUtil
import com.aisoftware.flexconnect.util.SharedPrefUtilImpl

interface ActivityBaseView {
    fun isNetworkAvailable(): Boolean
    fun getNetworkService(): NetworkService
    fun getSharedPrefUtil(): SharedPrefUtil
    fun showNetworkAvailabilityError()
    fun logout()
    fun navigateToMain()
    fun navigateToDashboard()
    fun showLogoutDialog()
}

open class FlexConnectActivityBase: AppCompatActivity(), ActivityBaseView {

    private val TAG = this.javaClass.simpleName
    private lateinit var sharedPrefsUtil: SharedPrefUtil

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPrefsUtil = SharedPrefUtilImpl(this)
        CrashLogger.log(1, TAG, "Activity onCreate()")
    }

    override fun finish() {
        super.finish()
        CrashLogger.log(1, TAG, "Activity finish()")
    }

    override fun onRestart() {
        super.onRestart()
        CrashLogger.log(1, TAG, "Activity onRestart()")
    }

    override fun onStart() {
        super.onStart()
        CrashLogger.log(1, TAG, "Activity onStart()")
    }

    override fun onStop() {
        super.onStop()
        CrashLogger.log(1, TAG, "Activity onStop()")
    }

    override fun onPause() {
        super.onPause()
        CrashLogger.log(1, TAG, "Activity onPause()")
    }

    override fun onResume() {
        super.onResume()
        CrashLogger.log(1, TAG, "Activity onResume()")
    }

    override fun onDestroy() {
        super.onDestroy()
        CrashLogger.log(1, TAG, "Activity onDestroy()")
    }

    override fun navigateToMain() {
        val intent = MainActivity.getIntent(this)
        startActivity(intent)
    }

    override fun navigateToDashboard() {
        val intent = DashboardActivity.getIntent(this, true)
        startActivity(intent)
    }

    override fun getNetworkService(): NetworkService = NetworkServiceDefault.Builder().build()

    override fun getSharedPrefUtil(): SharedPrefUtil = sharedPrefsUtil

    override fun isNetworkAvailable(): Boolean =
        ((getApplication() as FlexConnectApplication).isNetworkAvailable())

    override fun showNetworkAvailabilityError() {
        if( !isFinishing) {
            CrashLogger.log(1, TAG, "Showing networkAvailabilityError dialog")
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.network_availability_error_title))
                    .setMessage(getString(R.string.network_availability_error_message))
                    .setPositiveButton(Constants.POS_BUTTON) { dialog, id ->
                        dialog.dismiss()
                    }.create().show()
        }
    }

    override fun showLogoutDialog() {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_logout_title))
                    .setMessage(getString(R.string.delivery_logout_message))
                    .setPositiveButton(getString(R.string.delivery_logout_pos_button)) { dialog, id ->
                        logout()
                    }
                    .setNegativeButton(getString(R.string.delivery_logout_neg_button)) { dialog, id ->
                        dialog.dismiss()
                    }.create().show()
        }
    }

    override fun logout() {
        val sharedPrefUtil = getSharedPrefUtil()
        sharedPrefUtil.getUserPref(true)
        sharedPrefUtil.getIntervalPref(true)
        (application as FlexConnectApplication).getAppDatabase()?.clearAllTables()
        CrashLogger.log(1, TAG, "User logged out")
        navigateToMain()
        finish()
    }
}