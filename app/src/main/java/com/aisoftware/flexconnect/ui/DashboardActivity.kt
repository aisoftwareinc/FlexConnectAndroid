package com.aisoftware.flexconnect.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.MenuItem
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.adapter.DeliveryAdapter
import com.aisoftware.flexconnect.adapter.DeliveryAdapterItemCallback
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.ui.main.MainActivity
import com.aisoftware.flexconnect.util.SharedPrefUtil
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_dashboard.*


class DashboardActivity : AppCompatActivity(), DeliveryAdapterItemCallback {

    private val TAG = DashboardActivity::class.java.simpleName
    private val GOOGLE_SERVICES_REQUEST_CODE = 9
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeliveryAdapter

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent = Intent(context, DashboardActivity::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar = findViewById<Toolbar>(R.id.dashboardToolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        initializeRecyclerView()

        val sharedPrefUtil = SharedPrefUtil(this)
        val phoneNumber = sharedPrefUtil.getUserPref(false)
        val model = ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        model.getDeliveries(phoneNumber).observe(this, Observer<List<DeliveryEntity>> { deliveries ->

            if( dashboardSwipeLayout.isRefreshing ) {
                dashboardSwipeLayout.isRefreshing = false
            }

            if (deliveries != null) {
                Log.d(TAG, "Updating deliveries list with items: $deliveries")
                if (deliveries.isEmpty()) {
                    showNoDeliveriesDialog()
                } else {
                    adapter.updateList(deliveries)
                }
            } else {
                showErrorDialog()
            }
        })

        dashboardSwipeLayout.setOnRefreshListener {
            model.getDeliveries(phoneNumber)
        }
    }

    override fun onResume() {
        super.onResume()
        val avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when (avail) {
            ConnectionResult.SERVICE_MISSING -> showPlayServicesDialog(ConnectionResult.SERVICE_MISSING)
            ConnectionResult.SERVICE_DISABLED -> showPlayServicesDialog(ConnectionResult.SERVICE_DISABLED)
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> showPlayServicesDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            ConnectionResult.SERVICE_INVALID -> showPlayServicesDialog(ConnectionResult.SERVICE_INVALID)
        }
    }

    override fun onBackPressed() {
        showLogoutDialog()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return true
    }

    private fun showPlayServicesDialog(errorCode: Int) {
        val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, GOOGLE_SERVICES_REQUEST_CODE)
        if (!isFinishing) {
            dialog.show()
        }
    }

    private fun initializeRecyclerView() {
        Log.d(TAG, "Initializing recyclerview")
        recyclerView = findViewById(R.id.dashboardRecyclerView)
        adapter = DeliveryAdapter(this, this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        Log.d(TAG, "Completed initializing recyclerview")
    }

    override fun onItemClicked(deliveryEntity: DeliveryEntity) {
        Log.d(TAG, "Item clicked: $deliveryEntity")
        val intent = DeliveryDetailActivity.getInstance(this, deliveryEntity)
        startActivity(intent)
    }

    private fun logout() {
        val sharedPrefUtil = SharedPrefUtil(this)
        sharedPrefUtil.getUserPref(true)
        navigateToMain()
        finish()
    }

    private fun navigateToMain() {
        val intent = MainActivity.getIntent(this)
        startActivity(intent)
    }

    private fun showNoDeliveriesDialog() {
        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
                getString(R.string.delivery_detail_no_deliveries_message))
    }

    private fun showErrorDialog() {
        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
                getString(R.string.delivery_detail_no_deliveries_message))
    }

    private fun showDialog(title: String, message: String) {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(getString(R.string.delivery_logout_pos_button), { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }

    private fun showLogoutDialog() {
        if (!isFinishing) {
            AlertDialog.Builder(this, R.style.alertDialogStyle)
                    .setTitle(getString(R.string.delivery_logout_title))
                    .setMessage(getString(R.string.delivery_logout_message))
                    .setPositiveButton(getString(R.string.delivery_logout_pos_button), { dialog, id ->
                        logout()
                    })
                    .setNegativeButton(getString(R.string.delivery_logout_neg_button), { dialog, id ->
                        dialog.dismiss()
                    }).create().show()
        }
    }
}
