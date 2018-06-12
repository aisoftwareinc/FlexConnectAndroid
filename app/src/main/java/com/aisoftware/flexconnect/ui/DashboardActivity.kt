package com.aisoftware.flexconnect.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.adapter.DeliveryAdapter
import com.aisoftware.flexconnect.adapter.DeliveryAdapterItemCallback
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.ui.detail.DeliveryDetailActivity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.CrashLogger
import com.aisoftware.flexconnect.util.Logger
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModel
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModelFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.bottom_nav_layout.*

class DashboardActivity : FlexConnectActivityBase(), DeliveryAdapterItemCallback {

    private val TAG = DashboardActivity::class.java.simpleName
    private val GOOGLE_SERVICES_REQUEST_CODE = 9
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeliveryAdapter
    private var refreshList = true

    companion object {
        @JvmStatic
        fun getIntent(context: Context, refreshList: Boolean = true): Intent {
            val intent = Intent(context, DashboardActivity::class.java)
            intent.putExtra(Constants.REFRESH_LIST_KEY, refreshList)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        initializeToolbar()
        initializeRecyclerView()

        refreshList = intent.getBooleanExtra(Constants.REFRESH_LIST_KEY, true)
        val phoneNumber = getSharedPrefUtil().getUserPref(false)
        val factory = DeliveryViewModelFactory(application, getNetworkService())
        val model = ViewModelProviders.of(this, factory).get(DeliveryViewModel::class.java)
        model.getDeliveries(phoneNumber, refreshList).observe(this, Observer<List<Delivery>> { deliveries ->
            if (dashboardSwipeLayout.isRefreshing) {
                dashboardSwipeLayout.isRefreshing = false
            }

            if (deliveries != null && deliveries.isNotEmpty()) {
                Logger.d(TAG, "Updating deliveries list with items: $deliveries, and refresh flag: $refreshList")
                adapter.updateList(deliveries)
            }
            else {
//                showNoDeliveriesDialog()
            }
        })

        bottomNavPhoneNumber.setOnClickListener {
            Logger.d(TAG, "Bottom nav phone number clicked")
            navigateToMain()
        }

        bottomNavDeliveries.setOnClickListener {
            // NOOP
            Logger.d(TAG, "Bottom nav deliveries clicked")
        }

        // Pull down to refresh
        dashboardSwipeLayout.setOnRefreshListener {
            model.getDeliveries(phoneNumber, true)
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

    private fun initializeToolbar() {
//        val toolbar = findViewById<Toolbar>(R.id.dashboardToolbar)
//        setSupportActionBar(toolbar)
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun initializeRecyclerView() {
//        val drawable = ContextCompat.getDrawable(this, R.drawable.recyclerview_divider)

        recyclerView = findViewById(R.id.dashboardRecyclerView)
//        val decoration = DividerItemDecoration(this, LinearLayoutManager.HORIZONTAL)

        adapter = DeliveryAdapter(this, this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)

//        drawable?.let {
//            decoration.setDrawable(drawable)
//        }
//        recyclerView.addItemDecoration(decoration)

        recyclerView.adapter = adapter

    }

    override fun onItemClicked(delivery: Delivery) {
        Logger.d(TAG, "Item clicked: $delivery")
        val intent = DeliveryDetailActivity.getInstance(this, delivery)
        startActivity(intent)
    }

    override fun logout() {
        super.logout()
        navigateToMain()
        finish()
    }

    private fun showNoDeliveriesDialog() {
        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
                getString(R.string.delivery_detail_no_deliveries_message))
    }

    private fun showErrorDialog() {
        CrashLogger.log(1, TAG, "Showing error retrieving deliveries dialog")
        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
                getString(R.string.delivery_detail_no_deliveries_error_message))
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
