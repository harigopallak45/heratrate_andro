package com.harigopallak.thyroid

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class ConnectionActivity : AppCompatActivity() {

    private lateinit var textViewTitle: TextView
    private lateinit var buttonConnect: Button
    private lateinit var buttonDisconnect: Button
    private lateinit var textViewStatus: TextView

    private var isConnected = false
    private var deviceName: String? = null

    private val REQUEST_PERMISSION_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connection)

        textViewTitle = findViewById(R.id.textViewTitle)
        buttonConnect = findViewById(R.id.buttonConnect)
        buttonDisconnect = findViewById(R.id.buttonDisconnect)
        textViewStatus = findViewById(R.id.textViewStatus)

        buttonConnect.setOnClickListener {
            if (arePermissionsGranted()) {
                connectToDevice()
            } else {
                requestPermissions()
            }
        }

        buttonDisconnect.setOnClickListener {
            disconnectDevice()
        }

        updateConnectionStatus()
    }

    private fun arePermissionsGranted(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val wifiStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_WIFI_STATE)
        val networkStatePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)

        return fineLocationPermission == PackageManager.PERMISSION_GRANTED &&
                coarseLocationPermission == PackageManager.PERMISSION_GRANTED &&
                wifiStatePermission == PackageManager.PERMISSION_GRANTED &&
                networkStatePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE
            ),
            REQUEST_PERMISSION_CODE
        )
    }

    private fun connectToDevice() {
        if (isConnectedToWiFi(this)) {
            val wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiManager.connectionInfo
            val ssid = wifiInfo.ssid

            if (ssid != null && ssid != "<unknown ssid>") {
                isConnected = true
                deviceName = ssid
                fetchDataFromArduino()
            } else {
                isConnected = false
                Toast.makeText(this, "Not connected to any Wi-Fi network", Toast.LENGTH_SHORT).show()
            }
            updateConnectionStatus()
        } else {
            Toast.makeText(this, "Wi-Fi is not enabled or connected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disconnectDevice() {
        isConnected = false
        deviceName = null
        updateConnectionStatus()
        Toast.makeText(this, "Disconnected", Toast.LENGTH_SHORT).show()
    }

    private fun updateConnectionStatus() {
        if (isConnected) {
            textViewStatus.text = "Status: Connected to $deviceName"
        } else {
            textViewStatus.text = "Status: Not Connected"
        }
    }

    private fun fetchDataFromArduino() {
        StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().permitNetwork().build())

        Thread {
            try {
                val url = URL("http://<ARDUINO_IP_ADDRESS>/data") // Replace with your Arduino IP address and endpoint
                val urlConnection = url.openConnection() as HttpURLConnection

                try {
                    val inputStream = urlConnection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?

                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }

                    runOnUiThread {
                        Toast.makeText(this, "Data from Arduino: $response", Toast.LENGTH_LONG).show()
                        saveHeartRateReading(response.toString()) // Save the reading
                    }

                } finally {
                    urlConnection.disconnect()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Failed to fetch data from Arduino", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }
    private fun saveHeartRateReading(reading: String) {
        val sharedPref = getSharedPreferences("HeartRatePrefs", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()

        // Retrieve the existing list of readings
        val readings = sharedPref.getStringSet("heartRateReadings", mutableSetOf())?.toMutableList() ?: mutableListOf()

        // Add new reading
        readings.add(reading)

        // Keep only the last 10 readings
        if (readings.size > 10) {
            readings.removeAt(0)
        }

        // Save updated list back to SharedPreferences
        editor.putStringSet("heartRateReadings", readings.toSet())
        editor.apply()
    }
    private fun isConnectedToWiFi(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                connectToDevice()
            } else {
                Toast.makeText(this, "Permissions are required to connect to the device", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
        }
    }
}
