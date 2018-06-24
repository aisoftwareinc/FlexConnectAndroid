package com.aisoftware.flexconnect.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(tableName = "phonenumber")
class PhoneNumber(
        @PrimaryKey( autoGenerate = true)
        var id: Long? = 0,
        @Json(name = "phoneNumber")
        var phoneNumber: String = "" )