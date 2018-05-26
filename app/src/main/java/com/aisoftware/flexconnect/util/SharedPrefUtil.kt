package com.aisoftware.flexconnect.util

import android.content.Context
import com.aisoftware.flexconnect.R

class SharedPrefUtil(val context: Context) {

    private val SHARED_PREF_FILE = "com.aisoftware.flexconnect.PREFERENCE_FILE"

    fun userPrefExists(): Boolean {
        val num = getUserPref(false)
        return !num.isNullOrBlank()
    }

    fun setUserProp(number: String) {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(context.getString(com.aisoftware.flexconnect.R.string.phone_shared_pref_key), number)
            commit()
        }
    }

    fun getUserPref(delete: Boolean): String {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
        val phoneNum = sharedPref.getString(context.getString(R.string.phone_shared_pref_key), "")

        if( delete ) {
            with (sharedPref.edit()) {
                remove(context.getString(com.aisoftware.flexconnect.R.string.phone_shared_pref_key))
                commit()
            }
        }
        return phoneNum
    }

    fun setIntervalProp(interval: String) {
        var value = interval
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE) ?: return

        if( value.isNullOrBlank() ) {
            value = Constants.DEFAULT_INTERVAL_MIN
        }

        with (sharedPref.edit()) {
            putString(context.getString(com.aisoftware.flexconnect.R.string.interval_pref_key), value)
            commit()
        }
    }

    fun getIntervalPref(delete: Boolean): String {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
        val interval = sharedPref.getString(context.getString(R.string.interval_pref_key), "")

        if( delete ) {
            with (sharedPref.edit()) {
                remove(context.getString(com.aisoftware.flexconnect.R.string.phone_shared_pref_key))
                commit()
            }
        }
        return interval
    }
}