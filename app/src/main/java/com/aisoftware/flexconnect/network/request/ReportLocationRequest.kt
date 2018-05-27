package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import com.aisoftware.flexconnect.util.Constants
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

class ReportLocationRequest(val phoneNumber: String,
                            val latitude: String,
                            val longitude: String): NetworkRequestString() {

    override fun getRequestTag(): String = this.javaClass.simpleName

    override fun getRequestBody(): RequestBody {
        val bodyValue = "Phone=$phoneNumber&Latitude=$latitude&Longitude=$longitude&UserAgent=${Constants.USER_AGENT}&APIKey=${Constants.API_KEY}"
        val body = RequestBody.create(MediaType.parse(Constants.MEDIA_TYPE_FORM), bodyValue)
        return body
    }

    override fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody> =
            apiEndpoints.postReportLocation(getRequestHeaders(), getRequestBody())
}