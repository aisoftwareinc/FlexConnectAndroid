package com.aisoftware.flexconnect.db.dao

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import com.aisoftware.flexconnect.model.LastUpdate

@Dao
interface LastUpdateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(lastUpdate: LastUpdate)

    @Query("SELECT * FROM lastupdates")
    fun loadLastUpdate(): LiveData<LastUpdate>

    @Query("SELECT COUNT(*) FROM lastupdates")
    fun lastUpdateCount(): Int

    @Query("DELETE from lastupdates")
    fun deleteAll()
}