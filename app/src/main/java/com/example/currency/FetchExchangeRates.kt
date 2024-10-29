package com.example.currency

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class FetchExchangeRates(private val activity: MainActivity) {
    // Ứng dụng này dùng API của trang web https://app.exchangerate-api.com/ nên số request bị giới hạn. Nếu số request quá nhiều có thể dẫn đến hết API request quota
    private val API_URL = "https://v6.exchangerate-api.com/v6/71a297b758f2814422c341a7/latest/"

    private val rates = mutableMapOf<String, Double>()

    // Phương thức để lấy tỷ giá cho một cặp tiền tệ
    suspend fun getExchangeRate(sourceCurrency: String, targetCurrency: String): Double {
        fetchRates(sourceCurrency)
        return rates[targetCurrency] ?: 1.0
    }

    // Phương thức để lấy tất cả tỷ giá
    private suspend fun fetchRates(baseCurrency: String) {
        withContext(Dispatchers.IO) {
            var urlConnection: HttpURLConnection? = null
            var reader: BufferedReader? = null

            try {
                val url = URL(API_URL + baseCurrency)
                urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.requestMethod = "GET"
                urlConnection.connect()

                val responseCode = urlConnection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = urlConnection.inputStream
                    val stringBuilder = StringBuilder()
                    reader = BufferedReader(InputStreamReader(inputStream))
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line).append("\n")
                    }

                    val jsonResponse = stringBuilder.toString()
                    val jsonObject = JSONObject(jsonResponse)
                    val conversionRates = jsonObject.getJSONObject("conversion_rates")

                    rates.clear() // Xóa các tỷ giá cũ trước khi cập nhật mới
                    val keys = conversionRates.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = conversionRates.getDouble(key)
                        rates[key] = value
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                urlConnection?.disconnect()
                reader?.close()
            }
        }
    }
}
