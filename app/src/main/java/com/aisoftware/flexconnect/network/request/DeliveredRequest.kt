package com.aisoftware.flexconnect.network.request

import com.aisoftware.flexconnect.network.ApiEndpoints
import com.aisoftware.flexconnect.util.Constants
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call

class DeliveredRequest(val phoneNumber: String,
                       val guid: String,
                       val comments: String? = "",
                       val photoString: String? = ""): NetworkRequestString() {

    private val STATUS = "3"

    override fun getRequestTag(): String = this.javaClass.simpleName

    override fun getRequestBody(): RequestBody {
        //Phone=string&varStatus=string&Photo=string&Comments=string&GUID=string&APIKey=string
        val bodyValue = "Phone=$phoneNumber&Status=$STATUS&GUID=$guid&Comments=$comments&Photo=$photoString&APIKey=${Constants.API_KEY}"
        val body = RequestBody.create(MediaType.parse(Constants.MEDIA_TYPE_FORM), bodyValue)
        return body
    }

    override fun getRequestEndpoint(apiEndpoints: ApiEndpoints): Call<ResponseBody> =
            apiEndpoints.postDelivered(getRequestHeaders(), getRequestBody())
}