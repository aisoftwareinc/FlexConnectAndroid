package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import com.aisoftware.flexconnect.db.entity.DeliveryEntity

class DataRepository private constructor(private val appDatabase: AppDatabase) {

    private val observableDeliveries: MediatorLiveData<List<DeliveryEntity>> = MediatorLiveData()

    val deliveries: LiveData<List<DeliveryEntity>>
        get() = observableDeliveries

    init {
        observableDeliveries.addSource(appDatabase.deliveryDao().loadAllDeliveries()
        ) { deliveryEntities ->
            if (appDatabase.databaseCreated.value != null) {
                observableDeliveries.postValue(deliveryEntities)
            }
        }
    }

    fun loadDelivery(id: Int): LiveData<DeliveryEntity> {
        return appDatabase.deliveryDao().loadDelivery(id)
    }

    fun loadAllDeliveries(): LiveData<List<DeliveryEntity>> {
        return appDatabase.deliveryDao().loadAllDeliveries()
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