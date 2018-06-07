package com.aisoftware.flexconnect.util

import android.util.Log
import com.aisoftware.flexconnect.BuildConfig

class Logger {

    companion object {
        @JvmStatic
        fun d(tag: String, message: String) {
            if( BuildConfig.DEBUG ) {
                Log.d(tag, message)
            }
        }

        @JvmStatic
        fun i(tag: String, message: String) {
            if( BuildConfig.DEBUG ) {
                Log.i(tag, message)
            }
        }

        @JvmStatic
        fun v(tag: String, message: String) {
            if( BuildConfig.DEBUG ) {
                Log.v(tag, message)
            }
        }

        @JvmStatic
        fun w(tag: String, message: String, throwable: Throwable) {
            if( BuildConfig.DEBUG ) {
                Log.w(tag, message, throwable)
            }
        }

        @JvmStatic
        fun e(tag: String, message: String, throwable: Throwable) {
            if( BuildConfig.DEBUG ) {
                Log.e(tag, message, throwable)
            }
        }
    }
}