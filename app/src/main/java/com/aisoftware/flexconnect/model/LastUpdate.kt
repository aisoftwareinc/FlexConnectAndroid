package com.aisoftware.flexconnect.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "lastupdates")
data class LastUpdate(
        @PrimaryKey(autoGenerate = true)
        var id: Long? = 0,
        @Json(name = "lastUpdate")
        var lastUpdate: String = "")