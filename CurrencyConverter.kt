package com.example.calculator
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.google.gson.Gson

data class ExchangeRates(val rates: Map<String, Double>)
class CurrencyConverter {
    private val baseUrl = "https://open.er-api.com/v6/latest"

    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double? {
        val (_, response, result) = Fuel.get(baseUrl, listOf("from" to fromCurrency, "to" to toCurrency, "amount" to amount))
            .responseString()

        return when (result) {
            is Result.Success -> {
                val data = Gson().fromJson(result.value, ExchangeRates::class.java)
                val conversionRate = data.rates[toCurrency]
                conversionRate?.times(amount)
            }
            is Result.Failure -> {
                println("Error: ${response.statusCode} - ${response.responseMessage}")
                null
            }
        }
    }
}