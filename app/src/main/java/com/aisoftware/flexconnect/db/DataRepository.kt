package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.model.LastUpdate
import com.aisoftware.flexconnect.util.Logger

interface DataRepository {
    fun fetchDelivery(id: Int): LiveData<Delivery>
    fun fetchAllDeliveries(): LiveData<List<Delivery>>
    fun fetchDeliveriesCount(): Int
    fun loadDeliveries(deliveries: List<Delivery>)
    fun getDeliveries(): LiveData<List<Delivery>>
    fun loadLastUpdate(lastUpdate: LastUpdate)
    fun fetchLastUpdate(): LiveData<LastUpdate>
}

class DataRepositoryImpl private constructor(private val appDatabase: AppDatabase) : DataRepository {

    private val TAG = DataRepository::class.java.simpleName
    private val observableDeliveries: MediatorLiveData<List<Delivery>> = MediatorLiveData()
    private val observableLastUpdate: MediatorLiveData<LastUpdate> = MediatorLiveData()

    init {
        observableDeliveries.addSource(appDatabase.deliveryDao().loadAllDeliveries()
        ) { deliveryEntities ->
            if (appDatabase.databaseCreated.value != null) {
                observableDeliveries.postValue(deliveryEntities)
            }
        }

        observableLastUpdate.addSource(appDatabase.lastUpdateDao().loadLastUpdate()) {
            lastUpdate ->
            if( appDatabase.databaseCreated.value != null ) {
                observableLastUpdate.postValue(lastUpdate)
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
            if (fetchDeliveriesCount() == 0) {
                observableDeliveries.postValue(ArrayList<Delivery>())
            }
        }
    }

    override fun loadLastUpdate(lastUpdate: LastUpdate) {
        lastUpdate.let {
            Logger.d(TAG, "Attempting to insert last update into database: $lastUpdate")
            appDatabase.lastUpdateDao().deleteAll()
            appDatabase.lastUpdateDao().insert(lastUpdate)

            val count = appDatabase.lastUpdateDao().lastUpdateCount()
            Logger.d(TAG, "Loaded lastupdate count: $count")
            if( count == 0 ) {
                val lastUpdate = LastUpdate(lastUpdate = "0")
                observableLastUpdate.postValue(lastUpdate)
            }
        }
    }

    override fun fetchLastUpdate(): LiveData<LastUpdate> {
        return appDatabase.lastUpdateDao().loadLastUpdate()
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