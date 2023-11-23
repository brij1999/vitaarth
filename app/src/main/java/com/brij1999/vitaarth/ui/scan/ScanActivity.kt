package com.brij1999.vitaarth.ui.scan

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.brij1999.vitaarth.R
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback

class ScanActivity : AppCompatActivity() {
    private var mCodeScanner: CodeScanner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        //TODO: Handle camera permission

        val scannerView: CodeScannerView = findViewById(R.id.scanner_view)
        mCodeScanner = CodeScanner(this, scannerView)
        mCodeScanner!!.decodeCallback = DecodeCallback { result ->
            runOnUiThread {
                val readText: TextView = findViewById(R.id.read_text)
                readText.text = result.text
            }
        }
        mCodeScanner!!.errorCallback =
            ErrorCallback { thrown -> Log.e("Scan", "CodeScanner error: " + thrown.message) }
    }

    override fun onResume() {
        super.onResume()
        mCodeScanner!!.startPreview()
    }

    override fun onPause() {
        mCodeScanner!!.releaseResources()
        super.onPause()
    }
}