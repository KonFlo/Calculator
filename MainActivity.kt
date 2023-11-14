package com.example.calculator

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.objecthunter.exp4j.ExpressionBuilder


class MainActivity : AppCompatActivity(){
    private lateinit var displayTextView: TextView
    private val input = StringBuilder()
    private var sourceCurrency: String? = null
    private var targetCurrency: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        displayTextView = findViewById(R.id.displayTextView)
        val gridLayout: GridLayout = findViewById(R.id.gridLayout)

        // Define the button click listener
        val buttonClickListener = View.OnClickListener { view ->
            val button = view as Button
            handleButtonClick(button.text.toString())
        }

        // Add number and operation buttons
        for (i in 0 until gridLayout.childCount) {
            val view: View = gridLayout.getChildAt(i)
            if (view is Button) {
                view.setOnClickListener(buttonClickListener)
            }
        }
    }

    private fun callCurrencyExchange() {
        val currencies = arrayOf("USD", "EUR", "GBP", "JPY")

        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Select Currencies")

        alertDialogBuilder.setItems(currencies) { _, which ->
            if (sourceCurrency == null) {
                sourceCurrency = currencies[which]
                callCurrencyExchange()
            } else {
                targetCurrency = currencies[which]
                performCurrencyConversion()
            }
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun performCurrencyConversion() {
        GlobalScope.launch(Dispatchers.Main) {
            val amount = calculateResult()
            if (sourceCurrency != null && targetCurrency != null) {
                val result = withContext(Dispatchers.IO) {
                    val converter = CurrencyConverter()
                    converter.convert(amount.toDouble(), sourceCurrency!!, targetCurrency!!)
                }

                result?.let {
                    displayTextView.text = result.toString()
                } ?: run {
                    println("Conversion failed. Check your inputs.")
                }
            } else {
                println("Source or target currency is null. Cannot perform conversion.")
            }

            // Reset currencies for the next conversion
            sourceCurrency = null
            targetCurrency = null
        }
    }

    private fun handleButtonClick(buttonText: String) {
        when (buttonText) {
            "=" -> calculateResult()
            "C" -> clearInput()
            "←" -> deleteLastCharacter()
            "Converter" -> callCurrencyExchange()
            else -> appendToInput(buttonText)
        }
    }

    private fun calculateResult(): String {
        try {
            val result = evalExpression(input.toString())
            displayTextView.text = result
            input.setLength(0)
            input.append(result)
            return result
        } catch (e: Exception) {
            displayTextView.text = "Error"
            return "null"
        }
    }

    private fun evalExpression(expression: String): String {
        // Tokenize the expression
        val result = ExpressionBuilder(expression)
            .build()
            .evaluate()

        // Format the result to remove the decimal part if unnecessary
        return if (result % 1 == 0.0) {
            result.toLong().toString()
        } else {
            result.toString()
        }
    }

    private fun clearInput() {
        input.setLength(0)
        displayTextView.text = ""
    }

    private fun deleteLastCharacter() {
        if (input.isNotEmpty()) {
            input.deleteCharAt(input.length - 1)
            displayTextView.text = input.toString()
        }
    }

    private fun appendToInput(value: String) {
        input.append(value)
        displayTextView.text = input.toString()
    }
}