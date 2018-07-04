package com.aisoftware.flexconnect

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import com.aisoftware.flexconnect.db.AppDatabase
import com.aisoftware.flexconnect.db.DataRepository
import com.aisoftware.flexconnect.db.DataRepositoryImpl
import com.aisoftware.flexconnect.network.NetworkService
import com.aisoftware.flexconnect.network.NetworkServiceDefault
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.DeviceInfo
import com.aisoftware.flexconnect.util.Logger
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.Answers
import io.fabric.sdk.android.Fabric
import java.util.concurrent.Executors


class FlexConnectApplication: Application() {

    private val TAG = FlexConnectApplication::class.java.simpleName
    private val THREAD_COUNT = 3
    private lateinit var appExecutors: AppExecutors
    private lateinit var appDatabase: AppDatabase
    private lateinit var dataRepository: DataRepository
    private lateinit var networkService: NetworkService
    private lateinit var deviceInfo: DeviceInfo
    private lateinit var appVersionName: String
    private var appVersionCode: Int = 0
    private lateinit var appPackage: String
    var enRouteCount = 0

    override fun onCreate() {
        super.onCreate()

        if (! Fabric.isInitialized() ) {
            Fabric.with(this, Crashlytics())
            Fabric.with(this, Answers())
        }

        try {
            Logger.d(TAG, "Attempting to create app executors...")
            appExecutors = AppExecutors(Executors.newSingleThreadExecutor(),
                    Executors.newFixedThreadPool(THREAD_COUNT),
                    MainThreadExecutor())
            Logger.d(TAG, "Successfully created app executors:  $appExecutors")

            Logger.d(TAG, "Attempting to create app database instance...")
            appDatabase = AppDatabase.getInstance(this, appExecutors)
            Logger.d(TAG, "Successfully created app database instance: $appDatabase")

            Logger.d(TAG, "Attempting to create data repository...")
            dataRepository = DataRepositoryImpl.getInstance(appDatabase)
            Logger.d(TAG, "Successfully created data repository: $dataRepository")

            Logger.d(TAG, "Attempting to create network service...")
            networkService = NetworkServiceDefault.Builder().build()
            Logger.d(TAG, "Successfully created network service: $networkService")

            deviceInfo = DeviceInfo(this)

            appVersionName = applicationContext
                    .packageManager
                    .getPackageInfo(packageName,PackageManager.GET_ACTIVITIES)
                    .versionName

            appVersionCode = applicationContext
                    .packageManager
                    .getPackageInfo(packageName,PackageManager.GET_ACTIVITIES)
                    .versionCode

            appPackage = applicationContext.packageName

            initalizeCrashlytics()
        }
        catch(e: Exception) {
            Logger.e(TAG, "Unable to create persistence layer: ", e)
        }
    }

    private fun initalizeCrashlytics() {
        CrashLogger.initialize(this)
        CrashLogger.setBool("DEV_BUILD", BuildConfig.DEBUG)
        CrashLogger.setString("DEVICE_MANUFACTURER", deviceInfo.deviceManufacture)
        CrashLogger.setString("DEVICE_MODEL", deviceInfo.model)
        CrashLogger.setString("DEVICE_OS_NAME", deviceInfo.osBuild)
        CrashLogger.setString("DEVICE_OS_VERSION", deviceInfo.osVersion)
        CrashLogger.setInt("DEVICE_SDK_VERSION", deviceInfo.deviceAPI)
        CrashLogger.setString("DEVICE_LOCALE", deviceInfo.deviceLocale)
        CrashLogger.setInt("BUILD_VERSION_CODE", appVersionCode)
        CrashLogger.setString("BUILD_VERSION_NAME", appVersionName)
        CrashLogger.setString("BUILD_PACKAGE", appPackage)
    }

    fun getAppDatabase(): AppDatabase = appDatabase

    fun getRepository(): DataRepository = dataRepository

    fun getNetworkService(): NetworkService = networkService

    fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null
    }
}