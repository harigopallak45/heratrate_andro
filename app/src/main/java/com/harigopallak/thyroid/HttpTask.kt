package com.harigopallak.thyroid

import android.annotation.SuppressLint
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.CUPCAKE)

class HttpTask(private val callback: (String) -> Unit) : AsyncTask<String, Void, String>() {

    override fun doInBackground(vararg params: String): String? {
        val urlString = params[0]
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.connectTimeout = 5000 // Set connection timeout
            connection.readTimeout = 5000    // Set read timeout
            connection.requestMethod = "GET" // Set request method

            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line).append("\n")
            }
            reader.close()
            response.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            connection.disconnect()
        }
    }

    override fun onPostExecute(result: String?) {
        result?.let {
            callback(it)
        }
    }
}
