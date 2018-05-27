package com.aisoftware.flexconnect

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import java.util.concurrent.Executors



class FlexConnectApplication: Application() {

    private val TAG = FlexConnectApplication::class.java.simpleName
    private val THREAD_COUNT = 3
    private lateinit var appExecutors: AppExecutors
    private lateinit var appDatabase: AppDatabase
    private lateinit var dataRepository: DataRepository
    private lateinit var networkService: NetworkService

    override fun onCreate() {
        super.onCreate()

        //        if (! Fabric.isInitialized() ) {
//            Fabric.with(this, Crashlytics())
//        }

        try {
            Log.d(TAG, "Attempting to create app executors...")
            appExecutors = AppExecutors(Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(THREAD_COUNT),
                    MainThreadExecutor())
            Log.d(TAG, "Successfully created app executors:  $appExecutors")

            Log.d(TAG, "Attempting to create app database instance...")
            appDatabase = AppDatabase.getInstance(this, appExecutors)
            Log.d(TAG, "Successfully created app database instance: $appDatabase")

            Log.d(TAG, "Attempting to create data repository...")
            dataRepository = DataRepository.getInstance(appDatabase)
            Log.d(TAG, "Successfully created data repository: $dataRepository")

            Log.d(TAG, "Attempting to create network service...")
            networkService = NetworkServiceDefault.Builder().build()
            Log.d(TAG, "Successfully created network service: $networkService")
        }
        catch(e: Exception) {
            Log.e(TAG, "Unable to create persistence layer: ", e)
        }
    }

    fun getAppDatabase(): AppDatabase? {
        return appDatabase
    }

    fun getRepository(): DataRepository {
        return dataRepository
    }

    fun getNetworkService(): NetworkService {
        return networkService
    }

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }
}