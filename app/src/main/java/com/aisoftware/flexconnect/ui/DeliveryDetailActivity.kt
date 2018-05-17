package com.aisoftware.flexconnect.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.containsOnly
import com.aisoftware.flexconnect.util.isPermissionGranted
import com.aisoftware.flexconnect.util.requestPermission
import com.aisoftware.flexconnect.util.shouldShowPermissionRationale
import kotlinx.android.synthetic.main.activity_delivery_detail.*


class DeliveryDetailActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = DeliveryDetailActivity::class.java.simpleName
    private val MAP_REQUEST_CODE = 99
    private val CAMERA_REQUEST_CODE = 88
    private val REQUEST_CAMERA = 2
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

        detailEnRouteCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->

        }

        detailDeliveredCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                showCamera()
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
    }

    private fun initializeView(deliveryEntity: DeliveryEntity) {
        Log.d(TAG, "Initializing view with delivery: $deliveryEntity")

        detailDeliverylNameTextView.text = deliveryEntity.name
        detailDeliveryPhoneEditText.text = deliveryEntity.phone1
        detailAddress1TextView.text = deliveryEntity.address1
        detailAddress2TextView.text = deliveryEntity.address2
        detailAddress3TextView.text = ConverterUtil.formatExtendedAddress(deliveryEntity)
        detailDistanceTextView.text = deliveryEntity.distance
        detailEtaTextView.text = deliveryEntity.eta
        detailCommentsTextView.text = deliveryEntity.comments
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
                    .setPositiveButton(Constants.VALIDATION_POS_BUTTON, { dialog, id ->
                        dialog.dismiss()
                        if (isFinish) {
                            finish()
                        }
                    }).create().show()
        }
    }

    fun showCamera() {
        Log.i(TAG, "Show camera button pressed. Checking permission.")
        // Check if the Camera permission is already available.
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            // Camera permission has not been granted.
            requestCameraPermission()
        } else {
            // Camera permissions is already available, show the camera preview.
            Log.i(TAG, "CAMERA permission has already been granted. Displaying camera preview.")
            navigateToCamera()
        }
    }

    private fun requestCameraPermission() {
        Log.i(TAG, "CAMERA permission has NOT been granted. Requesting permission.")
        if (shouldShowPermissionRationale(Manifest.permission.CAMERA)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // For example if the user has previously denied the permission.
            Log.i(TAG, "Displaying camera permission rationale to provide additional context.")
            Snackbar.make(detailLayout, R.string.permission_camera_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.permission_dialog_ok, {
                        requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
                    })
                    .show()
        } else {

            // Camera permission has not been granted yet. Request it directly.
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CAMERA) {
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
                Snackbar.make(detailLayout, R.string.permissions_not_granted, Snackbar.LENGTH_SHORT).show()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
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
        }
    }
}
