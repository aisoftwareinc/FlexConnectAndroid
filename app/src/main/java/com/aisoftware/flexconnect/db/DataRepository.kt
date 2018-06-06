package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.util.Logger

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
        return deliveries
    }

    fun fetchDeliveriesCount(): Int {
        return appDatabase.deliveryDao().deliveriesCount()
    }

    fun loadDeliveries(deliveries: List<Delivery>) {
        val deliveryList = deliveries
        deliveryList?.let {
            Logger.d(TAG, "Attempting to insert deliveries list into database: ${deliveries}")
            appDatabase.deliveryDao().deleteAll()
            appDatabase.deliveryDao().insertAll(deliveryList)

            Logger.d(TAG, "Loaded update deliveries count: ${fetchDeliveriesCount()}")
            if( fetchDeliveriesCount() == 0 ) {
                observableDeliveries.postValue(ArrayList<Delivery>())
            }
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