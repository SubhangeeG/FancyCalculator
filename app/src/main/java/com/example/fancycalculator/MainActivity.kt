package com.example.fancycalculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Stack

class MainActivity : AppCompatActivity() {

    private lateinit var inputTextView: TextView
    private lateinit var resultTextView: TextView

    // Variables to keep track of the current expression
    private var expression = ""
    private var lastNumeric = false
    private var stateError = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the TextViews for input and result
        inputTextView = findViewById(R.id.input)
        resultTextView = findViewById(R.id.result)

        // Set up click listeners for each button
        setupButtons()
    }

    private fun setupButtons() {
        // Numeric buttons (0-9)
        val buttonIds = arrayOf(
            R.id.button0, R.id.button1, R.id.button2, R.id.button3,
            R.id.button4, R.id.button5, R.id.button6, R.id.button7,
            R.id.button8, R.id.button9
        )

        for (id in buttonIds) {
            findViewById<Button>(id).setOnClickListener { onDigit((it as Button).text.toString()) }
        }

        // Operation buttons (+, -, X, /)
        findViewById<Button>(R.id.buttonAdd).setOnClickListener { onOperator("+") }
        findViewById<Button>(R.id.buttonSubtract).setOnClickListener { onOperator("-") }
        findViewById<Button>(R.id.buttonMultiply).setOnClickListener { onOperator("*") }
        findViewById<Button>(R.id.buttonDivide).setOnClickListener { onOperator("/") }

        // Special buttons
        findViewById<Button>(R.id.buttonClear).setOnClickListener { onClear() }
        findViewById<Button>(R.id.buttonDelete).setOnClickListener { onDelete() }
        findViewById<Button>(R.id.buttonDot).setOnClickListener { onDecimalPoint() }
        findViewById<Button>(R.id.buttonEqual).setOnClickListener { onEqual() }
    }

    private fun onDigit(digit: String) {
        if (stateError) {
            // If there's an error, reset the expression
            expression = ""
            stateError = false
        }
        expression += digit
        inputTextView.text = expression
        lastNumeric = true
    }

    private fun onOperator(operator: String) {
        if (lastNumeric && !stateError) {
            expression += " $operator "
            inputTextView.text = expression
            lastNumeric = false
        }
    }

    private fun onClear() {
        // Reset all values
        expression = ""
        inputTextView.text = ""
        resultTextView.text = ""
        lastNumeric = false
        stateError = false
    }

    private fun onDelete() {
        if (expression.isNotEmpty() && !stateError) {
            expression = expression.dropLast(1)
            inputTextView.text = expression
        }
    }

    private fun onDecimalPoint() {
        if (lastNumeric && !stateError && !expression.endsWith(".")) {
            expression += "."
            inputTextView.text = expression
            lastNumeric = false
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onEqual() {
        if (lastNumeric && !stateError) {
            try {
                // Evaluate the expression and display the result
                val result = evaluateExpression(expression)
                resultTextView.text = "$result"
            } catch (e: Exception) {
                resultTextView.text = "Error"
                stateError = true
                lastNumeric = false
            }
        }
    }

    // Function to evaluate the expression using BigDecimal and a basic stack-based algorithm
    private fun evaluateExpression(exp: String): BigDecimal {
        val tokens = exp.split(" ")
        val values = Stack<BigDecimal>()
        val operators = Stack<String>()

        for (token in tokens) {
            when {
                token.isNumber() -> values.push(BigDecimal(token))
                token.isOperator() -> {
                    while (operators.isNotEmpty() && hasPrecedence(token, operators.peek())) {
                        val result = applyOperation(operators.pop(), values.pop(), values.pop())
                        values.push(result)
                    }
                    operators.push(token)
                }
            }
        }

        // Apply remaining operations
        while (operators.isNotEmpty()) {
            val result = applyOperation(operators.pop(), values.pop(), values.pop())
            values.push(result)
        }
        return values.pop()
    }


    private fun applyOperation(op: String, b: BigDecimal, a: BigDecimal): BigDecimal {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> a.divide(b, 10, RoundingMode.HALF_UP)  // Updated to use RoundingMode.HALF_UP
            else -> throw UnsupportedOperationException("Unknown operator")
        }
    }


    private fun hasPrecedence(op1: String, op2: String): Boolean {
        return (op2 == "*" || op2 == "/") && (op1 == "+" || op1 == "-")
    }

    private fun String.isNumber(): Boolean {
        return this.toBigDecimalOrNull() != null
    }

    private fun String.isOperator(): Boolean {
        return this == "+" || this == "-" || this == "*" || this == "/"
    }
}
