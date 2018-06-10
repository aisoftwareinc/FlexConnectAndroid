package com.aisoftware.flexconnect.ui.detail

import android.Manifest
import android.app.ActionBar
import android.app.Activity
import android.app.Dialog
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
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.aisoftware.flexconnect.BuildConfig
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.location.ACTION_PROCESS_UPDATES
import com.aisoftware.flexconnect.location.LocationUpdatesBroadcastReceiver
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.ui.DashboardActivity
import com.aisoftware.flexconnect.ui.FlexConnectActivityBase
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.aisoftware.flexconnect.util.containsOnly
import com.aisoftware.flexconnect.util.isPermissionGranted
import com.aisoftware.flexconnect.util.requestPermission
import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import kotlinx.android.synthetic.main.activity_delivery_detail.*


class DeliveryDetailActivity : FlexConnectActivityBase(), DeliveryDetailView, ActivityCompat.OnRequestPermissionsResultCallback {

    private val TAG = DeliveryDetailActivity::class.java.simpleName
    private val MAP_REQUEST_CODE = 99
    private val CAMERA_REQUEST_CODE = 88
    private val REQUEST_CAMERA_PERMISSION_CODE = 2
    private val REQUEST_LOCATION_PERMISSION_CODE = 3

    private var REQUESTING_LOCATION_UPDATES_KEY = "requestingLocationUpdatesKey"
    private var requestingLocationUpdates = false
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var presenter: DeliveryDetailPresenter

    companion object {
        @JvmStatic
        fun getInstance(context: Context, delivery: Delivery): Intent {
            val intent = Intent(context, DeliveryDetailActivity::class.java)
            intent.putExtra(Constants.DELIVERY_DETAIL, delivery)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivery_detail)
        updateValuesFromBundle(savedInstanceState)

//        val toolbar = findViewById<Toolbar>(R.id.detailToolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        presenter = DeliveryDetailPresenterImpl(this, DeliveryDetailInteractorImpl(getNetworkService()))
        fusedLocationProviderClient = FusedLocationProviderClient(this)

        detailEnRouteCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            requestingLocationUpdates = isChecked
            if (isChecked) {
                presenter.checkLocationUpdate()
            } else {
                presenter.stopLocationUpdate()
            }
        }

        detailDeliveredButton.setOnClickListener{
            presenter.detailDeliveredChecked()
        }

        detailDrivingDirectionsButton.setOnClickListener {
            presenter.detailDirectionsClicked()
        }

        if (intent.hasExtra(Constants.DELIVERY_DETAIL)) {
            val delivery = intent.getSerializableExtra(Constants.DELIVERY_DETAIL) as Delivery
            presenter.initialize(delivery)
        }
        else {
            presenter.initialize(null)
        }
    }

    override fun initializeView(delivery: Delivery, formattedPhone: String) {
        Logger.d(TAG, "Initializing view with delivery: $delivery")

        formattedPhone.let {
            detailDeliveryPhoneEditText.text = formattedPhone
        }
        detailDeliverylNameTextView.text = delivery.customerName
        detailAddress1TextView.text = delivery.address
        detailAddress2TextView.text = ConverterUtil.formatExtendedAddress(delivery)
        detailDistanceTextView.text = delivery.miles
        detailEtaTextView.text = delivery.distance
        detailCommentsTextView.text = delivery.comments

        adjustButtonSize(detailDrivingDirectionsButton)
        adjustButtonSize(detailDeliveredButton)
    }

    private fun adjustButtonSize(button: Button) {
    val displayMetrics = getResources().getDisplayMetrics();
    val width = displayMetrics.widthPixels;
    val params = button.getLayoutParams();
    params.width = width/2
    button.width = width
}

    override fun onBackPressed() {
        presenter.onBackPressed()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    override fun toggleEnRouteCheckbox(clicked: Boolean) {
        detailEnRouteCheckBox.isChecked = clicked
    }

    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        if (savedInstanceState.keySet().contains(REQUESTING_LOCATION_UPDATES_KEY)) {
            requestingLocationUpdates = savedInstanceState.getBoolean(REQUESTING_LOCATION_UPDATES_KEY)
        }
        detailEnRouteCheckBox.isChecked = requestingLocationUpdates
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putBoolean(REQUESTING_LOCATION_UPDATES_KEY, requestingLocationUpdates)
        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()
        if (requestingLocationUpdates) {
            presenter.checkLocationUpdate()
        }
    }

    fun onKeyMetric() {
        Answers.getInstance().logCustom(CustomEvent("Driving Directions")
                .putCustomAttribute("Category", "Clicked")
                .putCustomAttribute("Length", 350))
    }

    override fun navigateToMapView(delivery: Delivery) {
        val rawAddress = StringBuilder()
        rawAddress.append(delivery.address)
                .append(" ")
                .append(delivery.city)
                .append(", ")
                .append(delivery.state)
                .append(" ")
                .append(delivery.zip)
        val uriEncodedAddress = Uri.encode(rawAddress.toString())
        val intentUri = Uri.parse("google.navigation:q=$uriEncodedAddress")
        val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)
        mapIntent.`package` = "com.google.android.apps.maps"
        startActivityForResult(mapIntent, MAP_REQUEST_CODE)
    }

    override fun navigateToCamera() {
        val intentPic = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intentPic.resolveActivity(packageManager) != null) {
            startActivityForResult(intentPic, CAMERA_REQUEST_CODE)
        }
    }

    override fun checkShowCamera() {
        Logger.i(TAG, "Show camera button pressed. Checking permission.")
        // Check if the Camera permission is already available.
        if (!isPermissionGranted(Manifest.permission.CAMERA)) {
            // Camera permission has not been granted.
            requestPermission(Manifest.permission.CAMERA, REQUEST_CAMERA_PERMISSION_CODE)
        } else {
            // Camera permissions is already available, show the camera preview.
            Logger.i(TAG, "CAMERA permission has already been granted. Displaying camera preview.")
            presenter.cameraPermissionPassed()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            REQUEST_CAMERA_PERMISSION_CODE -> {
                Logger.i(TAG, "Received response for Camera permission request.")
                if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                    Logger.i(TAG, "CAMERA permission has now been granted. Showing preview.")
                    Snackbar.make(detailLayout, R.string.permision_available_camera, Snackbar.LENGTH_SHORT).show()
                    presenter.cameraPermissionPassed()
                }
                else {
                    Logger.i(TAG, "CAMERA permission was NOT granted.")
                    presenter.permissionFailed()
                }
            }

            REQUEST_LOCATION_PERMISSION_CODE -> {
                Logger.i(TAG, "Received response for Location permission request.")
                if (grantResults.containsOnly(PackageManager.PERMISSION_GRANTED)) {
                    Logger.i(TAG, "Location permission has now been granted.")
                    Snackbar.make(detailLayout, R.string.permision_available_location, Snackbar.LENGTH_SHORT).show()
                    presenter.locationPermissionPassed()
                }
                else {
                    Logger.i(TAG, "Location permission was NOT granted.")
                    presenter.permissionFailed()
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun navigateToSettings() {
        if (!isFinishing) {
            CrashLogger.log(1, TAG, "Showing permission required dialog")
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_detail_permission_required_title))
                    .setMessage(getString(R.string.delivery_detail_permission_required_message))
                    .setPositiveButton(getString(R.string.dialog_ok), { dialog, id ->
                        dialog.dismiss()
                        val intent = Intent()
                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        val uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                        intent.data = uri
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    })
                    .setNegativeButton( getString(R.string.delivery_detail_permission_neg_button), { dialog, id ->
                        dialog.dismiss()
                    } )
                    .create().show()
        }
    }

    override fun navigateToDashboard() {
        val intent = DashboardActivity.getIntent(this, true)
        startActivity(intent)
    }

    override fun checkLocationUpdate() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Logger.d(TAG, "Location permission not granted.  Starting grant process.")
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION, REQUEST_LOCATION_PERMISSION_CODE)
        }
        else {
            Logger.d(TAG, "Found location permission has been granted")
            presenter.locationPermissionPassed()
        }
    }

    override fun startLocationUpdate(locationUpdateRequest: LocationRequest) {
        Logger.i(TAG, "Starting location updates with request: $locationUpdateRequest")
        if( isNetworkAvailable() ) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationProviderClient.requestLocationUpdates(locationUpdateRequest, getLocationPendingIntent())
            }
        }
        else {
            showNetworkAvailabilityError()
        }
    }

    override fun stopLocationUpdate() {
        Logger.d(TAG, "Stopping location update")
        fusedLocationProviderClient.removeLocationUpdates(getLocationPendingIntent())
    }

    private fun getLocationPendingIntent(): PendingIntent {
        val intent = Intent(this, LocationUpdatesBroadcastReceiver::class.java)
        intent.action = ACTION_PROCESS_UPDATES
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK ->
                when (requestCode) {
                    MAP_REQUEST_CODE -> {
                        Logger.d(TAG, "Received MAP activity result with resultCode: $resultCode and data: $data")
                    }
                    CAMERA_REQUEST_CODE -> {
                        Logger.d(TAG, "Received CAMERA activity result with resultCode: $resultCode and data: $data")
                        presenter.imageDataReceived(data)
                    }
                    else -> {
                        Logger.d(TAG, "Received onActivity requestCode: $requestCode, resultCode: $resultCode, data: $data")
                    }
                }
            Activity.RESULT_CANCELED -> {
                Logger.d(TAG, "Action cancelled")
                presenter.onResultCancelled(data)
            }
            else ->
                Logger.d(TAG, "Unrecognized action")
        }
    }

    override fun showCameraDialog() {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_detail_delivered_photo_title))
                    .setMessage(getString(R.string.delivery_detail_delivered_photo_message))
                    .setPositiveButton(getString(R.string.dialog_ok), { dialog, id ->
                        dialog.dismiss()
                        presenter.deliveryCaptureImageClicked(true)
                    })
                    .setNeutralButton(getString(R.string.dialog_cancel), { dialog, id ->
                        presenter.imageCancelClicked()
                        dialog.dismiss()
                    })
                    .setNegativeButton(getString(R.string.delivery_detail_no_picture_message), { dialog, id ->
                        presenter.deliveryCaptureImageClicked(false)
                        dialog.dismiss()
                    })
                    .create().show()
        }
    }

    override fun showInitializationErrorDialog() {
        showDialog(
                getString(R.string.delivery_detail_error_title),
                getString(R.string.delivery_detail_error_message),
                true)
    }

    override fun showDeliveredRequestSuccess() {
       Toast.makeText(
               this,
               getString(R.string.delivery_detail_delivered_image_success_message),
               Toast.LENGTH_SHORT).show()
    }

    override fun showDeliveredRequestFailure() {
        showDialog(
                getString(R.string.delivery_detail_error_title),
                getString(R.string.delivery_detail_delivered_image_failure_message),
                false)
    }

    override fun showImageUploadConfirmDialog(bitmap: Bitmap) {
        if( !isFinishing ) {
            Logger.d(TAG, "Attempting to set bitmap: $bitmap")
            val dialog = Dialog(this, R.style.alertDialogStyle)
            dialog.setContentView(R.layout.dialog_image_upload_confirm)
            dialog.setCancelable(true)
            dialog.window.setLayout(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT)

            val imageView = dialog.findViewById<ImageView>(R.id.detailsConfirmImageView)
            imageView.scaleType = ImageView.ScaleType.CENTER_CROP
            imageView?.setImageBitmap(bitmap)

            val detailsCancelTextView = dialog.findViewById<TextView>(R.id.detailsCancelTextView)
            detailsCancelTextView.setOnClickListener {
                presenter.imageCancelClicked()
                dialog.dismiss()
            }

            val detailsRetakeTextView = dialog.findViewById<TextView>(R.id.detailsRetakeTextView)
            detailsRetakeTextView.setOnClickListener {
                presenter.imageRetryClicked()
                dialog.dismiss()
            }

            val detailsSendTextView = dialog.findViewById<TextView>(R.id.detailsSendTextView)
            detailsSendTextView.setOnClickListener {
                presenter.imageSendClicked()
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun showDialog(title: String, message: String, isFinish: Boolean) {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.dialog_ok), { dialog, id ->
                        dialog.dismiss()
                        if (isFinish) {
                            finish()
                        }
                    }).create().show()
        }
    }
}
