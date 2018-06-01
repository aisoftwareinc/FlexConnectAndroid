package com.aisoftware.flexconnect.ui.main

import com.aisoftware.flexconnect.model.AuthenticatePhone
import com.aisoftware.flexconnect.model.TimerInterval
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.network.request.AuthenticatePhoneRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.TImerIntervalRequest
import com.aisoftware.flexconnect.util.Logger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface OnFetchAuthCallback {
    fun onAuthFetchSuccess(authCode: String, phoneNumber: String)
    fun onFetchFailure(date: String)

    fun onTimerFetchSuccess(interval: String)

}

interface MainInteractor {
    fun setCallback(callback: OnFetchAuthCallback)
    fun fetchAuthCode(phoneNumber: String)
    fun fetchTimerInterval()
}

class MainInteractorImpl: MainInteractor {

    private val TAG = MainInteractorImpl::class.java.simpleName
    private lateinit var callback: OnFetchAuthCallback
    private val AUTH_REQUEST_CODE = "authPhoneRequestCode"
    private val TIMER_REQUEST_CODE = "timerRequestCode"

    override fun setCallback(callback: OnFetchAuthCallback) {
        this.callback = callback
    }

    override fun fetchAuthCode(phoneNumber: String) {
        Logger.d(TAG, "Attempting to fetch auth code with phone number: $phoneNumber")
        val request = AuthenticatePhoneRequest(phoneNumber)
        val networkService = NetworkServiceDefault.Builder().build()
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
                        onFailure(data, requestCode)
                    }
                }
                else {
                    onFailure(data, requestCode)
                }
            }

            override fun onFailure(data: String?, requestCode: String?) {
                Logger.d(TAG, "Received onFailure data: $data")
                callback.onFetchFailure("Unable to fetch auth code, null response")
            }

            override fun onComplete(requestCode: String?) { }
        }, AUTH_REQUEST_CODE)
    }

    override fun fetchTimerInterval() {
        Logger.d(TAG, "Attempting to fetch timer interval")
        val request = TImerIntervalRequest()
        val networkService = NetworkServiceDefault.Builder().build()
        networkService.startRequest(request, object: NetworkRequestCallback {
            override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                if( data != null ) {
                    try {
                        val moshi = Moshi.Builder()
                                .add(KotlinJsonAdapterFactory())
                                .build()
                        val adapter = moshi.adapter(TimerInterval::class.java)
                        val interval = adapter.fromJson(data)

                        if (interval != null && !interval.duration.isBlank()) {
                            Logger.d(TAG, "Fetched interval: ${interval.duration}")
                            callback.onTimerFetchSuccess(interval.duration)
                        }
                    }
                    catch(e: Exception) {
                        Logger.e(TAG, "Unable to process data response: $data", e)
                        onFailure(data, requestCode)
                    }
                }
                else {
                    onFailure(data, requestCode)
                }
            }

            override fun onFailure(data: String?, requestCode: String?) {
                Logger.d(TAG, "Received onFailure data: $data")
                callback.onFetchFailure("Unable to fetch interval, null response")
            }

            override fun onComplete(requestCode: String?) { }
        }, AUTH_REQUEST_CODE)
    }

}