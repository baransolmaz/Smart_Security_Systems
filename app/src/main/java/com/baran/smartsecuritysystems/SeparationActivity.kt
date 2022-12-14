package com.baran.smartsecuritysystems

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.baran.smartsecuritysystems.databinding.ActivitySeparationBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.agora.media.RtcTokenBuilder

class SeparationActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeparationBinding

    private var userName: String=MainActivity.USERNAME
    private var deviceId: String = MainActivity.DEVICE_ID
    private var appID : String=MainActivity.APP_ID // Fill the App ID of your project generated on Agora Console.
    private var appCer :String = MainActivity.APP_CER

    private var database: DatabaseReference = Firebase.database.reference
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN

        val camera0=Camera(1,"e","e")
        val camera1=Camera(2,"e","e")
        val camera2=Camera(3,"e","e")

        val device=Device(deviceId,0, mapOf(Pair(camera0.num.toString(),camera0),Pair(camera1.num.toString(),camera1),Pair(camera2.num.toString(),camera2)))
        binding= ActivitySeparationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.sepCam.setOnClickListener {
            device.type=-1
            device.cameras=null
            try {
                device.token =generateToken()
            }catch (e :Exception){
                Toast.makeText(this@SeparationActivity,e.toString(),Toast.LENGTH_LONG).show()
            }
            MainActivity.TOKEN= device.token!!
            database.child("Users").child(userName).child("devices").child(deviceId).setValue(device).addOnSuccessListener {
                val intent = Intent(this, CameraActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this@SeparationActivity,"Failed!",Toast.LENGTH_SHORT).show()
            }
        }
        binding.sepMon.setOnClickListener {
            device.type=1
            database.child("Users").child(userName).child("devices").child(deviceId).setValue(device).addOnSuccessListener {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            }.addOnFailureListener{
                Toast.makeText(this@SeparationActivity,"Failed!",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun generateToken(): String {
        val expirationTimeInSeconds = 12 * 60 * 60//12 saat
        val token = RtcTokenBuilder()
        val timestamp = (System.currentTimeMillis() / 1000 + expirationTimeInSeconds).toInt()
        return token.buildTokenWithUserAccount(
            appID,
            appCer,
            deviceId,
            "0",
            RtcTokenBuilder.Role.Role_Publisher,
            timestamp
        )
    }

}