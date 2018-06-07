package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import com.aisoftware.flexconnect.util.Constants
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

class EnRouteRequest(val phoneNumber: String, val guid: String): NetworkRequestString() {

    override fun getRequestTag(): String = this.javaClass.simpleName

    override fun getRequestBody(): RequestBody {
        val bodyValue = "Phone=$phoneNumber&GUID=$guid&APIKey=${Constants.API_KEY}"
        val body = RequestBody.create(MediaType.parse(Constants.MEDIA_TYPE_FORM), bodyValue)
        return body
    }

    override fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody> =
            apiEndpoints.postEnRoute(getRequestHeaders(), getRequestBody())
}