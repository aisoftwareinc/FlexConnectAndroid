package com.aisoftware.flexconnect.ui

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.aisoftware.flexconnect.BuildConfig
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.location.ACTION_PROCESS_UPDATES
import com.aisoftware.flexconnect.location.LocationUpdatesBroadcastReceiver
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.containsOnly
import com.aisoftware.flexconnect.util.isPermissionGranted
import com.aisoftware.flexconnect.util.requestPermission
import com.aisoftware.flexconnect.util.shouldShowPermissionRationale
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.activity_delivery_detail.*


class DeliveryDetailActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = DeliveryDetailActivity::class.java.simpleName
    private val MAP_REQUEST_CODE = 99
    private val CAMERA_REQUEST_CODE = 88
    private val REQUEST_CAMERA_PERMISSION_CODE = 2
    private val REQUEST_LOCATION_PERMISSION_CODE = 3

    // Location
    private val DEFAULT_UPDATE_INTERVAL: Long = 60000 // every 60 seconds
    private val DEFAULT_FAST_UPDATE_INTERVAL: Long = 30000 // every 30 seconds
    private val MAX_WAIT_TIME: Long = DEFAULT_UPDATE_INTERVAL * 10 // 10 minutes
    private var REQUESTING_LOCATION_UPDATES_KEY = "requestingLocationUpdatesKey"
    private var DELIVERED_STATE_KEY = "deliveredStateKey"
    private var requestingLocationUpdates = false
    private var deliveredState = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var deliveryEntity: DeliveryEntity

    companion object {
        @JvmStatic
        fun getInstance(context: Context, deliveryEntity: DeliveryEntity): Intent {
            val intent = Intent(context, DeliveryDetailActivity::class.java)
            intent.putExtra(Constants.DELIVERY_DETAIL, deliveryEntity)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_detail)
        fusedLocationProviderClient = FusedLocationProviderClient(this)


        detailEnRouteCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            requestingLocationUpdates = isChecked
            if (isChecked) {
                checkLocationUpdate()
            } else {
                stopLocationUpdate()
            }
        }

        detailDeliveredCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            deliveredState = isChecked
            if (isChecked) {
                showCameraDialog(getString(R.string.delivery_detail_delivered_photo_title), getString(R.string.delivery_detail_delivered_photo_message))
            }
        }

        detailDrivingDirectionsButton.setOnClickListener {
            navigateToMapView()
        }

        if (intent.hasExtra(Constants.DELIVERY_DETAIL)) {
            deliveryEntity = intent.getSerializableExtra(Constants.DELIVERY_DETAIL) as DeliveryEntity
            initializeView(deliveryEntity)
        } else {
            showErrorDialog(getString(R.string.delivery_detail_error_title), getString(R.string.delivery_detail_error_message), true)
        }

        updateValuesFromBundle(savedInstanceState)
    }

    private fun initializeView(deliveryEntity: DeliveryEntity) {
        Log.d(TAG, "Initializing view with delivery: $deliveryEntity")

        val formattedPhone = formatPhone(deliveryEntity.phone1)
        formattedPhone?.let {
            detailDeliveryPhoneEditText.text = formattedPhone
        }
        detailDeliverylNameTextView.text = deliveryEntity.name
        detailAddress1TextView.text = deliveryEntity.address1
        detailAddress2TextView.text = deliveryEntity.address2
        detailAddress3TextView.text = ConverterUtil.formatExtendedAddress(deliveryEntity)
        detailDistanceTextView.text = deliveryEntity.distance
        detailEtaTextView.text = deliveryEntity.eta
        detailCommentsTextView.text = deliveryEntity.comments
    }

    private fun formatPhone(rawPhone: String): String? {
        var formatted: String? = null

        try {
            if (!rawPhone.isNullOrBlank() && rawPhone.length == 10) {
                if (rawPhone.toLongOrNull() != null) {
                    var areaCode = rawPhone.substring(0..2)
                    var pref = rawPhone.subSequence(3..5)
                    var post = rawPhone.subSequence(6..9)
                    formatted = "( $areaCode ) $pref-$post"
                }
            }
        }
        catch(e: Exception) {
            Log.e(TAG, "Unable to format phone number", e)
        }
        return formatted
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY)
        }

        if (savedInstanceState.keySet().contains(DELIVERED_STATE_KEY)) {
            deliveredState = savedInstanceState.getBoolean(DELIVERED_STATE_KEY)
        }

        if (deliveredState) {
            requestingLocationUpdates = false
        } else {
            detailEnRouteCheckBox.isChecked = requestingLocationUpdates
        }
        detailDeliveredCheckBox.isChecked = deliveredState
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        outState?.putBoolean(DELIVERED_STATE_KEY, deliveredState)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            checkLocationUpdate()
        }
    }

    private fun navigateToMapView() {
        val rawAddress = StringBuilder()
        rawAddress.append(deliveryEntity.address1)
                .append(" ")
                .append(deliveryEntity.city)
                .append(", ")
                .append(deliveryEntity.state)
                .append(" ")
                .append(deliveryEntity.zip)
        val uriEncodedAddress = Uri.encode(rawAddress.toString())
        val intentUri = Uri.parse("google.navigation:q=$uriEncodedAddress")
        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        startActivityForResult(mapIntent, MAP_REQUEST_CODE)
    }

    private fun navigateToCamera() {
        val intentPic = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intentPic.resolveActivity(packageManager) != null) {
            startActivityForResult(intentPic, CAMERA_REQUEST_CODE)
        }
    }

    private fun showErrorDialog(title: String, message: String, isFinish: Boolean) {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(Constants.POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                        if (isFinish) {
                            finish()
                        }
                    }).create().show()
        }
    }

    private fun showCameraDialog(title: String, message: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(Constants.POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                        checkShowCamera()
                    })
                    .setNegativeButton(Constants.NEG_BUTTON, { dialog, id ->
                        dialog.dismiss()
                    })
                    .create().show()
        }
    }

    private fun checkShowCamera() {
        Log.i(TAG, "Show camera button pressed. Checking permission.")
        // Check if the Camera permission is already available.
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            // Camera permission has not been granted.
            requestPermission(Manifest.permission.CAMERA, getString(R.string.permission_camera_rationale), REQUEST_CAMERA_PERMISSION_CODE)
        } else {
            // Camera permissions is already available, show the camera preview.
            Log.i(TAG, "CAMERA permission has already been granted. Displaying camera preview.")
            navigateToCamera()
        }
    }

    private fun requestPermission(permission: String, message: String, requestCode: Int) {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.")
        if (shouldShowPermissionRationale(permission)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
            Snackbar.make(detailLayout, message,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permission_dialog_ok, {
                        requestPermission(permission, requestCode)
                    })
                    .show()
        } else {
            // Camera permission has not been granted yet. Request it directly.
            requestPermission(permission, requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA_PERMISSION_CODE) {
            // Received permission result for camera permission.
            Log.i(TAG, "Received response for Camera permission request.")

            // Check if the permission has been granted
            if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                // Camera permission has been granted, preview can be displayed
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.")
                Snackbar.make(detailLayout, R.string.permision_available_camera, Snackbar.LENGTH_SHORT).show()
                navigateToCamera()
            } else {
                Log.i(TAG, "CAMERA permission was NOT granted.")
                navigateToSettings(R.string.permissions_not_granted)
            }
        } else if (requestCode == REQUEST_LOCATION_PERMISSION_CODE) {
            Log.i(TAG, "Received response for Location permission request.")
            if (grantResults.containsOnly(PackageManager.PERMISSION_DENIED)) {
                Log.i(TAG, "CAMERA permission has now been granted. Showing preview.")
                Snackbar.make(detailLayout, R.string.permision_available_location, Snackbar.LENGTH_SHORT).show()
                startLocationUpdate()
            } else {
                Log.i(TAG, "Location permission was NOT granted.")
                navigateToSettings(R.string.permissions_not_granted)
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    private fun navigateToSettings(messageId: Int) {
        Snackbar.make(
                detailLayout,
                messageId,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.settings, View.OnClickListener {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                    intent.data = uri
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                })
                .show()
    }

    private fun checkLocationUpdate() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, getString(R.string.permission_location_rationale), REQUEST_LOCATION_PERMISSION_CODE)
        } else {
            Log.i(TAG, "Location permission has already been granted.")
            startLocationUpdate()
        }
    }

    private fun startLocationUpdate() {
        Log.i(TAG, "CAMERA permission has already been granted. Displaying camera preview.")
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), getLocationPendingIntent())
        }
    }

    private fun stopLocationUpdate() {
        fusedLocationProviderClient.removeLocationUpdates(getLocationPendingIntent())
    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locationRequest.interval = DEFAULT_UPDATE_INTERVAL
        locationRequest.fastestInterval = DEFAULT_FAST_UPDATE_INTERVAL
        locationRequest.maxWaitTime = MAX_WAIT_TIME
        return locationRequest
    }

    private fun getLocationPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = ACTION_PROCESS_UPDATES
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        return pendingIntent
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK ->
                when (requestCode) {
                    MAP_REQUEST_CODE -> Log.d(TAG, "Received MAP activity result with resultCode: $resultCode and data: $data")
                    CAMERA_REQUEST_CODE -> {
                        Log.d(TAG, "Received CAMERA activity result with resultCode: $resultCode and data: $data")
                        val extras = data?.getExtras()
                        val imageBitmap = extras?.get("data") as Bitmap
                    }

                    else -> {
                        Log.d(TAG, "Received onActivity requestCode: $requestCode, resultCode: $resultCode, data: $data")
                    }
                }
            Activity.RESULT_CANCELED ->
                    Log.d(TAG, "Action cancelled")
            else ->
                    Log.d(TAG, "Unrecognized action")
        }
    }
}
