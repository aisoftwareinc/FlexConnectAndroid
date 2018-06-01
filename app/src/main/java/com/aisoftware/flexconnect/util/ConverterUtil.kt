package com.aisoftware.flexconnect.util

import android.graphics.Bitmap
import android.util.Base64
import com.aisoftware.flexconnect.model.Delivery
import java.io.ByteArrayOutputStream
import java.net.URLEncoder

class ConverterUtil {

    companion object {
        private val TAG = ConverterUtil::class.java.simpleName

        @JvmStatic
        fun formatExtendedAddress(delivery: Delivery): String {
            with(delivery) {
                var buf = StringBuilder()
                buf.append(city)
                        .append(", ")
                        .append(delivery.state)
                        .append(" ")
                        .append(delivery.zip)
                return buf.toString()
            }
        }

        @JvmStatic
        fun convertImage(bitmap: Bitmap): String {
            var baos: ByteArrayOutputStream? = null
            var encodedStr = ""
            try {
                baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val byteArrayImage = baos.toByteArray()
                encodedStr = URLEncoder.encode(Base64.encodeToString(byteArrayImage, Base64.DEFAULT), "UTF-8")
            }
            catch (e: Exception) {
                Logger.e(TAG, "Unable to encode bitmap", e)
            }
            finally {
                baos?.close()
            }
            return encodedStr
        }
    }
}