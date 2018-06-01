package com.aisoftware.flexconnect.ui.detail

import com.aisoftware.flexconnect.model.Delivered
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.network.request.DeliveredRequest
import com.aisoftware.flexconnect.network.request.EnRouteRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.util.Logger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

interface DeliveryDetailRequestCallback {
    fun onDeliveredSuccess( delivered: Delivered? )
    fun onDeliveredFailure(data: String?)
}

interface EnRouteRequestCallback {
    fun onEnRouteSuccess(data: String?)
    fun onEnRouteFailure(data: String?)
}

interface DeliveryDetailInteractor {
    fun sendDeliveredUpdate(request: DeliveredRequest, callback: DeliveryDetailRequestCallback)
    fun sendEnRouteUpdate(request: EnRouteRequest, callback: EnRouteRequestCallback)
}

class DeliveryDetailInteractorImpl(): DeliveryDetailInteractor {

    private val TAG = DeliveryDetailInteractorImpl::class.java.simpleName
    private val DELIVERED_REQUEST_CODE = "deliveredRequestCode"
    private val ENROUTE_REQUEST_CODE = "enrouteRequestCode"

    override fun sendDeliveredUpdate(deliveredRequest: DeliveredRequest, callback: DeliveryDetailRequestCallback) {
        Logger.d(TAG, "Attempting to send delivered request: $deliveredRequest")
        try {
            val networkService = NetworkServiceDefault.Builder().build()
            networkService.startRequest(deliveredRequest, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {

                    if( data != null ) {
                        try {
                            val moshi = Moshi.Builder()
                                    .add(KotlinJsonAdapterFactory())
                                    .build()
                            val adapter = moshi.adapter(Delivered::class.java)
                            val result = adapter.fromJson(data)
                            callback.onDeliveredSuccess(result)
                        }
                        catch(e: Exception) {
                            Logger.e(TAG, "Unable to parse delivered response", e)
                        }
                    }
                    else {
                        onFailure(data, requestCode)
                    }
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    callback.onDeliveredFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, DELIVERED_REQUEST_CODE)
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to create delivered request", e)
        }
    }

    override fun sendEnRouteUpdate(request: EnRouteRequest, callback: EnRouteRequestCallback) {
        Logger.d(TAG, "Attempting to send enroute request: $request")
        try {
            val networkService = NetworkServiceDefault.Builder().build()
            networkService.startRequest(request, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    callback.onEnRouteSuccess(data)
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    callback.onEnRouteFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, ENROUTE_REQUEST_CODE)
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to create enroute request", e)
        }
    }
}