package com.baran.smartsecuritysystems

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baran.smartsecuritysystems.databinding.ActivitySeparationBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SeparationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeparationBinding
    private lateinit var userName: String
    private lateinit var deviceId: String
    private var database: DatabaseReference = Firebase.database.reference
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN

        val extras = intent.extras
        if (extras != null) {
            userName = extras.getString("USERNAME").toString()
            deviceId = extras.getString("DEVICE_ID").toString()
        }

        binding= ActivitySeparationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        database.child("Users").child(userName).child("devices").addListenerForSingleValueEvent( object : ValueEventListener {

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild(deviceId)){
                    val type=dataSnapshot.child(deviceId).value.toString()
                    if (type == "-1") {//Camera
                        val intent =Intent(this@SeparationActivity, CameraActivity::class.java)
                        intent.putExtra("USERNAME",userName)
                        intent.putExtra("DEVICE_ID",deviceId)
                        startActivity(intent)
                        finish()
                    }else if (type== "1"){//Monitor
                        val intent =Intent(this@SeparationActivity, HomeActivity::class.java)
                        intent.putExtra("USERNAME",userName)
                        intent.putExtra("DEVICE_ID",deviceId)
                        startActivity(intent)
                        finish()
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", databaseError.message)

            }

        })
        binding.sepCam.setOnClickListener {
            database.child("Users").child(userName).child("devices").child(deviceId).setValue(-1).addOnSuccessListener {
                val intent = Intent(this, CameraActivity::class.java)
                intent.putExtra("USERNAME",userName)
                intent.putExtra("DEVICE_ID",deviceId)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this@SeparationActivity,"Failed!",Toast.LENGTH_SHORT).show()
            }

        }
        binding.sepMon.setOnClickListener {
            database.child("Users").child(userName).child("devices").child(deviceId).setValue(1).addOnSuccessListener {
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("USERNAME",userName)
                intent.putExtra("DEVICE_ID",deviceId)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this@SeparationActivity,"Failed!",Toast.LENGTH_SHORT).show()
            }

        }
    }

}