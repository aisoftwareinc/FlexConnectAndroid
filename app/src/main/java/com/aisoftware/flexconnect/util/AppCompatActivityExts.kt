package com.aisoftware.flexconnect.util

import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity

fun AppCompatActivity.isPermissionGranted(permission: String) =
        ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

fun AppCompatActivity.shouldShowPermissionRationale(permission: String) =
        ActivityCompat.shouldShowRequestPermissionRationale(this, permission)

fun AppCompatActivity.requestPermission(permission: String, requestId: Int) =
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestId)

fun IntArray.containsOnly(num: Int): Boolean = filter { it == num }.isNotEmpty()