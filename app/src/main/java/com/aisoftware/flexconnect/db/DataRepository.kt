package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.util.Log
import com.aisoftware.flexconnect.model.Delivery

class DataRepository private constructor(private val appDatabase: AppDatabase) {

    private val TAG = DataRepository::class.java.simpleName
    private val observableDeliveries: MediatorLiveData<List<Delivery>> = MediatorLiveData()

    val deliveries: LiveData<List<Delivery>>
        get() = observableDeliveries

    init {
        observableDeliveries.addSource(appDatabase.deliveryDao().loadAllDeliveries()
        ) { deliveryEntities ->
            if (appDatabase.databaseCreated.value != null) {
                observableDeliveries.postValue(deliveryEntities)
            }
        }
    }

    fun fetchDelivery(id: Int): LiveData<Delivery> {
        return appDatabase.deliveryDao().loadDelivery(id)
    }

    fun fetchAllDeliveries(): LiveData<List<Delivery>> {
        return appDatabase.deliveryDao().loadAllDeliveries()
    }

    fun fetchDeliveriesCount(): Int {
        return appDatabase.deliveryDao().deliveriesCount()
    }

    fun loadDeliveries(deliveries: List<Delivery>) {
        val deliveryList = deliveries
        deliveryList?.let {
            Log.d(TAG, "Attempting to insert deliveries list into database: ${deliveries}")
            appDatabase.deliveryDao().deleteAll()
            appDatabase.deliveryDao().insertAll(deliveryList)
            observableDeliveries.postValue(deliveryList)
            Log.d(TAG, "Loaded update deliveries count: ${appDatabase.deliveryDao().deliveriesCount()}")
        }
    }

    fun loadDeliveriesToSync(deliveries: List<Delivery>) {
        val deliveryList = deliveries
        deliveryList?.let {
            Log.d(TAG, "Attempting to insert deliveries list into database: ${deliveries}")
            appDatabase.deliveryDao().deleteAll()
            appDatabase.deliveryDao().insertAll(deliveryList)
            Log.d(TAG, "Loaded update deliveries count: ${appDatabase.deliveryDao().deliveriesCount()}")
        }
    }

    companion object {
        private var INSTANCE: DataRepository? = null

        fun getInstance(database: AppDatabase): DataRepository {
            if (INSTANCE == null) {
                synchronized(DataRepository::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = DataRepository(database)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}