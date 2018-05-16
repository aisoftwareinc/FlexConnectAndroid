package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.util.Log
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.db.entity.DeliveryEntity


class DeliveryViewModel(val app: Application) : AndroidViewModel(app) {

    private val TAG = DeliveryViewModel::class.java.simpleName
    private var dataRepository: DataRepository = (app as FlexConnectApplication).getRepository()
    private var deliveries: LiveData<List<DeliveryEntity>>? = null

    fun getDeliveries(): LiveData<List<DeliveryEntity>> {
        Log.d(TAG, "Attempting to get deliveries")
        if (deliveries == null) {
            deliveries = MutableLiveData<List<DeliveryEntity>>()
            loadDeliveries()
        }
        return deliveries!!
    }

    private fun loadDeliveries() {
//        if ((getApplication() as FlexConnectApplication).isNetworkAvailable()) {

            // make api request
            // update database
//        }
//        else {
            deliveries = dataRepository.loadAllDeliveries()
            Log.d(TAG, "Loaded deliveries from repo: $deliveries")
//        }
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
