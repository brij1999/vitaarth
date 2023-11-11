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
        for (smsMessage: SmsMessage in Telephony.Sms.Intents.getMessagesFromIntent(intent)) {
            Log.d(TAG, "[${smsMessage.timestampMillis}]\tMessage from ${smsMessage.originatingAddress}: ${smsMessage.messageBody}")
            GlobalScope.launch(Dispatchers.IO) {
                for (template: Template in TemplateManager.getAllTemplates()) {
                    if (!template.matches(smsMessage))    continue
                    val transaction = template.parse(smsMessage)
                    TransactionManager.addTransaction(transaction)
                    break
                }
            }
        }
    }
}

