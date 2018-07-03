package com.aisoftware.flexconnect.ui.main

import com.aisoftware.flexconnect.model.AuthenticatePhone
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.request.AuthenticatePhoneRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface OnFetchAuthCallback {
    fun onAuthFetchSuccess(authCode: String, phoneNumber: String)
    fun onFetchFailure(date: String)
}

interface MainInteractor {
    fun setCallback(callback: OnFetchAuthCallback)
    fun fetchAuthCode(phoneNumber: String)
}

class MainInteractorImpl(private val networkService: NetworkService): MainInteractor {

    private val TAG = MainInteractorImpl::class.java.simpleName
    private lateinit var callback: OnFetchAuthCallback
    private val AUTH_REQUEST_CODE = "authPhoneRequestCode"

    override fun setCallback(callback: OnFetchAuthCallback) {
        this.callback = callback
    }

    override fun fetchAuthCode(phoneNumber: String) {
        Logger.d(TAG, "Attempting to fetch auth code with phone number: $phoneNumber")
        val request = AuthenticatePhoneRequest(phoneNumber)
        networkService.startRequest(request, object: NetworkRequestCallback {
            override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                if( data != null ) {
                    try {
                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                        val adapter = moshi.adapter(AuthenticatePhone::class.java)
                        val authPhoneResponse = adapter.fromJson(data)

                        if (authPhoneResponse != null && !authPhoneResponse.authCode.isBlank()) {
                            val authCode = authPhoneResponse.authCode
                            Logger.d(TAG, "Fetched auth code: $authCode")
                            callback.onAuthFetchSuccess(authCode, phoneNumber)
                        }
                    }
                    catch(e: Exception) {
                        Logger.e(TAG, "Unable to process data response: $data", e)
                        CrashLogger.logException(1, TAG, "Unable to process fetchAuthCode response: $data", e)
                        callback.onFetchFailure("Unable to fetch auth code, null response")
                    }
                }
                else {
                    onFailure(data, requestCode)
                }
            }

            override fun onFailure(data: String?, requestCode: String?) {
                Logger.d(TAG, "Received onFailure data: $data")
                CrashLogger.log(1, TAG, "Received onFailure data: $data")
                callback.onFetchFailure("Unable to fetch auth code, null response")
            }

            override fun onComplete(requestCode: String?) { }
        }, AUTH_REQUEST_CODE)
    }
}