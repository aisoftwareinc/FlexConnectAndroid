package com.aisoftware.flexconnect.network

import com.aisoftware.flexconnect.network.request.NetworkRequest
import com.aisoftware.flexconnect.network.request.NetworkRequestCallback
import com.aisoftware.flexconnect.network.request.NetworkRequestRawResponseCallback
import com.aisoftware.flexconnect.util.Constants.DEFAULT_CONNECTION_TIMEOUT_SEC
import com.aisoftware.flexconnect.util.Constants.DEFAULT_READ_TIMEOUT_SEC
import com.aisoftware.flexconnect.util.Logger
import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.OkHttpClient
import java.io.IOException
import java.util.concurrent.TimeUnit

interface NetworkService {
    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest)

    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback?, requestCode: String?)

    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback?, requestCode: String?, fetchFromCache: Boolean)

    @Throws(NetworkRequestException::class)
    fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestRawResponseCallback, requestCode: String)
}

open class NetworkServiceDefault private constructor(builder: Builder) : NetworkService {
    private var networkHandler: NetworkHandler? = null

    private val TAG = NetworkService::class.java.simpleName
    var DEFAULT_CONNECTION_TIMEOUT_SEC = 60
    var DEFAULT_READ_TIMEOUT_SEC = 60
    var DEFAULT_USE_CACHE = false
    var DEFAULT_FETCH_INTO_CACHE = true
    var DEFAULT_USE_LOG_INTERCEPTOR = false
    var DEFAULT_BUILD_TYPE = true  // true = production, which means don't enable logging
    var MEDIA_TYPE_JSON_TEXT = "application/json; charset=utf-8"
    var MEDIA_TYPE_FORM_DATA_TEXT = "multipart/form-data"
    var MEDIA_TYPE_PDF_NO_BODY_TEXT = "image/pdf"    //have handler send back the response body
    var MEDIA_TYPE_JSON = MediaType.parse(MEDIA_TYPE_JSON_TEXT)
    var MEDIA_TYPE_FORM_DATA = MediaType.parse(MEDIA_TYPE_FORM_DATA_TEXT)
    var DEFAULT_TIME_UNIT = TimeUnit.SECONDS

    var connectTimeout: Int = DEFAULT_CONNECTION_TIMEOUT_SEC
    var readTimeout: Int = DEFAULT_READ_TIMEOUT_SEC
    var interceptorList: List<Interceptor> = ArrayList()
    var timeUnit: TimeUnit = DEFAULT_TIME_UNIT
//    var networkCache: NetworkCache? = null


    private val okhttpClient: OkHttpClient
        get() {
            val builder = OkHttpClient.Builder()
                    .connectTimeout(connectTimeout.toLong(), timeUnit)
                    .readTimeout(readTimeout.toLong(), timeUnit)

            if ( interceptorList.isNotEmpty() ) {
                for (interceptor in interceptorList) {
                    builder.addInterceptor(interceptor)
                }
            }
            return builder.build()
        }

    init {
        this.connectTimeout = builder.connectTimeout
        this.readTimeout = builder.readTimeout
        this.interceptorList = builder.interceptorList
        this.timeUnit = builder.timeUnit
        this.networkHandler = builder.networkHandler
//        this.networkCache = builder.networkCache
    }

    override fun startRequest(networkRequest: NetworkRequest) {
        startRequest(networkRequest, null, null, false)
    }

    override fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback?, requestCode: String?) {
        startRequest(networkRequest, callback, requestCode, false)
    }

    /**
     * Start request.
     *
     * @param networkRequest the network request
     * @param callback       the callback
     * @param requestCode    the request code
     * @throws NetworkRequestException the network request exception
     */
    override fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestCallback?, requestCode: String?, fetchFromCache: Boolean) {

        getNetworkHandler().startRequest(networkRequest, object : NetworkRequestCallback {

            override fun onSuccess(data: String?, headers: Map<String, List<String>>, requestCode: String?) {
//                    networkCache?.let {
//                        Log.d(networkRequest.requestTag, "Attempting to cache with key: $networkRequest.requestTag and value: $data")
//                        it.put(networkRequest.requestTag, data)
//                    }
                callback?.onSuccess(data, headers, requestCode)
            }

            override fun onFailure(data: String?, requestCode: String?) {
                callback?.onFailure(data, requestCode)
            }

            override fun onComplete(requestCode: String?) {
                callback?.onComplete(requestCode)
            }
        }, requestCode)

//        var cacheResult: Any? = null
//        if (networkCache != null && fetchFromCache) {
//            try {
//                cacheResult = networkCache?.get(networkRequest.requestTag)
//            } catch (exception: NetworkCacheException) {
//                Log.d(networkRequest.requestTag, "Cache Exception:" + exception.message)
//            } finally {
//                if (cacheResult != null) {
//                    callback!!.onSuccess(cacheResult as String?, HashMap(), requestCode)//TODO Is it safe to always cast this to a string ? Look into cache improvements
//                }else
//                    makeRequest()
//            }
//        }else{
//            makeRequest()
//        }
    }

    /**
     * Start request.
     *
     * @param networkRequest the network request
     * @param callback       the callback
     * @param requestCode    the request code
     * @throws NetworkRequestException the network request exception
     */
    @Throws(NetworkRequestException::class)
    override fun startRequest(networkRequest: NetworkRequest, callback: NetworkRequestRawResponseCallback, requestCode: String) {
        getNetworkHandler().startRequest(networkRequest, callback, requestCode)
    }

    @Throws(NetworkRequestException::class)
    private fun getNetworkHandler(): NetworkHandler {
        if (networkHandler == null) {
            networkHandler = RequestHandler(okhttpClient)
        }
        return networkHandler as NetworkHandler
    }

    /**
     * The type Builder.
     */
    class Builder {
        var connectTimeout = DEFAULT_CONNECTION_TIMEOUT_SEC
        var readTimeout = DEFAULT_READ_TIMEOUT_SEC
        var timeUnit = TimeUnit.SECONDS
        var interceptorList: List<Interceptor> = ArrayList()
        var networkHandler: NetworkHandler? = null
//        var networkCache: NetworkCache? = null

        /**
         * Connect timeout builder.
         *
         * @param connectTimeout the connect timeout
         * @return the builder
         */
        fun connectTimeout(connectTimeout: Int): Builder {
            this.connectTimeout = connectTimeout
            return this
        }

        /**
         * Interceptor list builder.
         *
         * @param interceptorList the interceptor list
         * @return the builder
         */
        fun interceptorList(interceptorList: List<Interceptor>): Builder {
            this.interceptorList = interceptorList
            return this
        }

        /**
         * Read timeout builder.
         *
         * @param readTimeout the read timeout
         * @return the builder
         */
        fun readTimeout(readTimeout: Int): Builder {
            this.readTimeout = readTimeout
            return this
        }

        /**
         * Time unit builder.
         *
         * @param timeUnit the time unit
         * @return the builder
         */
        fun timeUnit(timeUnit: TimeUnit): Builder {
            this.timeUnit = timeUnit
            return this
        }

        /**
         * Network handler builder.
         *
         * @param networkHandler the network handler
         * @return the builder
         */
        fun networkHandler(networkHandler: NetworkHandler): Builder {
            this.networkHandler = networkHandler
            return this
        }

//        fun networkCache(cache: NetworkCache): Builder {
//            networkCache = cache
//            return this
//        }

        /**
         * Build network service.
         *
         * @return the network service
         */
        fun build(): NetworkService {
            return NetworkServiceDefault(this)
        }
    }

    /**
     * The type Logging interceptor.
     */
    internal inner class LoggingInterceptor : Interceptor {
        private val TAG = LoggingInterceptor::class.java.simpleName

        @Throws(IOException::class)
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val request = chain.request()
            val t1 = System.nanoTime()
            if (request.body() != null)
//                Log.d(TAG, "Intercepting content type: " + request.body().contentType().toString())
            //            MultipartBody multipartBody = (MultipartBody) request.body();
            //            Log.d(TAG, " intercepting multipartBody.type() "+multipartBody.type());
            //            Log.d(TAG, " intercepting multipartBody.boundary() "+multipartBody.boundary());
            //            Log.d(TAG, " intercepting multipartBody.parts() "+multipartBody.parts());
                Logger.d(TAG, String.format("Sending to url: %s with headers: %s", request.url(), request.headers()))
            val response = chain.proceed(request)
            val t2 = System.nanoTime()
            Logger.d(TAG, String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6, response.headers()))
            Logger.d(TAG, "Response code: " + response.code())
            //            Log.d(TAG, "Response body: " + response.body().string());
            return response
        }
    }
}