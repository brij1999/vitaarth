package com.brij1999.vitaarth.utils

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brij1999.vitaarth.R
import java.text.SimpleDateFormat
import java.util.*

class DailySMSAudit : BroadcastReceiver() {
    private val TAG = "DailySMSAudit"

    companion object {
        const val SMS_AUDIT_ALARM_REQUEST_CODE = 123
        const val SMS_AUDIT_NOTIFICATION_ID = 456
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val currentDate = System.currentTimeMillis()
        val sevenDaysAgo = currentDate - 7 * 24 * 60 * 60 * 1000

        val selection = "${Telephony.Sms.DATE} >= ? AND ${Telephony.Sms.TYPE} = ${Telephony.Sms.MESSAGE_TYPE_INBOX}"

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

        val notificationBuilder = NotificationCompat.Builder(context, "SMS_AUDIT_ALERTS")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Audit Complete")
            .setContentText("${cursor!!.count} sms were audited.")

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(SMS_AUDIT_NOTIFICATION_ID, notificationBuilder.build())
    }
}