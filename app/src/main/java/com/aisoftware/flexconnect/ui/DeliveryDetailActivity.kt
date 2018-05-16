package com.aisoftware.flexconnect.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.util.Constants

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

        if (intent.hasExtra(Constants.DELIVERY_DETAIL)) {
            deliveryEntity = intent.getSerializableExtra(Constants.DELIVERY_DETAIL) as DeliveryEntity
            initializeView(deliveryEntity)
        } else {
            showErrorDialog(getString(R.string.delivery_detail_error_title), getString(R.string.delivery_detail_error_message), true)
        }
    }

    private fun initializeView(deliveryEntity: DeliveryEntity) {
        Log.d(TAG, "Initializing view with delivery: $deliveryEntity")
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
