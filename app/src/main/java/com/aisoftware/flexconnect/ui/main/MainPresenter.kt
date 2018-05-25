package com.aisoftware.flexconnect.ui.main

import com.aisoftware.flexconnect.util.SharedPrefUtil

interface MainView {
    fun initializeViewLoading()
    fun navigateToDashboard(phoneNumber: String)
    fun initializeViewDefault()
    fun showErrorDialog()
    fun showAuthCodeInput(showField: Boolean)
}

interface MainPresenter {
    fun initialize()
    fun submitClicked(authCodeEditText: String, phoneEditText: String)
}

class MainPresenterImpl(val view: MainView, val interactor: MainInteractor, val sharedPrefUtil: SharedPrefUtil): MainPresenter, OnFetchAuthCallback {

    private val TAG = MainPresenterImpl::class.java.simpleName
    private lateinit var authCode: String

    init {
        interactor.setCallback(this)
    }

    override fun initialize() {
        if (sharedPrefUtil.userPrefExists()) {
            view.initializeViewLoading()
            view.navigateToDashboard(sharedPrefUtil.getUserPref(false))
        }
        else {
            view.initializeViewDefault()
        }
    }

    override fun submitClicked(authCodeEditText: String, phoneEditText: String) {
        if( sharedPrefUtil.userPrefExists()) {
            if ( validAuthCode(authCodeEditText) ) {
                view.navigateToDashboard(sharedPrefUtil.getUserPref(false))
            }
            else {
                view.showErrorDialog()
            }
        }
        else {
            if (isPhoneValid(phoneEditText)) {
                val phoneNumber = formatPhoneNumber(phoneEditText)
                interactor.fetchAuthCode(phoneNumber)
            } else {
                view.showErrorDialog()
            }
        }
    }

    override fun onFetchSuccess(authCode: String, phoneNumber: String) {
        this.authCode = authCode
        sharedPrefUtil.setUserProp(phoneNumber)
        view.showAuthCodeInput(true)
    }

    override fun onFetchFailure(date: String) {
        view.showErrorDialog()
    }

    private fun validAuthCode(authCodeEditText: String): Boolean {
        if ( authCode.isNullOrBlank() ||
                authCodeEditText.isNullOrBlank() ||
                (authCode != authCodeEditText)) {
            return false
        }
        return true
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