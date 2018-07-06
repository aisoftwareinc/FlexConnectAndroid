package com.aisoftware.flexconnect.adapter

import android.support.v7.util.DiffUtil
import com.aisoftware.flexconnect.model.Delivery

class DeliveriesDiffCallback(val oldList: List<Delivery>, val newList: List<Delivery>): DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.get(oldItemPosition).id == newList.get(newItemPosition).id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList.get(oldItemPosition).equals(newList.get(newItemPosition))
}