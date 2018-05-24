package com.aisoftware.flexconnect.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.adapter.DeliveryAdapter
import com.aisoftware.flexconnect.adapter.DeliveryAdapterItemCallback
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.viewmodel.DeliveryViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability


class DashboardActivity : AppCompatActivity(), DeliveryAdapterItemCallback {

    private val TAG = DashboardActivity::class.java.simpleName
    private val GOOGLE_SERVICES_REQUEST_CODE = 9
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: DeliveryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        initializeRecyclerView()

        val model = ViewModelProviders.of(this).get(DeliveryViewModel::class.java)
        model.getDeliveries().observe(this, Observer<List<DeliveryEntity>> { deliveries ->
            deliveries?.let {
                Log.d(TAG, "Updating deliveries list with items: $deliveries")
                adapter.updateList(deliveries)
            }
        })
    }

    override fun onResume() {
        super.onResume()
        val avail = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        when(avail) {
            ConnectionResult.SERVICE_MISSING -> showPlayServicesDialog(ConnectionResult.SERVICE_MISSING)
            ConnectionResult.SERVICE_DISABLED -> showPlayServicesDialog(ConnectionResult.SERVICE_DISABLED)
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> showPlayServicesDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            ConnectionResult.SERVICE_INVALID -> showPlayServicesDialog(ConnectionResult.SERVICE_INVALID)
        }
    }

    private fun showPlayServicesDialog(errorCode: Int) {
        val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, errorCode, GOOGLE_SERVICES_REQUEST_CODE)
        if( !isFinishing ) {
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
}
