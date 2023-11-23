package com.brij1999.vitaarth.utils

import android.app.*
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.provider.Telephony
import android.util.Log
import androidx.core.app.NotificationCompat
import com.brij1999.vitaarth.R
import com.brij1999.vitaarth.io.SmsReceiver
import com.brij1999.vitaarth.ui.main.MainActivity


class SmsForegroundService : Service() {
    private val TAG = "SmsForegroundService"
    
    private val smsReceiver: SmsReceiver = SmsReceiver()

    private val NOTIFICATION_TITLE = "Vitaarth : Listening for transactions"
    private val NOTIFICATION_TEXT = "Running in the background"

    companion object {
        var isAlive = false
        const val NOTIFICATION_ID = 101
        const val CHANNEL_ID = "ForegroundServiceChannel"
        const val CHANNEL_NAME = "Foreground Service Channel"
        const val CHANNEL_IMPORTANCE = NotificationManager.IMPORTANCE_DEFAULT
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: hello")
        isAlive = true
        registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand: hola!")
        startForeground(NOTIFICATION_ID, createNotification())
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: bye")
        unregisterReceiver(smsReceiver)
        stopForeground(STOP_FOREGROUND_REMOVE)
        isAlive = false
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
        val manager: NotificationManager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(NOTIFICATION_TITLE)
            .setContentText(NOTIFICATION_TEXT)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
