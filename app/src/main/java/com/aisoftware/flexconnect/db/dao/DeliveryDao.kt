package com.aisoftware.flexconnect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import com.aisoftware.flexconnect.db.entity.DeliveryEntity



@Dao
interface DeliveryDao {

    @Query("SELECT * FROM deliveries")
    fun loadAllDeliveries(): LiveData<List<DeliveryEntity>>

    @Query("select * from deliveries where id = :deliveryId")
    fun loadDelivery(deliveryId: Int): LiveData<DeliveryEntity>

    @Insert(onConflict = REPLACE)
    fun insert(deliveryEntity: DeliveryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(products: List<DeliveryEntity>)

    @Query("DELETE from deliveries")
    fun deleteAll()
}