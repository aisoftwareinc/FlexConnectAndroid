package com.aisoftware.flexconnect.ui

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.model.AuthenticatePhone
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.network.request.AuthenticatePhoneRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.SharedPrefUtil
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.android.synthetic.main.activity_main.*



/**
 * https://developer.android.com/training/scheduling/wakelock
 */
class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val AUTH_REQUEST_CODE = "authPhoneRequest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (SharedPrefUtil.userPrefExists(this.applicationContext)) {
            initializeViewLoading()
            navigateToDashboard()
        }
        else {
            initializeViewDefault()
        }

        submitButton.setOnClickListener {
            if( SharedPrefUtil.userPrefExists(this)) {
                navigateToDashboard()
            }
            else {

                val phoneNumber = phoneEditText.text.toString()
                if (isPhoneValid(phoneNumber)) {
                    fetchAuthCode(phoneNumber)
                } else {
                    showErrorDialog(getString(R.string.submit_dialog_title), getString(R.string.submit_dialog_error_message))
                }
            }
        }
    }

    private fun fetchAuthCode(phoneNumber: String) {
        val formattedNumber = formatPhoneNumber(phoneNumber)
        val request = AuthenticatePhoneRequest(formattedNumber)
        val networkService = NetworkServiceDefault.Builder().build()
        networkService.startRequest(request, object: NetworkRequestCallback{
            override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                if( data != null ) {
                    val moshi = Moshi.Builder()
                            .add(KotlinJsonAdapterFactory())
                            .build()
                    val adapter =  moshi.adapter(AuthenticatePhone::class.java)
                    val authPhoneResponse = adapter.fromJson(data)
                    if( authPhoneResponse!= null && !authPhoneResponse.authCode.isBlank() ) {
                        SharedPrefUtil.setUserProp(applicationContext, formattedNumber)
                        authCodeEditText.visibility = View.VISIBLE
                    }
                }
                else {
                    onFailure(data, requestCode)
                }
            }

            override fun onFailure(data: String?, requestCode: String?) {
                Log.d(TAG, "Received onFailure data: $data")
                showErrorDialog(getString(R.string.delivery_auth_error_title), getString(R.string.delivery_auth_error_message))
            }

            override fun onComplete(requestCode: String?) { }
        }, AUTH_REQUEST_CODE)
    }


    private fun navigateToDashboard() {
        val intent = DashboardActivity.getIntent(this)
        startActivity(intent)
    }

    private fun initializeViewLoading() {
        progressBar1.visibility = View.VISIBLE

        titleImageView.visibility = View.INVISIBLE
        phoneEditText.visibility = View.INVISIBLE
        submitButton.visibility = View.INVISIBLE
        authCodeEditText.visibility = View.INVISIBLE
    }

    private fun initializeViewDefault() {
        progressBar1.visibility = View.INVISIBLE
        authCodeEditText.visibility = View.GONE

        titleImageView.visibility = View.VISIBLE
        phoneEditText.visibility = View.VISIBLE
        submitButton.visibility = View.VISIBLE
    }

    private fun showErrorDialog(title: String, message: String) {
        if( !isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(Constants.POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }

    private fun isPhoneValid(phoneNumber: String): Boolean {
        val formatted = formatPhoneNumber(phoneNumber)
        if( formatted.isBlank() ) {
            return false
        }

        if( formatted.length < 10 ) {
            return false
        }
        return true
    }

    private fun formatPhoneNumber(phoneNumber: String): String {
        var formatted = ""

        if( phoneNumber.isBlank() ) {
            return formatted
        }

        formatted = phoneNumber
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(" ", "")
                .trim()

        return formatted
    }
}
