package com.aisoftware.flexconnect.ui.dashboard

import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.ui.ActivityBaseView

interface DashboardView: ActivityBaseView {
    fun initializeDeliveriesView(deliveries: List<Delivery>)
    fun initializeNoDeliveriesView()
    fun checkGoogleApiAvailability()
}

interface DashboardPresenter {
    fun initialize(deliveries: List<Delivery>?)
    fun onResumeEvent()
    fun onBackPressedEvent()
    fun onBottomNavPhoneClicked()
}

class DashboardPresenterImpl(val view: DashboardView): DashboardPresenter {

    private val TAG = DashboardPresenterImpl::class.java.simpleName

    override fun initialize(deliveries: List<Delivery>?) {
        if( deliveries != null && deliveries.isNotEmpty() ) {
            view.initializeDeliveriesView(deliveries)
        }
        else {
            view.initializeNoDeliveriesView()
        }
    }

    override fun onResumeEvent() {
        view.checkGoogleApiAvailability()
    }

    override fun onBackPressedEvent() {
        view.showLogoutDialog()
    }

    override fun onBottomNavPhoneClicked() {
        view.showLogoutDialog()
    }
}