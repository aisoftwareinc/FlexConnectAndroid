package com.aisoftware.flexconnect.model

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import java.io.Serializable

data class Deliveries(val deliveries: List<Delivery>)

@Entity(tableName = "deliveries")
data class Delivery(
        @PrimaryKey(autoGenerate = true)
        var id: Long?,
        @Json(name = "GUID")
        var guid: String = "",
        @Json(name = "TimerInterval")
        var interval: String = "",
        @Json(name = "Status")
        var status: String = "",
        @Json(name = "Date")
        var date: String ="",
        @Json(name = "Time")
        var time: String = "",
        @Json(name = "CustomerName")
        var customerName: String = "",
        @Json(name = "CustomerEmail")
        var customerEmail: String ="",
        @Json(name = "CustomerPhone")
        var customerPhone: String = "",
        @Json(name = "Address")
        var address: String = "",
        @Json(name = "AddressCont")
        var addressCont: String = "",
        @Json(name = "City")
        var city: String = "",
        @Json(name = "State")
        var state: String = "",
        @Json(name = "Zip")
        var zip: String = "",
        @Json(name = "Latitude")
        var latitude: String ="",
        @Json(name = "Longitude")
        var longitude: String = "",
        @Json(name = "Distance")
        var distance: String = "",
        @Json(name = "Miles")
        var miles: String = "",
        @Json(name = "Comments")
        var comments: String = "",
        @Json(name = "IsEnRoute")
        var isenroute: Boolean = false): Serializable {

    constructor(): this(
            null,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            false)
}