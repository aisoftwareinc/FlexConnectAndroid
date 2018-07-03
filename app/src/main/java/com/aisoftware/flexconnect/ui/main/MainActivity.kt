package com.aisoftware.flexconnect.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.View
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.model.PhoneNumber
import com.aisoftware.flexconnect.ui.FlexConnectActivityBase
import com.aisoftware.flexconnect.util.Logger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : FlexConnectActivityBase(), MainView {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mainPresenter: MainPresenter

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainPresenter = MainPresenterImpl(this, MainInteractorImpl(getNetworkService()), getSharedPrefUtil())
        mainPresenter.initialize()

        submitButton.setOnClickListener {
            mainPresenter.submitClicked(
                    authCodeEditText.text.toString(),
                    phoneEditText.text.toString())
        }
    }

    override fun initializeViewLoading() {
        progressBar1.visibility = View.VISIBLE

        titleImageView.visibility = View.INVISIBLE
        phoneEditText.visibility = View.INVISIBLE
        submitButton.visibility = View.INVISIBLE
        authCodeEditText.visibility = View.GONE
    }

    override fun initializeViewDefault() {
        progressBar1.visibility = View.INVISIBLE
        authCodeEditText.text = null
        authCodeEditText.visibility = View.GONE

        titleImageView.visibility = View.VISIBLE
        phoneEditText.visibility = View.VISIBLE
        submitButton.visibility = View.VISIBLE
    }

    override fun showAuthCodeInput(showField: Boolean) {
        if(showField) {
            authCodeEditText.visibility = View.VISIBLE
        }
        else {
            authCodeEditText.visibility = View.GONE
        }
    }

    override fun savePhoneNumber(phoneNumber: String) {
        Logger.d(TAG, "Saving phone number: $phoneNumber")
        (application as FlexConnectApplication).getAppDatabase()?.phoneNumberDao()?.insert(PhoneNumber(phoneNumber = phoneNumber))
    }

    override fun showErrorDialog() {
        if( !isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_auth_error_title))
                    .setMessage(getString(R.string.delivery_auth_error_message))
                    .setPositiveButton(getString(R.string.delivery_logout_pos_button)) { dialog, id ->
                        dialog.dismiss()
                    }.create().show()
        }
    }
}
