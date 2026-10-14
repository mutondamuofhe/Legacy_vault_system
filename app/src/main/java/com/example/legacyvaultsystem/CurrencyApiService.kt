package com.example.legacyvaultsystem

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

data class CurrencyRates(
    val base: String,
    val zar: Double,
    val eur: Double,
    val gbp: Double,
    val aud: Double,
    val cad: Double,
    val lastUpdated: String,
    val isSuccess: Boolean
)

object CurrencyApiService {
    private const val API_URL = "https://open.er-api.com/v6/latest/USD"
    private val executor = Executors.newSingleThreadExecutor()

    fun fetchLatestRates(onResult: (CurrencyRates) -> Unit) {
        executor.execute {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(API_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 8000
                connection.readTimeout = 8000

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    reader.close()

                    val json = JSONObject(response.toString())
                    val rates = json.getJSONObject("rates")
                    val timeLastUpdate = json.optString("time_last_update_utc", "Recently")

                    val result = CurrencyRates(
                        base = json.optString("base_code", "USD"),
                        zar = rates.optDouble("ZAR", 18.50),
                        eur = rates.optDouble("EUR", 0.92),
                        gbp = rates.optDouble("GBP", 0.78),
                        aud = rates.optDouble("AUD", 1.52),
                        cad = rates.optDouble("CAD", 1.38),
                        lastUpdated = timeLastUpdate,
                        isSuccess = true
                    )

                    postResult(result, onResult)
                } else {
                    postResult(getFallbackRates(), onResult)
                }
            } catch (e: Exception) {
                postResult(getFallbackRates(), onResult)
            } finally {
                connection?.disconnect()
            }
        }
    }

    private fun postResult(result: CurrencyRates, onResult: (CurrencyRates) -> Unit) {
        try {
            val mainLooper = Looper.getMainLooper()
            if (mainLooper != null) {
                Handler(mainLooper).post { onResult(result) }
            } else {
                onResult(result)
            }
        } catch (e: Exception) {
            onResult(result)
        }
    }

    private fun getFallbackRates(): CurrencyRates {
        return CurrencyRates(
            base = "USD",
            zar = 18.50,
            eur = 0.92,
            gbp = 0.78,
            aud = 1.52,
            cad = 1.38,
            lastUpdated = "Offline Cached Rates",
            isSuccess = false
        )
    }
}