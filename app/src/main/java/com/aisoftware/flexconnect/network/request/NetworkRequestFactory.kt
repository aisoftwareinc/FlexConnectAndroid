package com.aisoftware.flexconnect.network.request

import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type


class NetworkRequestFactory : Converter.Factory() {

    override fun requestBodyConverter(type: Type, parameterAnnotations: Array<Annotation>, methodAnnotations: Array<Annotation>, retrofit: Retrofit): Converter<*, RequestBody>? {
        return if (NetworkRequestJson::class.java == type) {
            NetworkRequestJsonConverter.INSTANCE
        } else null
    }

    internal class NetworkRequestJsonConverter : Converter<NetworkRequestJson, RequestBody> {
        @Throws(IOException::class)
        override fun convert(request: NetworkRequestJson): RequestBody {
            val requestBody = request.requestWithToken.toString()
            return RequestBody.create(MediaType.parse(MEDIA_TYPE_JSON_TEXT), requestBody)
        }

        companion object {
            var INSTANCE = NetworkRequestJsonConverter()
        }
    }

    companion object {
        var MEDIA_TYPE_JSON_TEXT = "application/json; charset=utf-8"
    }
}