package com.aisoftware.flexconnect.ui.dashboard

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.adapter.DeliveryAdapter
import com.aisoftware.flexconnect.adapter.DeliveryAdapterItemCallback
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.model.EnRouteState
import com.aisoftware.flexconnect.ui.FlexConnectActivityBase
import com.aisoftware.flexconnect.ui.detail.DeliveryDetailActivity
import com.aisoftware.flexconnect.util.Constants
import com.aisoftware.flexconnect.util.Logger
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModel
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModelFactory
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.bottom_nav_layout.*

class DashboardActivity : FlexConnectActivityBase(), DashboardView, DeliveryAdapterItemCallback {

    private val TAG = DashboardActivity::class.java.simpleName
    private val GOOGLE_SERVICES_REQUEST_CODE = 9
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeliveryAdapter
    private var refreshList = true
    private lateinit var presenter: DashboardPresenter

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
        initializeRecyclerView()


        presenter = DashboardPresenterImpl(this)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        refreshList = intent.getBooleanExtra(Constants.REFRESH_LIST_KEY, true)
        val phoneNumber = getSharedPrefUtil().getUserPref(false)
        val factory = DeliveryViewModelFactory(application, getNetworkService())
        val deliveryViewModel = ViewModelProviders.of(this, factory).get(DeliveryViewModel::class.java)
        deliveryViewModel.getDeliveries(phoneNumber, refreshList).observe(this, Observer<List<Delivery>> { deliveries ->
            if (dashboardSwipeLayout.isRefreshing) {
                dashboardSwipeLayout.isRefreshing = false
            }
            presenter!!.initialize(deliveries)
        })

        bottomNavPhoneNumber.setOnClickListener {
            Logger.d(TAG, "Bottom nav phone number clicked")
            presenter.onBottomNavPhoneClicked()
        }

        bottomNavDeliveries.setOnClickListener {
            // NOOP
            Logger.d(TAG, "Bottom nav deliveries clicked")
        }

        dashboardSwipeLayout.setOnRefreshListener {
            deliveryViewModel.getDeliveries(phoneNumber, true)
        }
    }

    override fun initializeDeliveriesView(deliveries: List<Delivery>) {
        Logger.d(TAG, "Updating deliveries list with items: $deliveries, and refresh flag: $refreshList")

        val count = deliveries.filter{ it.status == EnRouteState.ENROUTE.state}.count()
        setEnRouteCount(count)

        dashboardRecyclerView.visibility = View.VISIBLE
        noDeliveriesTextView.visibility = View.GONE
        adapter.updateList(deliveries)
    }

    override fun initializeNoDeliveriesView() {
        dashboardRecyclerView.visibility = View.GONE
        noDeliveriesTextView.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        presenter.onResumeEvent()
    }

    override fun checkGoogleApiAvailability() {
        val avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when (avail) {
            ConnectionResult.SERVICE_MISSING -> showPlayServicesDialog(ConnectionResult.SERVICE_MISSING)
            ConnectionResult.SERVICE_DISABLED -> showPlayServicesDialog(ConnectionResult.SERVICE_DISABLED)
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> showPlayServicesDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            ConnectionResult.SERVICE_INVALID -> showPlayServicesDialog(ConnectionResult.SERVICE_INVALID)
        }
    }

    override fun onBackPressed() {
        presenter.onBackPressedEvent()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        presenter.onBackPressedEvent()
        return true
    }

    private fun showPlayServicesDialog(errorCode: Int) {
        val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, GOOGLE_SERVICES_REQUEST_CODE)
        if (!isFinishing) {
            dialog.show()
        }
    }

    private fun initializeRecyclerView() {
        recyclerView = findViewById(R.id.dashboardRecyclerView)
        adapter = DeliveryAdapter(this, this)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

    }

    override fun onItemClicked(delivery: Delivery) {
        Logger.d(TAG, "Item clicked: $delivery")
        val intent = DeliveryDetailActivity.getInstance(this, delivery)
        startActivity(intent)
    }

//    private fun showNoDeliveriesDialog() {
//        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
//                getString(R.string.delivery_detail_no_deliveries_message))
//    }
//
//    private fun showErrorDialog() {
//        CrashLogger.log(1, TAG, "Showing error retrieving deliveries dialog")
//        showDialog(getString(R.string.delivery_detail_no_deliveries_title),
//                getString(R.string.delivery_detail_no_deliveries_error_message))
//    }
//
//    private fun showDialog(title: String, message: String) {
//        if (!isFinishing) {
//            AlertDialog.Builder(this, R.style.alertDialogStyle)
//                    .setTitle(title)
//                    .setMessage(message)
//                    .setPositiveButton(getString(R.string.delivery_logout_pos_button)) { dialog, id ->
//                        dialog.dismiss()
//                    }.create().show()
//        }
//    }

}
