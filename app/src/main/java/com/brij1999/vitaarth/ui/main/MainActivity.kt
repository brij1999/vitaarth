package com.brij1999.vitaarth.ui.main

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.brij1999.vitaarth.R
import com.brij1999.vitaarth.ui.transaction.TransactionActivity
import com.brij1999.vitaarth.utils.DailySMSAudit
import com.brij1999.vitaarth.utils.SmsForegroundService
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var scanBtn: Button
    private lateinit var txnBtn: Button
    private val SMS_PERMISSION_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.POST_NOTIFICATIONS)
        } else {
            arrayOf(Manifest.permission.RECEIVE_SMS)
        }
        requestPermissions(permissions, SMS_PERMISSION_CODE)

        // Start the Foreground Service to keep SmsReceiver alive.
        val serviceIntent = Intent(this, SmsForegroundService::class.java)
        startForegroundService(serviceIntent)

        scanBtn = findViewById(R.id.scanBtn)
        scanBtn.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        txnBtn = findViewById(R.id.txnBtn)
        txnBtn.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupDailySMSAudit() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Create an intent for the BroadcastReceiver
        val intent = Intent(this, DailySMSAudit::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            DailySMSAudit.ALARM_REQUEST_CODE,
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
    }
}