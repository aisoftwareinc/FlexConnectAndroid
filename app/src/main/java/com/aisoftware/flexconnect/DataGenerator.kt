package com.aisoftware.flexconnect

import com.aisoftware.flexconnect.model.Delivery

class DataGenerator {

    fun getDeliveries(): List<Delivery> {

        val deliveryList = mutableListOf<Delivery>()

        val d1 = Delivery(1L, "Chase Field")
        with (d1) {
            address = "401 E Jefferson St"
            city = "Phoenix"
            state = "AZ"
            zip = "85004"
            status = "En Route"
            time = "ASAP"
            distance = "15 miles"
            customerPhone = "6026391168"
            comments = "Here is a sample delivery to Chase Field downtown."
        }
        deliveryList.add(d1)

        val d2 = Delivery(2L, "FlexConnect Office")
        with (d2) {
            address = "17470 N Pacesetter Way"
            city = "Scottsdate"
            state = "AZ"
            zip = "85255"
            status = "En Route"
            time = "Anytime Before 5 PM"
            distance = "15 miles"
            customerPhone = "6026391168"
            comments = "These guys are drunks."
        }
        deliveryList.add(d2)

        return deliveryList
    }
}