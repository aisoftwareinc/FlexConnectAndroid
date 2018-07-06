package com.aisoftware.flexconnect.model

enum class EnRouteState(val id: Int, val state: String) {
    PENDING(1, "Pending"),
    ENROUTE(2, "En Route"),
    DELIVERED(3, "Delivered")
}