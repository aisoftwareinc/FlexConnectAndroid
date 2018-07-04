package com.aisoftware.flexconnect.ui.detail

import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.model.Delivered
import com.aisoftware.flexconnect.model.LastUpdate
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.request.DeliveredRequest
import com.aisoftware.flexconnect.network.request.EnRouteRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.PendingEnRouteRequest
import com.aisoftware.flexconnect.network.request.ReportLocationRequest
import com.aisoftware.flexconnect.util.CrashLogger
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

interface ReportLocationCallback {
    fun onReportLocationSuccess(data: String?)
    fun onReportLocationFailure(data: String?)
}

interface DeliveryDetailInteractor {
    fun sendDeliveredUpdate(request: DeliveredRequest, callback: DeliveryDetailRequestCallback)
    fun sendEnRouteUpdate(request: EnRouteRequest, callback: EnRouteRequestCallback)
    fun sendPendingEnRouteUpdate(request: PendingEnRouteRequest, callback: EnRouteRequestCallback)
    fun sendLocationUpdate(request: ReportLocationRequest, callback: ReportLocationCallback)
    fun updateLastUpdateTime(timeStamp: String)
}

class DeliveryDetailInteractorImpl(private val networkService: NetworkService, private val appDatabase: AppDatabase): DeliveryDetailInteractor {

    private val TAG = DeliveryDetailInteractorImpl::class.java.simpleName
    private val DELIVERED_REQUEST_CODE = "deliveredRequestCode"
    private val ENROUTE_REQUEST_CODE = "enrouteRequestCode"
    private val REPORT_LOCATION_REQUEST_CODE = "reportLocationRequestCode"

    override fun sendDeliveredUpdate(deliveredRequest: DeliveredRequest, callback: DeliveryDetailRequestCallback) {
        Logger.d(TAG, "Attempting to send delivered request: $deliveredRequest")
        try {
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
                            CrashLogger.logException(1, TAG, "Unable to parse delivered response data: $data", e)
                            callback.onDeliveredFailure(data)
                        }
                    }
                    else {
                        onFailure(data, requestCode)
                    }
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    Logger.d(TAG, "Unable to process delivery update request, data: $data")
                    CrashLogger.log(1, TAG, "Unable to process update request, data: $data")
                    callback.onDeliveredFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, DELIVERED_REQUEST_CODE)
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to create delivered request", e)
            CrashLogger.logException(1, TAG, "Unable to create delivered request", e)
        }
    }

    override fun sendEnRouteUpdate(request: EnRouteRequest, callback: EnRouteRequestCallback) {
        Logger.d(TAG, "Attempting to send enroute request: $request")
        try {
            networkService.startRequest(request, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    callback.onEnRouteSuccess(data)
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    CrashLogger.log(1, TAG, "Unable to process enroute update, data $data")
                    callback.onEnRouteFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, ENROUTE_REQUEST_CODE)
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to create enroute request", e)
            CrashLogger.logException(1, TAG, "Unable to create enroute request", e)
        }
    }

    override fun sendPendingEnRouteUpdate(request: PendingEnRouteRequest, callback: EnRouteRequestCallback) {
        Logger.d(TAG, "Attempting to send enroute request: $request")
        try {
            networkService.startRequest(request, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    callback.onEnRouteSuccess(data)
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    CrashLogger.log(1, TAG, "Unable to process enroute update, data $data")
                    callback.onEnRouteFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, ENROUTE_REQUEST_CODE)
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to create enroute request", e)
            CrashLogger.logException(1, TAG, "Unable to create enroute request", e)
        }
    }

    override fun sendLocationUpdate(request: ReportLocationRequest, callback: ReportLocationCallback) {
        Logger.d(TAG, "Attempting to send report location request: $request")
        try {
            networkService.startRequest(request, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    callback.onReportLocationSuccess(data)
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    CrashLogger.log(1, TAG, "Unable to process report location response: $data")
                    callback.onReportLocationFailure(data)
                }

                override fun onComplete(requestCode: String?) { }
            }, REPORT_LOCATION_REQUEST_CODE)
        }
        catch(e: Exception) {
            Logger.e(TAG, "Unable to complete report location request", e)
            try {
                CrashLogger.logException(1, TAG, "Unable to complete report location request", e)
            }
            catch(ex: Exception) {
                Logger.e(TAG, "Unable to complete report location processing" ,e)
            }
            callback.onReportLocationFailure("Unable to complete report location request: ${e.localizedMessage}")
        }
    }

    override fun updateLastUpdateTime(timeStamp: String) {
        appDatabase.let {
            Logger.d(TAG, "Inserting last updated time of: $timeStamp")
            it.lastUpdateDao().insert(LastUpdate(lastUpdate = timeStamp))
        }
    }
}