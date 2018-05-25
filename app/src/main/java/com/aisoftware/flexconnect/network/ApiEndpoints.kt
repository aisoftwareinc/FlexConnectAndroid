package com.aisoftware.flexconnect.network

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiEndpoints {
    @GET("/ws/index.asmx/ReportLocation")
    fun getReportLocation(
            @Query("Phone") phoneNumber: String,
            @Query("Username") username: String,
            @Query("Password") password: String,
            @Query("UserAgent") userAgent: String,
            @Query("Latitude") latitude: String,
            @Query("Longitude") longitude: String
    ):Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/AuthenticatePhone")
    fun postAuthenticatePhone(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>

    @POST("/ws/tracking-json.asmx/GetDeliveries")
    fun postGetDeliveries(@HeaderMap headerMap: Map<String, String>, @Body body: RequestBody): Call<ResponseBody>
}