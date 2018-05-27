package com.aisoftware.flexconnect.network.request

import org.json.JSONObject
import java.lang.reflect.Type


interface NetworkRequestJson : NetworkRequest, Type {
    val requestWithToken: JSONObject
}