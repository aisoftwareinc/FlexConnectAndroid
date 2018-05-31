package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.support.annotation.VisibleForTesting
import android.util.Log
import com.aisoftware.flexconnect.AppExecutors
import com.aisoftware.flexconnect.db.dao.DeliveryDao
import com.aisoftware.flexconnect.model.Delivery


@Database(entities = arrayOf(Delivery::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    private val TAG = AppDatabase::class.java.simpleName
    private val isDatabaseCreated = MutableLiveData<Boolean>()

    val databaseCreated: LiveData<Boolean>
        get() = isDatabaseCreated

    abstract fun deliveryDao(): DeliveryDao

    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated()
        }
    }

    private fun setDatabaseCreated() {
        isDatabaseCreated.postValue(true)
        Log.d(TAG, "Database set to created")
    }

    companion object {
        private val TAG = AppDatabase::class.java.simpleName
        private var INSTANCE: AppDatabase? = null

        @VisibleForTesting
        val DATABASE_NAME = "flexconnect.db"

        fun getInstance(context: Context, executors: AppExecutors): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = buildDatabase(context.applicationContext, executors)
                        INSTANCE!!.updateDatabaseCreated(context.applicationContext)
                    }
                }
            }
            return INSTANCE!!
        }

        private fun buildDatabase(appContext: Context, executors: AppExecutors): AppDatabase {
            return Room.databaseBuilder(appContext, AppDatabase::class.java, DATABASE_NAME)
                    .addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            executors.diskIO().execute {
                                // Generate the data for pre-population
                                val database = AppDatabase.getInstance(appContext, executors)
//                                val dataGenerator = DataGenerator()
//                                val deliveries = dataGenerator.getDeliveries()
//                                insertData(database, deliveries)
                                // notify that the database was created and it's ready to be used
                                database.setDatabaseCreated()
                            }
                        }
                    }).build()
        }

        fun insertData(database: AppDatabase, deliveries: List<Delivery>) {
            database.runInTransaction {
                Log.d(TAG, "Attempting to insert deliveries: ${deliveries.size}")
                database.deliveryDao().insertAll(deliveries)
                Log.d(TAG, "Inserted deliveries: ${deliveries.size}")
            }
        }
    }
}