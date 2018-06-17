package com.aisoftware.flexconnect.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.support.annotation.NonNull
import com.aisoftware.flexconnect.FlexConnectApplication
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.model.LastUpdate
import com.aisoftware.flexconnect.util.Logger

class LastUpdateViewModel(val app: Application, val dataRepository: DataRepository): AndroidViewModel(app) {

    private val TAG = LastUpdateViewModel::class.java.simpleName
    private var lastUpdate: LiveData<LastUpdate> = dataRepository.fetchLastUpdate()

    fun getLastUpdate(): LiveData<LastUpdate> {
        Logger.d(TAG, "Attempting to get deliveries with last update value: $lastUpdate")
        return lastUpdate
    }
}

class LastUpdateViewModelFactory(@param:NonNull
                               @field:NonNull
                               private val app: Application): ViewModelProvider.NewInstanceFactory() {

    private val dataRepo: DataRepository = (app as FlexConnectApplication).getRepository()

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LastUpdateViewModel(app, dataRepo) as T
    }
}