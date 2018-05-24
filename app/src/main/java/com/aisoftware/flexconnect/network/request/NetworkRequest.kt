package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

interface NetworkRequest {
    fun getRequestHeaders(): Map<String, String>
    fun getBaseUrl(): String
    fun getRequestTag(): String
    fun getRequestBody(): RequestBody
    fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody>
}