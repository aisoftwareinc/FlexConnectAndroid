package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.annotation.NonNull
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.Deliveries
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.request.DeliveriesRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.experimental.launch


class DeliveryViewModel(val app: Application, val dataRepository: DataRepository, val networkService: NetworkService) : AndroidViewModel(app) {

    private val TAG = DeliveryViewModel::class.java.simpleName
    private val DELIVERIES_REQUEST_CODE = "deliveriesRequestCode"
    private var deliveries: LiveData<List<Delivery>> = dataRepository.getDeliveries()

    fun getDeliveries(phoneNumber: String, refreshList: Boolean): LiveData<List<Delivery>> {
        Logger.d(TAG, "Attempting to get deliveries with dataRepository: $dataRepository")

        if( refreshList ) {
            loadNetowrkDeliveries(phoneNumber)
        }
        else {
            loadPersistedDeliveries()
        }

        return deliveries
    }

    private fun loadNetowrkDeliveries(phoneNumber: String) {
        if ( (app as FlexConnectApplication).isNetworkAvailable() ) {
            val request = DeliveriesRequest(phoneNumber)
            networkService.startRequest(request, object: NetworkRequestCallback {
                override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
                    if( data != null ) {
                        try {
                            val moshi = Moshi.Builder()
                                    .add(KotlinJsonAdapterFactory())
                                    .build()
                            val adapter = moshi.adapter(Deliveries::class.java)
                            val deliveriesResponse = adapter.fromJson(data)
                            if (deliveriesResponse != null ) {
                                launch {
                                    try {
                                        Logger.d(TAG, "Retrieved new dataset, updating database")
                                        dataRepository.loadDeliveries(deliveriesResponse.deliveries)
                                    }
                                    catch(e: Exception) {
                                        Logger.e(TAG, "Unable to update data repository", e)
                                        CrashLogger.logException(1, TAG, "Unable to update data repository", e)
                                    }
                                }
                            }
                        }
                        catch(e: Exception) {
                            Logger.e(TAG, "Unable to process data response: $data", e)
                            onFailure(data, requestCode)
                        }
                    }
                    else {
                        Logger.d(TAG, "Received a failure data response: $data")
                        onFailure(data, requestCode)
                    }
                }

                override fun onFailure(data: String?, requestCode: String?) {
                    (deliveries as MutableLiveData).postValue(null)
                }

                override fun onComplete(requestCode: String?) { }
            }, DELIVERIES_REQUEST_CODE)
        }
        else {
            Logger.d(TAG,"Network not available, loading from repo")
            loadPersistedDeliveries()
        }
    }

    private fun loadPersistedDeliveries() {
        val delList = dataRepository.fetchAllDeliveries()
        Logger.d(TAG, "Loaded deliveries from repo: ${delList.value}")
        (deliveries as MutableLiveData).postValue(delList.value)
    }
}

class DeliveryViewModelFactory(@param:NonNull
              @field:NonNull
              private val app: Application,
              private val networkService: NetworkService):ViewModelProvider.NewInstanceFactory() {

    private val dataRepo: DataRepository = (app as FlexConnectApplication).getRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DeliveryViewModel(app, dataRepo, networkService) as T
    }
}
