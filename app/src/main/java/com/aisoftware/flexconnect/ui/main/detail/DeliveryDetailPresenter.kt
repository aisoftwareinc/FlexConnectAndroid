package com.aisoftware.flexconnect.ui.main.detail

interface DeliveryDetailView {

}

interface DeliveryDetailPresenter {

}

class DeliveryDetailPresenterImpl(val view: DeliveryDetailView): DeliveryDetailPresenter {

    private val TAG = DeliveryDetailPresenterImpl::class.java.simpleName


}