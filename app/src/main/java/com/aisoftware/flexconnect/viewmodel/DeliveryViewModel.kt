package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.Deliveries
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.request.DeliveriesRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.experimental.launch


class DeliveryViewModel(val app: Application) : AndroidViewModel(app) {

    private val TAG = DeliveryViewModel::class.java.simpleName
    private val DELIVERIES_REQUEST_CODE = "deliveriesRequestCode"
    private val dataRepository: DataRepository = (app as FlexConnectApplication).getRepository()
    private var deliveries: LiveData<List<Delivery>> = MutableLiveData<List<Delivery>>()
    private val networkService: NetworkService = (app as FlexConnectApplication).getNetworkService()

    fun getDeliveries(phoneNumber: String): LiveData<List<Delivery>> {
        Log.d(TAG, "Attempting to get deliveries")
        loadDeliveries(phoneNumber)
        return deliveries
    }

    private fun loadDeliveries(phoneNumber: String) {
        if ((getApplication() as FlexConnectApplication).isNetworkAvailable()) {
            // make api request if network available
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
                                (deliveries as MutableLiveData).postValue(deliveriesResponse.deliveries)

                                launch {
                                    try {
                                        Log.d(TAG, "Retrieved new dataset, updating database")
                                        dataRepository.loadDeliveriesToSync(deliveriesResponse.deliveries)
                                    }
                                    catch(e: Exception) {
                                        Log.e(TAG, "Unable to update data repository", e)
                                    }
                                }
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
                    (deliveries as MutableLiveData).postValue(null)
                }

                override fun onComplete(requestCode: String?) { }
            }, DELIVERIES_REQUEST_CODE)
        }
        else {
            val delList = dataRepository.fetchAllDeliveries()
            (deliveries as MutableLiveData).postValue(delList.value)
            Log.d(TAG, "Loaded deliveries from repo: ${delList.value}")
        }
    }
}

//class Factory(@param:NonNull
//              @field:NonNull
//              private val mApplication: Application,
//              private val mProductId: Int) : ViewModelProvider.NewInstanceFactory() {
//
//    private val mRepository: DataRepository
//
//    init {
//        mRepository = (mApplication as BasicApp).getRepository()
//    }
//
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//
//        return ProductViewModel(mApplication, mRepository, mProductId) as T
//    }
//}
