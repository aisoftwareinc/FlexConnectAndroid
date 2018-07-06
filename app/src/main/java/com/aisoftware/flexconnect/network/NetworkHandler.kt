package com.aisoftware.flexconnect.network

import com.aisoftware.flexconnect.network.request.NetworkRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.NetworkRequestRawResponseCallback


interface NetworkHandler {
    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback, requestCode: String?)

    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestRawResponseCallback, requestCode: String?)
}