package com.aisoftware.flexconnect.util

import android.util.Log
import com.aisoftware.flexconnect.db.entity.DeliveryEntity

class ConverterUtil {

    companion object {
        private val TAG = ConverterUtil::class.java.simpleName

        @JvmStatic
        fun formatExtendedAddress(deliveryEntity: DeliveryEntity): String {
            with(deliveryEntity) {
                var buf = StringBuilder()
                buf.append(city)
                        .append(", ")
                        .append(deliveryEntity.state)
                        .append(" ")
                        .append(deliveryEntity.zip)
                Log.d(TAG, "Formatted extended address: $buf")
                return buf.toString()
            }
        }
    }
}