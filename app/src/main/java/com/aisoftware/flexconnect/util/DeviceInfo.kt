package com.aisoftware.flexconnect.util

import android.app.Application
import android.os.Build
import android.os.Build.VERSION
import android.provider.Settings
import java.util.*


class DeviceInfo(val application: Application) {

    private val TAG = DeviceInfo::class.java.simpleName

    /** The Unique device ID, the IMEI for GSM and the MEID for CDMA phones.  */
    var uniqueDeviceId: String

    /** The OS build name and version.  */
    var osBuild: String

    /** The OS version.  */
    var osVersion: String

    /** The phone/device model.  */
    var model: String

    /** The phone/device model.  */
    var deviceManufacture: String

    /** The phone/device API Level.(Added for hybrid changes)  */
    var deviceAPI: Int = 0

    // Variable to hold device locale.
    var deviceLocale: String

    /** A 64-bit number (as a hex string) that is randomly generated on the device's first boot
     * and should remain constant for the lifetime of the device
     * (The value may change if a factory reset is performed on the device.)
     */
    var androidDeviceId: String

    var platformOsVersion: String

    init {
        var version = ""
        var deviceID = ""
        var androidOsVer = ""

        try {
            deviceID = Settings.Secure.getString(application.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID)
//            if (deviceID == null || deviceID == "9774d56d682e549c") {
//                val tman = application.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                deviceID = if (tman != null) {
//                    tman.deviceId
//                } else {
//                    ""
//                }
//            }

            androidOsVer = System.getProperty("os.version")
            version = System.getProperty("os.name") + " v " + androidOsVer
        }
        catch (e: Exception) {
            Logger.e(TAG, "Unable to initialize device info environment", e)
        }

        androidDeviceId = deviceID
        uniqueDeviceId = deviceID
        osBuild = version
        osVersion = androidOsVer
        model = Build.MODEL
        deviceManufacture = Build.MANUFACTURER
        deviceAPI = VERSION.SDK_INT
        platformOsVersion = VERSION.RELEASE
        deviceLocale = Locale.getDefault().toString()
    }
}