package com.aisoftware.flexconnect.util

import android.content.Context
import com.aisoftware.flexconnect.R

interface SharedPrefUtil {
    fun userPrefExists(): Boolean
    fun setUserProp(number: String)
    fun getUserPref(delete: Boolean): String
    fun setIntervalProp(interval: String)
    fun getIntervalPref(delete: Boolean): String
    fun setEnRouteStatus(guid: String, isEnRoute: Boolean)
    fun getEnRouteStatus(guid: String, delete: Boolean): Boolean
}

class SharedPrefUtilImpl(val context: Context): SharedPrefUtil {

    private val SHARED_PREF_FILE = "com.aisoftware.flexconnect.PREFERENCE_FILE"

    override fun userPrefExists(): Boolean {
        val num = getUserPref(false)
        return !num.isNullOrBlank()
    }

    override fun setUserProp(number: String) {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putString(context.getString(R.string.phone_shared_pref_key), number)
            commit()
        }
    }

    override fun getUserPref(delete: Boolean): String {
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

    override fun setIntervalProp(interval: String) {
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

    override fun getIntervalPref(delete: Boolean): String {
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

    override fun setEnRouteStatus(guid: String, isEnRoute: Boolean) {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE) ?: return

        with (sharedPref.edit()) {
            putBoolean(guid, isEnRoute)
            commit()
        }
    }

    override fun getEnRouteStatus(guid: String, delete: Boolean): Boolean {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
        val isEnRoute = sharedPref.getBoolean(guid, false)

        if( delete ) {
            with(sharedPref.edit()) {
                remove(guid)
                commit()
            }
        }
        return isEnRoute
    }
}