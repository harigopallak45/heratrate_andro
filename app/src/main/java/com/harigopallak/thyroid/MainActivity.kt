package com.harigopallak.thyroid

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var logOut: Button
    private lateinit var heartRate: TextView
    private lateinit var usersRef: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var heart: ValueEventListener
    private lateinit var listView: ListView
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var historyList: MutableList<String>
    private lateinit var navigationView: NavigationView
    private lateinit var connectButton: Button
    private lateinit var responseTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupToolbar()
        initializeFirebase()
        setupListeners()
        checkUserLoginStatus()  // Check login status when activity starts
        setupHeartRateListener()
        setupListView()
    }

    private fun initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        connectButton = findViewById(R.id.connectButton)
        responseTextView = findViewById(R.id.responseTextView)
        logOut = findViewById(R.id.logout)
        heartRate = findViewById(R.id.heartrate)
        listView = findViewById(R.id.list_view)
        navigationView = findViewById(R.id.nav_view)
    }

    private fun setupToolbar() {
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(android.R.drawable.arrow_down_float) // Replace with your menu icon
    }

    private fun initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference
    }

    private fun setupListeners() {
        connectButton.setOnClickListener {
            val arduinoUrl = "http://your_arduino_ip_address/"
            HttpTask { response ->
                responseTextView.text = response
                val pulseRate = extractPulseRate(response)
                pulseRate?.let {
                    historyList.add(it)
                    adapter.notifyDataSetChanged()
                }
            }.execute(arduinoUrl)
        }

        logOut.setOnClickListener(this)
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun checkUserLoginStatus() {
        if (!isUserLoggedIn()) {
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
        }
    }

    private fun setupHeartRateListener() {
        val user: FirebaseUser? = firebaseAuth.currentUser
        user?.let {
            heart = usersRef.child(it.uid).child("Heartrate").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val value = dataSnapshot.getValue(String::class.java)
                    heartRate.text = value
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("MainActivity", "loadPost:onCancelled", databaseError.toException())
                }
            })
        }
    }

    private fun setupListView() {
        historyList = mutableListOf()
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter
    }

    private fun extractPulseRate(response: String): String? {
        val regex = """Pulse Rate: (\d+) bpm""".toRegex()
        val matchResult = regex.find(response)
        return matchResult?.groups?.get(1)?.value?.plus(" bpm")
    }

    private fun signOutUser() {
        firebaseAuth.signOut()
        saveLoginState(false)  // Update login state to false
        Toast.makeText(this, "User signed out", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.logout -> {
                signOutUser()
            }
            // Handle other onClick events if needed
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_connection -> {
                Toast.makeText(this, "Navigating to connection screen", Toast.LENGTH_SHORT).show()
                displayConnectionSettings()
            }
            R.id.nav_history -> {
                Toast.makeText(this, "Showing history of heart rates", Toast.LENGTH_SHORT).show()
                displayHeartRateHistory()
            }
            R.id.nav_switch_account -> {
                Toast.makeText(this, "Switching account", Toast.LENGTH_SHORT).show()
                switchAccount()
            }
            R.id.nav_logout -> {
                signOutUser()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun displayConnectionSettings() {
        val intent = Intent(this, ConnectionActivity::class.java)
        startActivity(intent)
    }

    private fun displayHeartRateHistory() {
        // Example of adding historical data, replace with actual logic as needed
        historyList.addAll(listOf("69 bpm", "70 bpm", "68 bpm"))
        adapter.notifyDataSetChanged()
    }

    private fun switchAccount() {
        firebaseAuth.signOut()
        saveLoginState(false)  // Update login state to false
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveLoginState(isLoggedIn: Boolean) {
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", isLoggedIn)
            apply()
        }
    }

    private fun isUserLoggedIn(): Boolean {
        val sharedPref = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        return sharedPref.getBoolean("isLoggedIn", false)
    }
}
