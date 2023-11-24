package com.brij1999.vitaarth.io

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import com.brij1999.vitaarth.R
import com.brij1999.vitaarth.data.Template
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class DailySmsAuditReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DailySmsAuditReceiver"
        const val SMS_AUDIT_ALARM_REQUEST_CODE = 123
        const val SMS_AUDIT_NOTIFICATION_ID = 456
        const val CHANNEL_ID = "SMSAuditAlertsChannel"
        const val CHANNEL_NAME = "SMS Audit Alerts Channel"
        const val CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
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

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("SMS Audit Complete")
            .setContentText("${cursor!!.count} sms were audited.")

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(SMS_AUDIT_NOTIFICATION_ID, notificationBuilder.build())

        cursor.use { c ->
            while (c.moveToNext()) {
                val smsSender = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.ADDRESS))
                val smsMessage = c.getString(c.getColumnIndexOrThrow(Telephony.Sms.BODY))
                val smsTimestamp = c.getLong(c.getColumnIndexOrThrow(Telephony.Sms.DATE))

                GlobalScope.launch(Dispatchers.IO) {
                    val transaction = Template.assimilate(smsSender, smsMessage, smsTimestamp)
                }
            }
        }
    }
}