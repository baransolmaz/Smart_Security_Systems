package com.baran.smartsecuritysystems

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MonitorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_monitor)
    }
}