package com.aisoftware.flexconnect.network.request

import okhttp3.ResponseBody



interface NetworkRequestRawResponseCallback {
    fun onSuccess(responseBody: ResponseBody?, headers: Map<String, List<String>>, requestCode: String?)
    fun onFailure(failureMessage: String?, requestCode: String?)
    fun onComplete(requestCode: String?)
}