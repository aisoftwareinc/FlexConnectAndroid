package com.aisoftware.flexconnect.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.ConverterUtil
import kotlinx.android.synthetic.main.activity_delivery_detail.*

class DeliveryDetailActivity : AppCompatActivity() {

    private val TAG = DeliveryDetailActivity::class.java.simpleName
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
        startActivity(mapIntent)
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
}
