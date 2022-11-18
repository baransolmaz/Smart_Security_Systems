package com.baran.smartsecuritysystems

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationItemView

class SettingsActivity : AppCompatActivity() {
    private lateinit var userName: String
    private lateinit var deviceId: String

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        overridePendingTransition(1,0)
        setContentView(R.layout.activity_settings)

        val extras = intent.extras
        if (extras != null) {
            userName = extras.getString("USERNAME").toString()
            deviceId = extras.getString("DEVICE_ID").toString()
        }

        val navBarHome=findViewById<BottomNavigationItemView>(R.id.home_nav)
        navBarHome.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("USERNAME",userName)
            intent.putExtra("DEVICE_ID",deviceId)
            startActivity(intent)
            finish()
        }
        val navBarProfile=findViewById<BottomNavigationItemView>(R.id.profile_nav)
        navBarProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            intent.putExtra("USERNAME",userName)
            intent.putExtra("DEVICE_ID",deviceId)
            startActivity(intent)
            finish()
        }
        val navBarSettings=findViewById<BottomNavigationItemView>(R.id.settings_nav)
        navBarSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("USERNAME",userName)
            intent.putExtra("DEVICE_ID",deviceId)
            startActivity(intent)
            finish()
        }
    }
}