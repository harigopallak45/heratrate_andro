package com.harigopallak.thyroid

import android.content.Context
import android.content.Intent
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawerLayout = findViewById(R.id.drawer_layout)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_menu) // Replace with your menu icon

        firebaseAuth = FirebaseAuth.getInstance()
        usersRef = FirebaseDatabase.getInstance().reference

        if (!isUserLoggedIn()) {
            // If the user is not logged in, navigate to the login screen
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }

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

        logOut = findViewById(R.id.logout)
        heartRate = findViewById(R.id.heartrate)
        listView = findViewById(R.id.list_view)

        historyList = mutableListOf("70 bpm", "72 bpm", "75 bpm", "73 bpm", "71 bpm")
        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, historyList)
        listView.adapter = adapter

        logOut.setOnClickListener {
            signOutUser()
        }

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun signInUser() {
        val email = "example@email.com" // Replace with the user's email
        val password = "password" // Replace with the user's password

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = firebaseAuth.currentUser
                    saveLoginState(true)
                    Toast.makeText(this, "Authentication successful.", Toast.LENGTH_SHORT).show()
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signOutUser() {
        firebaseAuth.signOut()
        saveLoginState(false)
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
            R.id.nav_heart_rate -> {
                // Show heart rate
                Toast.makeText(this, "Showing current heart rate", Toast.LENGTH_SHORT).show()
                // Implement logic to display current heart rate
                displayHeartRate()
            }
            R.id.nav_connection -> {
                // Navigate to connection screen
                Toast.makeText(this, "Navigating to connection screen", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, ConnectionActivity::class.java))
            }
            R.id.nav_history -> {
                // Show history of heart rates
                Toast.makeText(this, "Showing history of heart rates", Toast.LENGTH_SHORT).show()
                displayHeartRateHistory()
            }
            R.id.nav_switch_account -> {
                // Switch account logic
                Toast.makeText(this, "Switching account", Toast.LENGTH_SHORT).show()
                switchAccount()
            }
            R.id.nav_logout -> {
                // Sign out user
                signOutUser()
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun displayHeartRate() {
        // Implement logic to display the current heart rate
        val currentHeartRateTextView: TextView = findViewById(R.id.currentHeartRate)
        currentHeartRateTextView.text = "Current Heart Rate: --"
    }

    private fun displayHeartRateHistory() {
        // Implement logic to display the history of heart rates
        historyList.addAll(listOf("69 bpm", "70 bpm", "68 bpm"))
        adapter.notifyDataSetChanged()
    }

    private fun switchAccount() {
        // Implement logic to switch accounts
        firebaseAuth.signOut()
        saveLoginState(false)
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
