package com.aisoftware.flexconnect.db

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context
import android.support.annotation.VisibleForTesting
import com.aisoftware.flexconnect.AppExecutors
import com.aisoftware.flexconnect.db.dao.DeliveryDao
import com.aisoftware.flexconnect.db.dao.LastUpdateDao
import com.aisoftware.flexconnect.db.dao.PhoneNumberDao
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.model.LastUpdate
import com.aisoftware.flexconnect.model.PhoneNumber
import com.aisoftware.flexconnect.util.Logger


@Database(entities = arrayOf(Delivery::class, LastUpdate::class, PhoneNumber::class), version = 1)
abstract class AppDatabase : RoomDatabase() {

    private val TAG = AppDatabase::class.java.simpleName
    private val isDatabaseCreated = MutableLiveData<Boolean>()

    val databaseCreated: LiveData<Boolean>
        get() = isDatabaseCreated

    abstract fun deliveryDao(): DeliveryDao
    abstract fun lastUpdateDao(): LastUpdateDao
    abstract fun phoneNumberDao(): PhoneNumberDao

    private fun updateDatabaseCreated(context: Context) {
        if (context.getDatabasePath(DATABASE_NAME).exists()) {
            setDatabaseCreated()
        }
    }

    private fun setDatabaseCreated() {
        isDatabaseCreated.postValue(true)
        Logger.d(TAG, "Database set to created, is open: ${INSTANCE?.isOpen}")
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
                                val database = AppDatabase.getInstance(appContext, executors)
                                database.setDatabaseCreated()
                            }
                        }
                    })
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }

        fun destroyInstance() {
            INSTANCE = null
        }

        fun insertData(database: AppDatabase, deliveries: List<Delivery>) {
            database.runInTransaction {
                Logger.d(TAG, "Attempting to insert deliveries: ${deliveries.size}")
                database.deliveryDao().insertAll(deliveries)
                Logger.d(TAG, "Inserted deliveries: ${deliveries.size}")
            }
        }
    }
}