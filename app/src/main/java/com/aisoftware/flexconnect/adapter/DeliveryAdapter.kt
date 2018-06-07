package com.aisoftware.flexconnect.adapter

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.model.Delivery
import com.aisoftware.flexconnect.util.ConverterUtil
import com.aisoftware.flexconnect.util.Logger
import kotlinx.android.synthetic.main.delivery_list_item.view.*

interface DeliveryAdapterItemCallback {
    fun onItemClicked(delivery: Delivery)
}

class DeliveryAdapter(val context: Context, val callback: DeliveryAdapterItemCallback): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TAG = DeliveryAdapter::class.java.simpleName
    private var items = ArrayList<Delivery>()

    override fun getItemCount(): Int = items.size

    fun updateList(updateItems: List<Delivery>) {
        Logger.d(TAG, "Attempting to update adapter list with items: $updateItems")
        val diffResult = DiffUtil.calculateDiff(DeliveriesDiffCallback(items, updateItems))
        items.clear()
        items.addAll(updateItems)
        diffResult.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val vh = DeliveriesViewHolder(LayoutInflater.from(context).inflate(R.layout.delivery_list_item, parent, false))
        vh.containerLayout.setOnClickListener {
            val pos = vh.adapterPosition
            callback.onItemClicked(items[pos])
        }
        return vh
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val deliveryItem = items[position]
        with(holder as DeliveriesViewHolder) {
            deliveryNameTextView.text = deliveryItem.customerName
            deliveryAddress1TextView.text = deliveryItem.address
            deliveryAddress2TextView.text = ConverterUtil.formatExtendedAddress(deliveryItem)
            statusValueTextView.text = deliveryItem.status
            etaValueTextView.text = deliveryItem.distance
            timeValueTextView.text = deliveryItem.time
            distanceValueTextView.text = deliveryItem.miles

            if( position%2 == 0 ) {
                containerLayout.setBackgroundColor(context.getColor(R.color.colorAlternateRow))
            }
            else {
                containerLayout.setBackgroundColor(context.getColor(R.color.colorWhite))
            }
        }

    }
}

class DeliveriesViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var containerLayout = view.containerLayout
    var deliveryNameTextView = view.deliveryNameTextView
    var deliveryAddress1TextView = view.deliveryAddress1TextView
    var deliveryAddress2TextView = view.deliveryAddress2TextView
    var statusValueTextView = view.statusValueTextView
    var etaValueTextView = view.etaValueTextView
    var timeValueTextView = view.timeValueTextView
    var distanceValueTextView = view.distanceValueTextView
}