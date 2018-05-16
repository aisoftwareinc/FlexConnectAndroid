package com.aisoftware.flexconnect.network

interface NetworkResponseCallback {
    fun onSuccess(message: String)
    fun onFailure(message: String)
}