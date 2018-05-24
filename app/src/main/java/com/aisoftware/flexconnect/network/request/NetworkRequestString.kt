package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.Constants.BASE_URL
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import java.lang.reflect.Type

abstract class NetworkRequestString: NetworkRequest, Type {

    override fun getRequestHeaders(): Map<String, String> {
        val headers = HashMap<String, String>()
        headers["Host"] = Constants.HOST
        headers["Content-Type"] = Constants.MEDIA_TYPE_FORM
        return headers
    }

    override fun getBaseUrl(): String = BASE_URL

    abstract override fun getRequestTag(): String

    abstract override fun getRequestBody(): RequestBody

    abstract override fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody>
}