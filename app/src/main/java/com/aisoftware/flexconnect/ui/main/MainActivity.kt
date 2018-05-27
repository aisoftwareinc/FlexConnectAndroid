package com.aisoftware.flexconnect.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.ui.DashboardActivity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.SharedPrefUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), MainView {

    private val TAG = MainActivity::class.java.simpleName
    private lateinit var mainPresenter: MainPresenter
    private lateinit var mainInteractor: MainInteractor

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent = Intent(context, MainActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainInteractor = MainInteractorImpl()
        mainPresenter = MainPresenterImpl(this, mainInteractor, SharedPrefUtil(this))
        mainPresenter.initialize()

        submitButton.setOnClickListener {
            mainPresenter.submitClicked(
                    authCodeEditText.text.toString(),
                    phoneEditText.text.toString())
        }
    }

    override fun navigateToDashboard() {
        val intent = DashboardActivity.getIntent(this)
        startActivity(intent)
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

    override fun showErrorDialog() {
        if( !isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_auth_error_title))
                    .setMessage(getString(R.string.delivery_auth_error_message))
                    .setPositiveButton(Constants.POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }
}
