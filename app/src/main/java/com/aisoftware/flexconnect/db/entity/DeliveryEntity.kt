package com.aisoftware.flexconnect.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import com.squareup.moshi.Json
import java.io.Serializable

/*
deliveries": [
        {
            "GUID": "bf0ff286-be02-46c5-bb69-afc8462a7ec2",
            "Status": "En Route",
            "Date": "5/10/2018",
            "Time": "ASAP",
            "CustomerName": "Chase Field",
            "CustomerEmail": "ed@aisoftware.us",
            "CustomerPhone": "6026391168",
            "Address": "401 E Jefferson St",
            "AddressCont": "",
            "City": "Phoenix",
            "State": "AZ",
            "Zip": "85004",
            "Latitude": "33.4453626",
            "Longitude": "-112.0674961",
            "Distance": "22 mins",
            "Miles": "15 miles",
            "Comments": "Here is a sample delivery to Chase Field downtown."
        },
 */

data class Deliveries(val deliveries: List<DeliveryEntity>)

@Entity(tableName = "deliveries")
data class DeliveryEntity (
        @PrimaryKey(autoGenerate = true)
        var id: Long?,
        @Json(name = "GUID")
        var guid: String = "",
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
        var comments: String = ""): Serializable {

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
            "")
}