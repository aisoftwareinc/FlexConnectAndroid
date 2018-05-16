package com.aisoftware.flexconnect.model

interface Delivery {
    fun getId(): Long
    fun getName(): String
    fun getAddress1(): String
    fun getAddress2(): String
    fun getAddress3(): String
    fun getCity(): String
    fun getState(): String
    fun getZip(): String
    fun getZip4(): String
    fun getPhone1(): String
    fun getPhone2(): String
    fun getStatus(): String
    fun getEta(): String
    fun getTime(): String
    fun getDistance(): String
    fun getComments(): String
}
