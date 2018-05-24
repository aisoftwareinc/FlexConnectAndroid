package com.aisoftware.flexconnect.ui.main

import android.util.Log
import com.aisoftware.flexconnect.model.AuthenticatePhone
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.network.request.AuthenticatePhoneRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface OnFetchAuthCallback {
    fun onFetchSuccess(authCode: String, phoneNumber: String)
    fun onFetchFailure(date: String)
}

interface MainInteractor {
    fun setCallback(callback: OnFetchAuthCallback)
    fun fetchAuthCode(phoneNumber: String)
}

class MainInteractorImpl: MainInteractor {

    private val TAG = MainInteractorImpl::class.java.simpleName
    private lateinit var callback: OnFetchAuthCallback
    private val AUTH_REQUEST_CODE = "authPhoneRequest"

    override fun setCallback(callback: OnFetchAuthCallback) {
        this.callback = callback
    }

    override fun fetchAuthCode(phoneNumber: String) {
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
                            callback.onFetchSuccess(authCode, phoneNumber)
                        }
                    }
                    catch(e: Exception) {
                        Log.e(TAG, "Unable to process data response: $data", e)
                        onFailure(data, requestCode)
                    }
                }
                else {
                    onFailure(data, requestCode)
                }
            }

            override fun onFailure(data: String?, requestCode: String?) {
                Log.d(TAG, "Received onFailure data: $data")
                callback.onFetchFailure("Unable to fetch auth code, null response")
            }

            override fun onComplete(requestCode: String?) { }
        }, AUTH_REQUEST_CODE)
    }
}