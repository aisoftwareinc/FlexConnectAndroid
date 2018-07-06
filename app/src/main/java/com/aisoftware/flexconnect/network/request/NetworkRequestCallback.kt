package com.aisoftware.flexconnect.network.request

interface NetworkRequestCallback {
    fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?)
    fun onFailure(data: String?, requestCode: String?)
    fun onComplete(requestCode: String?)
}