package com.brij1999.vitaarth.ui.main

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.brij1999.vitaarth.R
import com.brij1999.vitaarth.io.DailySmsAuditReceiver
import com.brij1999.vitaarth.ui.scan.ScanActivity
import com.brij1999.vitaarth.ui.transaction.TransactionActivity
import com.brij1999.vitaarth.utils.SmsForegroundService
import java.util.Calendar


class MainActivity : AppCompatActivity() {

    private lateinit var scanBtn: Button
    private lateinit var txnBtn: Button

    companion object {
        private const val TAG = "MainActivity"
        private const val GLOBAL_PERMISSION_REQUEST_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestGlobalPermissions()
        setupNotificationChannels()
        setupSMSForegroundService()
        setupDailySMSAudit()

        scanBtn = findViewById(R.id.scanBtn)
        scanBtn.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        txnBtn = findViewById(R.id.txnBtn)
        txnBtn.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

//        GlobalScope.launch(Dispatchers.Main) {
//            val t = Transaction.fetch("1DjiYRlrZEtOEBskl8xh")
//            Log.d(TAG, "onCreate: $t")
//        }
    }

    private fun setupNotificationChannels() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(
            NotificationChannel(
                DailySmsAuditReceiver.CHANNEL_ID,
                DailySmsAuditReceiver.CHANNEL_NAME,
                DailySmsAuditReceiver.CHANNEL_IMPORTANCE
            )
        )
        notificationManager.createNotificationChannel(
            NotificationChannel(
                SmsForegroundService.CHANNEL_ID,
                SmsForegroundService.CHANNEL_NAME,
                SmsForegroundService.CHANNEL_IMPORTANCE
            )
        )
    }

    private fun requestGlobalPermissions() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(
                Manifest.permission.READ_SMS,
                Manifest.permission.RECEIVE_SMS
            )
        }
        requestPermissions(permissions, GLOBAL_PERMISSION_REQUEST_CODE)
    }

    private fun setupSMSForegroundService() {
        // Start the Foreground Service to keep SmsReceiver alive.
        val serviceIntent = Intent(this, SmsForegroundService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun setupDailySMSAudit() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an intent for the BroadcastReceiver
        val intent = Intent(this, DailySmsAuditReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            DailySmsAuditReceiver.SMS_AUDIT_ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set the alarm to trigger at 12:00 AM every day
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Set the alarm to trigger every day at 12:00 AM
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )

        // fire audit once for testing and init
         sendBroadcast(intent)
    }
}