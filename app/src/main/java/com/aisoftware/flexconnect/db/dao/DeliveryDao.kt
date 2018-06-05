package com.aisoftware.flexconnect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.aisoftware.flexconnect.model.Delivery



@Dao
interface DeliveryDao {

    @Query("SELECT * FROM deliveries")
    fun loadAllDeliveries(): LiveData<List<Delivery>>

    @Query("select * from deliveries where id = :deliveryId")
    fun loadDelivery(deliveryId: Int): LiveData<Delivery>

    @Insert(onConflict = REPLACE)
    fun insert(delivery: Delivery)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(deliveryList: List<Delivery>)

    @Query("DELETE from deliveries")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM deliveries")
    fun deliveriesCount(): Int
}