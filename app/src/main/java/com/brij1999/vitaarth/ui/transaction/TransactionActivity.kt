package com.brij1999.vitaarth.ui.transaction

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.brij1999.vitaarth.R
import com.brij1999.vitaarth.data.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext


class TransactionActivity : AppCompatActivity() {
    private lateinit var transaction: Transaction

    private lateinit var amountEditText: EditText
    private lateinit var accountEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var updateButton: Button

    private val TAG = "TransactionActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        // Retrieve the Transaction object passed from the previous activity
        runBlocking {
            transaction = Transaction.fetch("rO3dlHDleq1UHVnqAM3d") ?: Transaction(sourceTag = "ADD -> TransactionActivity")
        }

        amountEditText = findViewById(R.id.amountEditText)
        accountEditText = findViewById(R.id.accountEditText)
        typeEditText = findViewById(R.id.typeEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        updateButton = findViewById(R.id.updateButton)

        // Populate the views with transaction details
        populateTransactionDetails()

        // Add listeners to track changes in the text fields field
        addTextChangeListeners()

        // Set an OnClickListener for the Update button
        updateButton.setOnClickListener {
            updateTransaction()
        }
    }

    private fun populateTransactionDetails() {
        amountEditText.setText(transaction.amount.toString())
        accountEditText.setText(transaction.account)
        typeEditText.setText(transaction.type)
        descriptionEditText.setText(transaction.description)
    }

    private fun addTextChangeListeners() {
        amountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                showUpdateButton()
            }
        })

        accountEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                showUpdateButton()
            }
        })

        typeEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                showUpdateButton()
            }
        })

        descriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                showUpdateButton()
            }
        })
    }

    private fun showUpdateButton() {
        updateButton.visibility = View.VISIBLE
    }

    private fun updateTransaction() {
        val updatedTransaction = Transaction(
            amount = amountEditText.text.toString().toDouble(),
            account = accountEditText.text.toString(),
            type = typeEditText.text.toString(),
            description = descriptionEditText.text.toString(),
        )

        // Update the transaction in Firebase Firestore
        MainScope().launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                updatedTransaction.save("UPDATE -> APP | $TAG | updateTransaction")
            }
            Toast.makeText(this@TransactionActivity, "Transaction updated successfully", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
