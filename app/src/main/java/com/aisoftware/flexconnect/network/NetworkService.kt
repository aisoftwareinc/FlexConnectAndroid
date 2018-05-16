package com.aisoftware.flexconnect.network

import android.util.Log
import com.aisoftware.flexconnect.network.request.NetworkRequest
import com.aisoftware.flexconnect.util.Constants
import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.simplexml.SimpleXmlConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

interface NetworkService {
    fun sendRequest(networkRequest: NetworkRequest, callback: NetworkResponseCallback)
}

class NetworkServiceImpl: NetworkService {

    private val TAG = NetworkService::class.java.simpleName

    private val connectTimeout = Constants.DEFAULT_CONNECTION_TIMEOUT_SEC
    private val readTimeout = Constants.DEFAULT_READ_TIMEOUT_SEC
    private val mediaType = MediaType.parse(Constants.MEDIA_TYPE_FORM)

    override fun sendRequest(networkRequest: NetworkRequest, callback: NetworkResponseCallback) {
        var body: RequestBody? = null

        if( !networkRequest.getRequestBody().isNullOrEmpty()) {
            body = RequestBody.create(mediaType, networkRequest.getRequestBody())
            networkRequest.addHeader("Content-Length", body.contentLength().toString())
            Log.d(networkRequest.getRequestTag(), "Request body: " + networkRequest.getRequestBody())
        }

        val client = getOkhttpClient(networkRequest.getHeadersMap(), body)
        val retrofit = getRetrofit(client, networkRequest.getBaseUrl())

        val api = retrofit.create(ApiEndpoints::class.java)
        val call = networkRequest.getRequestEndpoint(api)

        Log.d(TAG, "Attempting call with url: " + call.request().toString())
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.body() != null) {
                    try {
                        val bodyContent = response.body()!!.string()
                        callback.onSuccess(bodyContent)
                    } catch (e: IOException) {
                        Log.e(TAG, "Unable to retrieve response", e)
                        onFailure(call, e)
                    }

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                callback.onFailure(Constants.NETWORK_REQUEST_CALL_FAILURE)
                Log.e(TAG, Constants.NETWORK_REQUEST_CALL_FAILURE, t)
            }
        })
    }


    private fun getOkhttpClient(headers: Map<String, String>, body: RequestBody?): OkHttpClient {
        val builder = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val ongoing = chain.request().newBuilder()
                    ongoing.headers(Headers.of(headers))
                    if( body != null )ongoing.post(body)
                    chain.proceed(ongoing.build())
                }
//                .addInterceptor(LoggingInterceptor())
                .connectTimeout(connectTimeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(readTimeout.toLong(), TimeUnit.SECONDS)
        return builder.build()
    }

    private fun getRetrofit(okHttpClient: okhttp3.Call.Factory, baseUrl: String): Retrofit {
        return Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(SimpleXmlConverterFactory.create())
                .callFactory(okHttpClient)
                .build()
    }

    internal inner class LoggingInterceptor : Interceptor {
        private val TAG = LoggingInterceptor::class.java.simpleName

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()

            val t1 = System.nanoTime()
            if (request.body() != null) {
                Log.d(TAG, "Intercepting content type: " + request.body()!!.contentType()!!)
            }
            Log.d(TAG, String.format("Sending to url: %s with headers: \n%s", request.url(), request.headers()))
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Log.d(TAG, String.format("Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6, response.headers()))
            Log.d(TAG, "Response code: " + response.code())
            return response
        }
    }
}