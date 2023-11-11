package com.brij1999.vitaarth.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class DailySMSAudit : BroadcastReceiver() {
    private val TAG = "DailySMSAudit"
    private val SMS_PERMISSION_REQUEST_CODE = 101

    companion object {
        const val ALARM_REQUEST_CODE: Int = 123
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val currentDate = System.currentTimeMillis()
        val sevenDaysAgo = currentDate - 7 * 24 * 60 * 60 * 1000

        val selection =
            "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.TYPE} = ${Telephony.Sms.MESSAGE_TYPE_INBOX}"

        val selectionArgs = arrayOf(sevenDaysAgo.toString())

        val cursor = context!!.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use { c ->
            while (c.moveToNext()) {
                val address = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val body = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val dateInMillis = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                val sdf = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault())
                val formattedDate = sdf.format(Date(dateInMillis))

                Log.d(TAG, "Address: $address, Body: $body, Date: $formattedDate")
            }
        }
    }

    private fun logMessages(messages: List<String>) {
        for (message in messages) {
            Log.d(TAG, "logMessages: $message")
        }
    }
}