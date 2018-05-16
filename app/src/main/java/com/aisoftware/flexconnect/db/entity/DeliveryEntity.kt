package com.aisoftware.flexconnect.db.entity

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.PrimaryKey
import com.aisoftware.flexconnect.model.Delivery
import java.io.Serializable

@Entity(tableName = "deliveries")
data class DeliveryEntity (
        @PrimaryKey(autoGenerate = true)
        var id: Long?,
        var name: String = "",
        var address1: String = "",
        var address2: String = "",
        var address3: String = "",
        var city: String = "",
        var state: String = "",
        var zip: String = "",
        var zip4: String = "",
        var phone1: String = "",
        var phone2: String = "",
        var status: String = "",
        var eta: String = "",
        var time: String = "",
        var distance: String = "",
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
            "")

    @Ignore
    constructor(delivery: Delivery) : this(
            delivery.getId(),
            delivery.getName(),
            delivery.getAddress1(),
            delivery.getAddress2(),
            delivery.getAddress3(),
            delivery.getCity(),
            delivery.getState(),
            delivery.getZip(),
            delivery.getZip4(),
            delivery.getPhone1(),
            delivery.getPhone2(),
            delivery.getStatus(),
            delivery.getEta(),
            delivery.getTime(),
            delivery.getDistance(),
            delivery.getComments()
    )
}