package com.baran.smartsecuritysystems

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationItemView

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        overridePendingTransition(1,0)
        setContentView(R.layout.activity_settings)

        val navBarHome=findViewById<BottomNavigationItemView>(R.id.home_nav)
        navBarHome.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
        val navBarProfile=findViewById<BottomNavigationItemView>(R.id.profile_nav)
        navBarProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
        val navBarSettings=findViewById<BottomNavigationItemView>(R.id.settings_nav)
        navBarSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}