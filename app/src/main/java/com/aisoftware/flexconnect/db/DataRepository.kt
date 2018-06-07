package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.util.Logger

interface DataRepository {
    fun fetchDelivery(id: Int): LiveData<Delivery>
    fun fetchAllDeliveries(): LiveData<List<Delivery>>
    fun fetchDeliveriesCount(): Int
    fun loadDeliveries(deliveries: List<Delivery>)
    fun getDeliveries(): LiveData<List<Delivery>>
}

class DataRepositoryImpl private constructor(private val appDatabase: AppDatabase): DataRepository {

    private val TAG = DataRepository::class.java.simpleName
    private val observableDeliveries: MediatorLiveData<List<Delivery>> = MediatorLiveData()

    init {
        observableDeliveries.addSource(appDatabase.deliveryDao().loadAllDeliveries()
        ) { deliveryEntities ->
            if (appDatabase.databaseCreated.value != null) {
                observableDeliveries.postValue(deliveryEntities)
            }
        }
    }

    override fun getDeliveries(): LiveData<List<Delivery>> {
        return observableDeliveries
    }

    override fun fetchDelivery(id: Int): LiveData<Delivery> {
        return appDatabase.deliveryDao().loadDelivery(id)
    }

    override fun fetchAllDeliveries(): LiveData<List<Delivery>> {
        return observableDeliveries
    }

    override fun fetchDeliveriesCount(): Int {
        return appDatabase.deliveryDao().deliveriesCount()
    }

    override fun loadDeliveries(deliveries: List<Delivery>) {
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
                        INSTANCE = DataRepositoryImpl(database)
                    }
                }
            }
            return INSTANCE!!
        }
    }
}