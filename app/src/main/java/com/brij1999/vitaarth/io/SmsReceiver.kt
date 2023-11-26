package com.brij1999.vitaarth.io

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.telephony.SmsMessage
import android.util.Log
import com.brij1999.vitaarth.data.Template
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {

    private val TAG = "SmsReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION)    return
        Log.d(TAG, "onReceive: SMS Intent received")
        for (sms: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            val smsSender = sms.originatingAddress.toString()
            val smsMessage = sms.messageBody
            val smsTimestamp = sms.timestampMillis
            val smsSourceTag = "SYNC -> SMS | $TAG | onReceive"

            GlobalScope.launch(Dispatchers.IO) {
                val transaction = Template.assimilate(smsSender, smsMessage, smsSourceTag, smsTimestamp)
            }
        }
    }
}

