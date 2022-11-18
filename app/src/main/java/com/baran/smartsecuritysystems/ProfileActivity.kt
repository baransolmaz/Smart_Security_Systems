package com.baran.smartsecuritysystems

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.baran.smartsecuritysystems.databinding.ActivityHomeBinding
import com.baran.smartsecuritysystems.databinding.ActivityProfileBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private lateinit var userName: String
    private lateinit var deviceId: String
    private var database: DatabaseReference = Firebase.database.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        overridePendingTransition(1,0)

        binding= ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val extras = intent.extras
        if (extras != null) {
            userName = extras.getString("USERNAME").toString()
            deviceId = extras.getString("DEVICE_ID").toString()
        }

        database.child("Users").child(userName).addListenerForSingleValueEvent( object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                var firstName=dataSnapshot.child("firstName").value.toString()
                var lastName=dataSnapshot.child("lastName").value.toString()
                var mail=dataSnapshot.child("mail").value.toString()
                firstName="Name: $firstName"
                lastName="Surname: $lastName"
                var username="Username: $userName"
                mail= "E-mail: $mail"
                binding.profileName.text=firstName
                binding.profileSurname.text=lastName
                binding.profileUsername.text=username
                binding.profileMail.text=mail
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", databaseError.message)

            }

        })

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