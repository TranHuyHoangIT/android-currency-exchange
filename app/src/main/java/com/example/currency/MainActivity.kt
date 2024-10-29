package com.example.currency

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var sourceAmount: EditText
    private lateinit var targetAmount: EditText
    private lateinit var sourceCurrency: Spinner
    private lateinit var targetCurrency: Spinner

    private var selectedSourceCurrency = "USD" // Mặc định là USD
    private var selectedTargetCurrency = "VND" // Mặc định là VND

    private lateinit var fetchExchangeRates: FetchExchangeRates

    // Danh sách các đồng tiền và tên quốc gia
    private val currencyList = listOf(
        "United Arab Emirates - AED", "Afghanistan - AFN", "Albania - ALL", "Armenia - AMD",
        "Netherlands Antilles - ANG", "Angola - AOA", "Argentina - ARS", "Australia - AUD",
        "Aruba - AWG", "Azerbaijan - AZN", "Bosnia and Herzegovina - BAM", "Barbados - BBD",
        "Bangladesh - BDT", "Bulgaria - BGN", "Bahrain - BHD", "Burundi - BIF", "Bermuda - BMD",
        "Brunei - BND", "Bolivia - BOB", "Brazil - BRL", "Bahamas - BSD", "Bhutan - BTN",
        "Botswana - BWP", "Belarus - BYN", "Belize - BZD", "Canada - CAD", "Congo - CDF",
        "Switzerland - CHF", "Chile - CLP", "China - CNY", "Colombia - COP", "Costa Rica - CRC",
        "Cuba - CUP", "Cape Verde - CVE", "Czech Republic - CZK", "Djibouti - DJF",
        "Denmark - DKK", "Dominican Republic - DOP", "Algeria - DZD", "Egypt - EGP",
        "Eritrea - ERN", "Ethiopia - ETB", "European Union - EUR", "Fiji - FJD",
        "Falkland Islands - FKP", "Faroe Islands - FOK", "United Kingdom - GBP",
        "Georgia - GEL", "Guernsey - GGP", "Ghana - GHS", "Gibraltar - GIP",
        "Gambia - GMD", "Guinea - GNF", "Guatemala - GTQ", "Guyana - GYD",
        "Hong Kong - HKD", "Honduras - HNL", "Croatia - HRK", "Haiti - HTG",
        "Hungary - HUF", "Indonesia - IDR", "Israel - ILS", "Isle of Man - IMP",
        "India - INR", "Iraq - IQD", "Iran - IRR", "Iceland - ISK", "Jersey - JEP",
        "Jamaica - JMD", "Jordan - JOD", "Japan - JPY", "Kenya - KES",
        "Kyrgyzstan - KGS", "Cambodia - KHR", "Kiribati - KID", "Comoros - KMF",
        "South Korea - KRW", "Kuwait - KWD", "Cayman Islands - KYD", "Kazakhstan - KZT",
        "Laos - LAK", "Lebanon - LBP", "Sri Lanka - LKR", "Liberia - LRD",
        "Lesotho - LSL", "Libya - LYD", "Morocco - MAD", "Moldova - MDL",
        "Madagascar - MGA", "North Macedonia - MKD", "Myanmar - MMK", "Mongolia - MNT",
        "Macau - MOP", "Mauritania - MRU", "Mauritius - MUR", "Maldives - MVR",
        "Malawi - MWK", "Mexico - MXN", "Malaysia - MYR", "Mozambique - MZN",
        "Namibia - NAD", "Nigeria - NGN", "Nicaragua - NIO", "Norway - NOK",
        "Nepal - NPR", "New Zealand - NZD", "Oman - OMR", "Panama - PAB",
        "Peru - PEN", "Papua New Guinea - PGK", "Philippines - PHP", "Pakistan - PKR",
        "Poland - PLN", "Paraguay - PYG", "Qatar - QAR", "Romania - RON",
        "Serbia - RSD", "Russia - RUB", "Rwanda - RWF", "Saudi Arabia - SAR",
        "Solomon Islands - SBD", "Seychelles - SCR", "Sudan - SDG", "Sweden - SEK",
        "Singapore - SGD", "Saint Helena - SHP", "Sierra Leone - SLE", "Sierra Leone - SLL",
        "Somalia - SOS", "Suriname - SRD", "South Sudan - SSP", "São Tomé and Príncipe - STN",
        "Syria - SYP", "Swaziland - SZL", "Thailand - THB", "Tajikistan - TJS",
        "Turkmenistan - TMT", "Tunisia - TND", "Tonga - TOP", "Turkey - TRY",
        "Trinidad and Tobago - TTD", "Tuvalu - TVD", "Taiwan - TWD", "Tanzania - TZS",
        "Ukraine - UAH", "Uganda - UGX", "United States - USD", "Uruguay - UYU",
        "Uzbekistan - UZS", "Venezuela - VES", "Vietnam - VND", "Vanuatu - VUV",
        "Samoa - WST", "Central African Republic - XAF", "East Caribbean - XCD",
        "International Monetary Fund - XDR", "West African CFA - XOF", "Pacific Franc - XPF",
        "Yemen - YER", "South Africa - ZAR", "Zambia - ZMW", "Zimbabwe - ZWL"
    )

    private var isUpdating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sourceAmount = findViewById(R.id.sourceAmount)
        targetAmount = findViewById(R.id.targetAmount)
        sourceCurrency = findViewById(R.id.sourceCurrency)
        targetCurrency = findViewById(R.id.targetCurrency)

        fetchExchangeRates = FetchExchangeRates(this)

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        sourceCurrency.adapter = adapter
        targetCurrency.adapter = adapter

        sourceCurrency.setSelection(currencyList.indexOfFirst { it.contains("USD") })
        targetCurrency.setSelection(currencyList.indexOfFirst { it.contains("VND") })

        sourceCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedSourceCurrency = parent.getItemAtPosition(position).toString().substringAfter("- ")
                convertCurrencyFromSourceToTarget()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        targetCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTargetCurrency = parent.getItemAtPosition(position).toString().substringAfter("- ")
                convertCurrencyFromSourceToTarget()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        sourceAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    convertCurrencyFromSourceToTarget()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        targetAmount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!isUpdating) {
                    convertCurrencyFromTargetToSource()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun convertCurrencyFromSourceToTarget() {
        val amountString = sourceAmount.text.toString()
        if (amountString.isNotEmpty()) {
            val amount = amountString.toDouble()
            getExchangeRateAndConvert(selectedSourceCurrency, selectedTargetCurrency, amount)
        } else {
            targetAmount.text.clear()
        }
    }

    private fun convertCurrencyFromTargetToSource() {
        val amountString = targetAmount.text.toString()
        if (amountString.isNotEmpty()) {
            val amount = amountString.toDouble()
            getExchangeRateAndConvert(selectedTargetCurrency, selectedSourceCurrency, amount, isReverse = true)
        } else {
            sourceAmount.text.clear()
        }
    }

    private fun getExchangeRateAndConvert(sourceCurrency: String, targetCurrency: String, amount: Double, isReverse: Boolean = false) {
        lifecycleScope.launch {
            // Gọi đến hàm lấy tỷ giá
            val exchangeRate = fetchExchangeRates.getExchangeRate(sourceCurrency, targetCurrency)
            val convertedAmount = amount * exchangeRate
            if (isReverse) {
                updateSourceAmount(convertedAmount)
            } else {
                updateTargetAmount(convertedAmount)
            }
        }
    }

    fun updateTargetAmount(convertedAmount: Double) {
        isUpdating = true
        targetAmount.setText(convertedAmount.toString())
        isUpdating = false
    }

    fun updateSourceAmount(convertedAmount: Double) {
        isUpdating = true
        sourceAmount.setText(convertedAmount.toString())
        isUpdating = false
    }
}
