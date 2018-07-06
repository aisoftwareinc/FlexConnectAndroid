package com.aisoftware.flexconnect.network

import com.aisoftware.flexconnect.network.request.NetworkRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.NetworkRequestFactory
import com.aisoftware.flexconnect.network.request.NetworkRequestRawResponseCallback
import com.aisoftware.flexconnect.util.Logger
import com.google.common.io.ByteStreams
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class RequestHandler( private val okHttpClient: OkHttpClient): NetworkHandler {

    private val TAG = RequestHandler::class.java.simpleName
    private val DEFAULT_ERROR_BODY = "{\"errorCode\":\"9999\"}"

    @Throws(NetworkRequestException::class)
    override fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestRawResponseCallback, requestCode: String?){
        request(networkRequest, null, callback, requestCode)
    }

    @Throws(NetworkRequestException::class)
    override fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback, requestCode: String?) {
        request(networkRequest, callback, null, requestCode)
    }

    fun request(networkRequest: NetworkRequest, callback: NetworkRequestCallback?, callbackRaw: NetworkRequestRawResponseCallback?, requestCode: String?){
        val call = networkRequest.getRequestEndpoint(getRetrofit(okHttpClient, networkRequest.getBaseUrl()).create(ApiEndpoints::class.java))

        if(call == null){
            Logger.d(TAG, "Failed Request: " + networkRequest.getRequestTag())
            callback?.onFailure("Unable to build request", requestCode)
            callbackRaw?.onFailure("Unable to build request", requestCode)
            callback?.onComplete(requestCode)
            callbackRaw?.onComplete(requestCode)
            return
        }

        Logger.d(TAG, "Executing Request: " + networkRequest.getRequestTag())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>?) {
                printCall(call)
                try {
                    if (response != null) {
                        Logger.d(TAG, " Response Http Code: ${response.code()} Status Message: ${response.message()}" )
                        if (response.isSuccessful) {
                            val headers = response.headers().toMultimap()
                            callbackRaw?.onSuccess(response.body(), headers, requestCode)
                            val body = response.body()?.string()//body.string() will clear response buffer
                            Logger.d(TAG, " Response Body: $body")
                            callback?.onSuccess(body, headers, requestCode)
                        }
                        else {
                            var errorBody = DEFAULT_ERROR_BODY
                            if (response.errorBody() != null) {
                                try {
                                    val eb = response.errorBody()
                                    if( eb != null) {
                                        errorBody = String(ByteStreams.toByteArray(eb.byteStream()))
                                    }
                                }
                                catch (e: Exception) {
                                    Logger.e(TAG, "Unable to create data string", e)
                                }
                            }

                            Logger.d(TAG, " Response Body: $errorBody")
                            callback?.onFailure(errorBody, requestCode)
                            callbackRaw?.onFailure(errorBody, requestCode)
                        }
                    }
                    else {
                        Logger.d(TAG, " Response object was null")
                        callback?.onFailure(DEFAULT_ERROR_BODY, requestCode)
                        callbackRaw?.onFailure(DEFAULT_ERROR_BODY, requestCode)
                    }
                }
                catch (e: Exception) {
                    Logger.d(TAG, " Response exception: " + e.message)
                    callback?.onFailure("Unable to process response: " + e.message, requestCode)
                    callbackRaw?.onFailure("Unable to process response: " + e.message, requestCode)
                }
                finally {
                    callback?.onComplete(requestCode)
                    callbackRaw?.onComplete(requestCode)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable?) {
                printCall(call)
                Logger.d(TAG, " Execution failure: " + t?.message)
                callback?.onFailure(t?.toString(), requestCode)
                callbackRaw?.onFailure(t?.toString(), requestCode)
                callback?.onComplete(requestCode)
                callbackRaw?.onComplete(requestCode)
            }

            private fun printCall(call: Call<ResponseBody>){

                var formattedCall = ""
                formattedCall +=
                        "< --------------------------------------------------------------------------------- >" +
                        "\n   Request TAG: ${networkRequest.getRequestTag()} " +
                        "\n   Request URL: ${call.request().url()} " +
                        "\n   Request Headers: "

                call.request().headers().toMultimap().forEach { (key, valueList) ->
                    valueList.forEach { value ->
                        formattedCall += "\n     |- $key -> $value"
                    }
                }

                Buffer().let {
                    call.request().body()?.writeTo(it)
                    formattedCall += "\n   Request Body: ${it.readUtf8()}"
                }

                formattedCall += "\n< --------------------------------------------------------------------------------- >\n\n."

                Logger.d(TAG, "Call details: \n\n $formattedCall")
            }
        })
    }

    private fun getRetrofit(okHttpClient: okhttp3.Call.Factory, baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(NetworkRequestFactory())
                .callFactory(okHttpClient)
                .build()
    }
}