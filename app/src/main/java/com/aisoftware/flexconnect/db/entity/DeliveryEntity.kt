package com.aisoftware.flexconnect.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
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
        var guid: String = "",
        var status: String = "",
        var date: String ="",
        var time: String = "",
        var customername: String = "",
        var customeremail: String ="",
        var customerphone: String = "",
        var address: String = "",
        var addresscont: String = "",
        var city: String = "",
        var state: String = "",
        var zip: String = "",
        var latitude: String ="",
        var longitude: String = "",
        var distance: String = "",
        var miles: String = "",
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