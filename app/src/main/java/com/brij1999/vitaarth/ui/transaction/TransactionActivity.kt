package com.brij1999.vitaarth.ui.transaction

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
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
import java.net.URLDecoder


class TransactionActivity : AppCompatActivity() {
    private var upiString: String? = null
    private var transactionId: String? = null
    private lateinit var transaction: Transaction

    private lateinit var amountEditText: EditText
    private lateinit var accountEditText: EditText
    private lateinit var typeEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var upiButton: Button

    companion object {
        private const val TAG = "TransactionActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)
        upiString = intent.getStringExtra("UPI_STRING")
        transactionId = intent.getStringExtra("TRANSACTION_ID")

        if(transactionId!=null) {
            runBlocking {
                val res = Transaction.fetch(transactionId!!)
                transaction = res ?: Transaction()

                // TODO: if res==null, handle transactionId not found

            }
        } else {
            transaction = Transaction()
        }

        if (upiString!=null) {
            parseUpiString()
        }

        // Setup UI elements with respective initializations
        setupUiFields()

        // Populate the views with transaction details
        populateTransactionDetails()
    }

    private fun parseUpiString() {
        val uri = Uri.parse(upiString)
        val query = uri.query
        val params = query!!.split("&")
            .map { it.split("=") }
            .associate { it[0] to it[1] }

        transaction.amount = params["a"]?.toDoubleOrNull()?.let { -it }
        params["tn"]?.let { transaction.description = URLDecoder.decode(params["tn"], "UTF-8") }
        params["pa"]?.let { transaction.extraParams.put("payee_vpa", it) }
        params["pn"]?.let { transaction.extraParams.put("payee_name", it) }
        params["pa"]?.let { transaction.extraParams.put("payee_vpa", it) }
        params["mc"]?.let { transaction.extraParams.put("merchant_code", it) }
        params["tid"]?.let { transaction.extraParams.put("upi_txn_id", it) }
        params["tr"]?.let { transaction.extraParams.put("upi_txn_ref_id", it) }
        params["cu"]?.let { transaction.extraParams.put("currency", it) }
        params["url"]?.let { transaction.extraParams.put("details_url", it) }

        Log.d(TAG, "parseUpiString: $transaction")
    }

    private fun setupUiFields() {
        amountEditText = findViewById(R.id.amountEditText)
        accountEditText = findViewById(R.id.accountEditText)
        typeEditText = findViewById(R.id.typeEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)

        saveButton = findViewById(R.id.saveButton)
        if (upiString==null)    saveButton.visibility = View.VISIBLE
        saveButton.setOnClickListener { onSaveButtonClick() }

        upiButton = findViewById(R.id.upiButton)
        if (upiString!=null)    upiButton.visibility = View.VISIBLE
        upiButton.setOnClickListener { onUpiButtonClick() }
    }

    private fun populateTransactionDetails() {
        amountEditText.setText(transaction.amount?.toString() ?: "")
        accountEditText.setText(transaction.account)
        typeEditText.setText(transaction.type)
        descriptionEditText.setText(transaction.description)
    }

    private fun onSaveButtonClick() {
        saveTransaction()
        finish()
    }

    private fun onUpiButtonClick() {
        saveTransaction()
        initiateUpiPayment()
        finish()
    }

    private fun saveTransaction() {
        // Update transaction with latest values from UI components
        transaction.amount = amountEditText.text.toString().toDoubleOrNull()
        transaction.account = accountEditText.text.toString().takeIf { it.isNotEmpty() }
        transaction.type = typeEditText.text.toString().takeIf { it.isNotEmpty() }
        transaction.description = descriptionEditText.text.toString().takeIf { it.isNotEmpty() }

        // Update the transaction in Firebase Firestore
        MainScope().launch(Dispatchers.Main) {
            withContext(Dispatchers.IO) {
                transaction.save("UPDATE -> APP | $TAG | updateTransaction")
            }
            Toast.makeText(
                this@TransactionActivity,
                "Transaction updated successfully",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun initiateUpiPayment() {
        val uri = Uri.parse(upiString)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        try {
            startActivity(intent)
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(this@TransactionActivity, "No UPI app found", Toast.LENGTH_SHORT).show()
        }
    }
}
