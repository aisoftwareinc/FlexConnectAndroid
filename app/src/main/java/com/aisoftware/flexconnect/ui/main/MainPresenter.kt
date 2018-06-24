package com.aisoftware.flexconnect.ui.main

import com.aisoftware.flexconnect.ui.ActivityBaseView
import com.aisoftware.flexconnect.util.SharedPrefUtil

interface MainView: ActivityBaseView {
    fun savePhoneNumber(phoneNumber: String)
    fun initializeViewLoading()
    fun initializeViewDefault()
    fun showErrorDialog()
    fun showAuthCodeInput(showField: Boolean)
}

interface MainPresenter {
    fun initialize()
    fun submitClicked(authCodeEditText: String, phoneEditText: String)
}

class MainPresenterImpl(val view: MainView, private val interactor: MainInteractor, private val sharedPrefUtil: SharedPrefUtil): MainPresenter, OnFetchAuthCallback {

    private val TAG = MainPresenterImpl::class.java.simpleName
    private lateinit var authCode: String
    private lateinit var phoneNumber: String

    init {
        interactor.setCallback(this)
    }

    override fun initialize() {
        if (sharedPrefUtil.userPrefExists()) {
            view.initializeViewLoading()
            view.navigateToDashboard()
        }
        else {
            view.initializeViewDefault()
        }
    }

    override fun submitClicked(authCodeEditText: String, phoneEditText: String) {
        // User has authenticated, forward to dashboard
        if( !authCodeEditText.isBlank()) {
            if ( validAuthCode(authCodeEditText) ) {
                // Save to database
                view.savePhoneNumber(formatPhoneNumber(phoneEditText))

                // Send to dashboard
                view.navigateToDashboard()
            }
            else {
                sharedPrefUtil.getUserPref(true)
                view.initializeViewDefault()
                view.showErrorDialog()
            }
        }
        else {
            // Authenticate user flow
            if( view.isNetworkAvailable() ) {
                if (isPhoneValid(phoneEditText)) {
                    phoneNumber = formatPhoneNumber(phoneEditText)
                    interactor.fetchAuthCode(phoneNumber)
//                    interactor.fetchTimerInterval()
                } else {
                    view.showErrorDialog()
                }
            }
            else {
                view.showNetworkAvailabilityError()
            }
        }
    }

    override fun onAuthFetchSuccess(authCode: String, phoneNumber: String) {
        this.authCode = authCode
        view.showAuthCodeInput(true)
    }

    override fun onFetchFailure(date: String) {
        view.showErrorDialog()
    }

    private fun validAuthCode(authCodeEditText: String): Boolean {
        return if ( authCode.isBlank() ||
                authCodeEditText.isBlank() ||
                (authCode != authCodeEditText)) {
            false
        }
        else {
            sharedPrefUtil.setUserProp(phoneNumber)
            true
        }
    }

    override fun onTimerFetchSuccess(interval: String) {
        sharedPrefUtil.setIntervalProp(interval)
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