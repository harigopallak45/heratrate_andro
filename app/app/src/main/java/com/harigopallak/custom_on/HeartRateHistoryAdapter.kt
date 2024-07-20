package com.harigopallak.thyroid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class HeartRateRecord(val timestamp: String, val heartRate: String)

class HeartRateHistoryAdapter(private val historyList: List<HeartRateRecord>) :
    RecyclerView.Adapter<HeartRateHistoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timestamp: TextView = view.findViewById(R.id.timestamp)
        val heartRate: TextView = view.findViewById(R.id.heart_rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_heart_rate_record, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val record = historyList[position]
        holder.timestamp.text = record.timestamp
        holder.heartRate.text = record.heartRate
    }

    override fun getItemCount() = historyList.size
}
