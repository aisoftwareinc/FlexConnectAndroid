package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import com.aisoftware.flexconnect.util.Constants
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

class UpdateStatusRequest(val phoneNumber: String, val status: Int, val guid: String): NetworkRequestString() {

    override fun getRequestTag(): String = this.javaClass.simpleName

    override fun getRequestBody(): RequestBody {
        val bodyValue = "Phone=$phoneNumber&varStatus=${status}&GUID=${guid}&APIKey=${Constants.API_KEY}"
        val body = RequestBody.create(MediaType.parse(Constants.MEDIA_TYPE_FORM), bodyValue)
        return body
    }

    override fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody> =
            apiEndpoints.postUpdateStatus(getRequestHeaders(), getRequestBody())
}