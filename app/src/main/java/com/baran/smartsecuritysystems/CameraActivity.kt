package com.baran.smartsecuritysystems

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.baran.smartsecuritysystems.databinding.ActivityCameraBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class CameraActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var userName: String
    private lateinit var deviceId: String
    private var database: DatabaseReference = Firebase.database.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN

        binding= ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            userName = extras.getString("USERNAME").toString()
            deviceId = extras.getString("DEVICE_ID").toString()
        }

    }
}