package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.db.entity.Deliveries
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.request.DeliveriesRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory


class DeliveryViewModel(val app: Application) : AndroidViewModel(app) {

    private val TAG = DeliveryViewModel::class.java.simpleName
    private val dataRepository: DataRepository = (app as FlexConnectApplication).getRepository()
    private var deliveries: LiveData<List<DeliveryEntity>>? = null
    private val networkService: NetworkService = (app as FlexConnectApplication).getNetworkService()

    fun getDeliveries(phoneNumber: String): LiveData<List<DeliveryEntity>> {
        Log.d(TAG, "Attempting to get deliveries")
        deliveries = MutableLiveData<List<DeliveryEntity>>()
        loadDeliveries(phoneNumber)
        return deliveries!!
    }

    private fun loadDeliveries(phoneNumber: String) {
        if ((getApplication() as FlexConnectApplication).isNetworkAvailable()) {
            // make api request
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

                }

                override fun onComplete(requestCode: String?) {

                }
            }, "deliveriesRequestCode" )

            // update database
        }
        else {
            deliveries = dataRepository.loadAllDeliveries()
            Log.d(TAG, "Loaded deliveries from repo: $deliveries")
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
