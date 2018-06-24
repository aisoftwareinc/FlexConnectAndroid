package com.aisoftware.flexconnect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.aisoftware.flexconnect.model.PhoneNumber

@Dao
interface PhoneNumberDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(phoneNumber: PhoneNumber)

    @Query("SELECT * FROM phonenumber")
    fun fetch(): LiveData<PhoneNumber>
}