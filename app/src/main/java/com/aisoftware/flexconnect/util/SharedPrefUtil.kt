package com.aisoftware.flexconnect.util

import android.content.Context
import com.aisoftware.flexconnect.R

interface SharedPrefUtil {
    fun userPrefExists(): Boolean
    fun setUserProp(number: String)
    fun getUserPref(delete: Boolean): String

    fun setLocationClientRunning(isRunning: Boolean)
    fun getLocationClientRunning(): Boolean
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

    override fun setLocationClientRunning(isRunning: Boolean) {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            putBoolean(context.getString(R.string.is_running_shared_pref_key), isRunning)
            commit()
        }
    }

    override fun getLocationClientRunning(): Boolean {
        val sharedPref = context.getSharedPreferences(SHARED_PREF_FILE, Context.MODE_PRIVATE)
        val isRunning = sharedPref.getBoolean(context.getString(R.string.is_running_shared_pref_key), false)
        return isRunning
    }
}