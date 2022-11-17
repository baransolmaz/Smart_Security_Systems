package com.baran.smartsecuritysystems

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class SeparationActivity : AppCompatActivity() {

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        setContentView(R.layout.activity_separation)
        val buttonSepCam = findViewById<Button>(R.id.sep_cam)
        buttonSepCam.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
            finish()
        }
        val buttonSepMon = findViewById<Button>(R.id.sep_mon)
        buttonSepMon.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}