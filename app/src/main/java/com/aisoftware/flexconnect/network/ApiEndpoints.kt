package com.aisoftware.flexconnect.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HeaderMap
import retrofit2.http.POST


interface ApiEndpoints {
    @POST("/ws/tracking-json.asmx/AuthenticatePhone")
    fun postAuthenticatePhone(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/GetDeliveries")
    fun postGetDeliveries(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/TimerInterval")
    fun postTimerInterval(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/ReportLocation")
    fun postReportLocation(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/UpdateStatus")
    fun postDelivered(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/UpdateStatus")
    fun postEnRoute(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/UpdateStatus")
    fun postUpdateStatus(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>
}