package com.aisoftware.flexconnect

import com.aisoftware.flexconnect.db.entity.DeliveryEntity

class DataGenerator {

    fun getDeliveries(): List<DeliveryEntity> {

        val deliveryList = mutableListOf<DeliveryEntity>()

        val d1 = DeliveryEntity(1L, "Chase Field")
        with (d1) {
            address1 = "401 E Jefferson St"
            city = "Phoenix"
            state = "AZ"
            zip = "85004"
            status = "En Route"
            time = "ASAP"
            eta = "22 mins"
            distance = "15 miles"
            phone1 = "6026391168"
            comments = "Here is a sample delivery to Chase Field downtown."
        }
        deliveryList.add(d1)

        val d2 = DeliveryEntity(2L, "FlexConnect Office")
        with (d2) {
            address1 = "17470 N Pacesetter Way"
            city = "Scottsdate"
            state = "AZ"
            zip = "85255"
            status = "En Route"
            time = "Anytime Before 5 PM"
            eta = "19 mins"
            distance = "15 miles"
            phone1 = "6026391168"
            comments = "These guys are drunks."
        }
        deliveryList.add(d2)

        val d3 = DeliveryEntity(3L, "American Express")
        with(d3) {
            address1 = "19640 N 31st Ave"
            address2 = "Ste 1"
            city = "Phoenix"
            state = "AZ"
            zip = "85027"
            zip4 = "1234"
            status = "En Route"
            time = "Anytime Before 5 PM"
            eta = "19 mins"
            distance = "15 miles"
            phone1 = "6026391168"
            comments = "These guys are drunks."
        }
        deliveryList.add(d3)

        return deliveryList
    }
}