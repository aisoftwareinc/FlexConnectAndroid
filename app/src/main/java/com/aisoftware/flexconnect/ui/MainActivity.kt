package com.aisoftware.flexconnect.ui

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.SharedPrefUtil
import kotlinx.android.synthetic.main.activity_main.*

/**
 * https://developer.android.com/training/scheduling/wakelock
 */
class MainActivity : AppCompatActivity() {

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
            val phoneNumber = phoneEditText.text.toString()
            if( isPhoneValid(phoneNumber) ) {
                SharedPrefUtil.setUserProp(this.applicationContext, phoneNumber)
                // send network request
                navigateToDashboard()
            }
            else{
                showErrorDialog(getString(R.string.submit_dialog_title), getString(R.string.submit_dialog_error_message))
            }

        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
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
        titleImageView.visibility = View.VISIBLE
        phoneEditText.visibility = View.VISIBLE
        submitButton.visibility = View.VISIBLE
        authCodeEditText.visibility = View.VISIBLE
    }

    fun showErrorDialog(title: String, message: String) {
        if( !isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(Constants.VALIDATION_POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }

    private fun isPhoneValid(phoneNumber: String): Boolean {
        if( phoneNumber.isBlank() ) {
            return false
        }

        val trimmed = phoneNumber
                .replace("(", "")
                .replace(")", "")
                .replace("-", "")
                .replace(" ", "")
                .trim()

        if( trimmed.length < 10 ) {
            return false
        }
        return true
    }
}
