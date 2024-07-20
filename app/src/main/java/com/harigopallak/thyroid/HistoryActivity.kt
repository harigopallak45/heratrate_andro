package com.harigopallak.thyroid

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.ArrayAdapter
import android.widget.ListView

class HistoryActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var historyList: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        listView = findViewById(R.id.history_list_view)
        historyList = mutableListOf()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter

        loadHeartRateHistory()
    }

    private fun loadHeartRateHistory() {
        val sharedPref = getSharedPreferences("HeartRatePrefs", Context.MODE_PRIVATE)
        val readings = sharedPref.getStringSet("heartRateReadings", setOf())

        readings?.let {
            historyList.clear()
            historyList.addAll(it)
            adapter.notifyDataSetChanged()
        }
    }
}
