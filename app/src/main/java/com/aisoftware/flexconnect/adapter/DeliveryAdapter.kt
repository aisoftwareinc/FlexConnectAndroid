package com.aisoftware.flexconnect.adapter

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.aisoftware.flexconnect.R
import com.aisoftware.flexconnect.db.entity.DeliveryEntity
import com.aisoftware.flexconnect.util.ConverterUtil
import kotlinx.android.synthetic.main.delivery_list_item.view.*

interface DeliveryAdapterItemCallback {
    fun onItemClicked(deliveryEntity: DeliveryEntity)
}

class DeliveryAdapter(val context: Context, val callback: DeliveryAdapterItemCallback): RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val TAG = DeliveryAdapter::class.java.simpleName
    private var items = ArrayList<DeliveryEntity>()

    override fun getItemCount(): Int = items.size

    fun updateList(updateItems: List<DeliveryEntity>) {
        Log.d(TAG, "Attempting to update adapter list with items: $updateItems")
        val diffResult = DiffUtil.calculateDiff(DeliveriesDiffCallback(items, updateItems))
        Log.d(TAG, "Dispatching updated list with items: $diffResult")

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
        val deliveryItem = items.get(position)
        Log.d(TAG, "Attempting to bind view holder with item: $deliveryItem")
        with(holder as DeliveriesViewHolder) {
            deliveryNameTextView.text = deliveryItem.name
            deliveryAddress1TextView.text = deliveryItem.address1

            if( !deliveryItem.address2.isNullOrBlank() ) {
                deliveryAddress2TextView.visibility = View.VISIBLE
                deliveryAddress2TextView.text = deliveryItem.address2
            }
            deliveryAddress3TextView.text = ConverterUtil.formatExtendedAddress(deliveryItem)
            statusValueTextView.text = deliveryItem.status
            etaValueTextView.text = deliveryItem.eta
            timeValueTextView.text = deliveryItem.time
            distanceValueTextView.text = deliveryItem.distance

            if( position%2 == 0 ) {
                containerLayout.setBackgroundColor(context.getColor(R.color.colorAlternateRow))
            }
            else {
                containerLayout.setBackgroundColor(context.getColor(R.color.colorWhite))
            }
        }

    }

    override fun registerAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.registerAdapterDataObserver(observer)
    }

    override fun unregisterAdapterDataObserver(observer: RecyclerView.AdapterDataObserver) {
        super.unregisterAdapterDataObserver(observer)
    }
}

class DeliveriesViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var containerLayout = view.containerLayout
    var deliveryNameTextView = view.deliveryNameTextView
    var deliveryAddress1TextView = view.deliveryAddress1TextView
    var deliveryAddress2TextView = view.deliveryAddress2TextView
    var deliveryAddress3TextView = view.deliveryAddress3TextView
    var statusValueTextView = view.statusValueTextView
    var etaValueTextView = view.etaValueTextView
    var timeValueTextView = view.timeValueTextView
    var distanceValueTextView = view.distanceValueTextView
}