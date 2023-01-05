package com.baran.smartsecuritysystems

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var database: DatabaseReference = Firebase.database.reference
    private var userName: String =MainActivity.USERNAME
    private var deviceId: String=MainActivity.DEVICE_ID

    private var resultQR:String?=null
    companion object{
        var USERNAMES= Array<String?>(3){null}
        var CHANNELS= Array<String?>(3){null}
        var TOKENS= Array<String?>(3){null}
        var PRESSED =-1
    }
    //Notification
    private lateinit var notificationManager: NotificationManager
    private lateinit var notificationChannel: NotificationChannel
    private lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Motion Notification"

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        overridePendingTransition(1,0)

        binding= ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        checkPermission()
        giveNotification(8)
        database.child("Users").child(userName).child("devices").child(deviceId).child("cameras").addListenerForSingleValueEvent( object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.hasChild("1")) {
                    USERNAMES[0]= dataSnapshot.child("1").child("user").value.toString()
                    CHANNELS[0]= dataSnapshot.child("1").child("devID").value.toString()
                    TOKENS[0]= dataSnapshot.child("1").child("token").value.toString()
                }
                if (dataSnapshot.hasChild("2")) {
                    USERNAMES[1]= dataSnapshot.child("2").child("user").value.toString()
                    CHANNELS[1]= dataSnapshot.child("2").child("devID").value.toString()
                    TOKENS[1]= dataSnapshot.child("2").child("token").value.toString()
                }
                if (dataSnapshot.hasChild("3")) {
                    USERNAMES[2]= dataSnapshot.child("3").child("user").value.toString()
                    CHANNELS[2]= dataSnapshot.child("3").child("devID").value.toString()
                    TOKENS[2]= dataSnapshot.child("3").child("token").value.toString()
                }
                for(i in CHANNELS.indices){
                    if (CHANNELS[i]=="e"){
                        when (i) {
                            0 -> setDisable(0,binding.textLive1,binding.textArc1,binding.textPair1)
                            1 -> setDisable(1,binding.textLive2,binding.textArc2,binding.textPair2)
                            2 -> setDisable(2,binding.textLive3,binding.textArc3,binding.textPair3)
                            else -> { Log.d("error", "invalid number") }
                        }
                    }else
                        when (i) {
                            0 -> setEnable(0,binding.textLive1,binding.textArc1,binding.textPair1)
                            1 -> setEnable(1,binding.textLive2,binding.textArc2,binding.textPair2)
                            2 -> setEnable(2,binding.textLive3,binding.textArc3,binding.textPair3)
                            else -> { Log.d("error", "invalid number") }
                        }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", databaseError.message)
            }
        })

        /*val navBarHome=findViewById<BottomNavigationItemView>(R.id.home_nav)
        navBarHome.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }*/
        val navBarProfile=findViewById<BottomNavigationItemView>(R.id.profile_nav)
        navBarProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        val navBarSettings=findViewById<BottomNavigationItemView>(R.id.settings_nav)
        navBarSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
    }
    override fun onResume() {
        super.onResume()
        overridePendingTransition(1,0)
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Scanned", Toast.LENGTH_LONG).show()
                resultQR=result.contents.toString()

                val arr= resultQR!!.split("::")
                val camera=Camera(PRESSED,arr[0],arr[1],arr[2])
                database.child("Users").child(userName).child("devices").child(deviceId).child("cameras").child((PRESSED+1).toString()).setValue(camera).addOnSuccessListener {
                    refresh()
                }.addOnFailureListener {
                    Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }
    @Suppress("DEPRECATION")
    fun readBarcode(){
        val intentIntegrator =IntentIntegrator(this)
        intentIntegrator.setBeepEnabled(true)
        intentIntegrator.setPrompt("")
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
        intentIntegrator.setCameraId(0)
        intentIntegrator.setOrientationLocked(false)
        intentIntegrator.initiateScan()
    }
    @SuppressLint("SetTextI18n")
    fun setEnable(cameraNum:Int, textLive: TextView, textArc: TextView, textPair: TextView) {
        textLive.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
        textArc.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
        textPair.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
        textLive.setOnClickListener {
            PRESSED=cameraNum
            checkTokenValidation(USERNAMES[cameraNum].toString(), CHANNELS[cameraNum].toString(),cameraNum)
            val intent = Intent(this, MonitorActivity::class.java)
            startActivity(intent)
        }
        textArc.setOnClickListener {
            PRESSED=cameraNum
            val intent = Intent(this, ArchiveActivity::class.java)
            startActivity(intent)
        }
        checkTokenValidation(USERNAMES[cameraNum].toString(), CHANNELS[cameraNum].toString(),cameraNum)
        textPair.text="Unpair Cam"
        textPair.setOnClickListener {
            setDisable(cameraNum,textLive,textArc,textPair)
        }
    }

    private fun checkTokenValidation(username: String, devID: String, cameraNum: Int) {
        database.child("Users").child(username).child("devices").child(devID).child("token").addListenerForSingleValueEvent( object :ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                TOKENS[cameraNum]=dataSnapshot.value.toString()
                database.child("Users").child(userName).child("devices").child(deviceId).child("cameras").child((cameraNum+1).toString()).child("token").setValue(
                    TOKENS[cameraNum])
            }
            override fun onCancelled(databaseError: DatabaseError) {
                Log.d("error", databaseError.message)
            }
        })

    }
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }
    @SuppressLint("SetTextI18n")
    fun setDisable(cameraNum: Int,textLive: TextView, textArc: TextView, textPair: TextView) {
        textLive.setTextColor(ContextCompat.getColor(this,R.color.dark_cyan))
        textArc.setTextColor(ContextCompat.getColor(this,R.color.dark_cyan))
        textPair.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
        textLive.setOnClickListener(null)
        textArc.setOnClickListener(null)
        textPair.text="Pair Cam"
        CHANNELS[cameraNum]="e"
        TOKENS[cameraNum]="e"
        val camera=Camera(cameraNum,"e", CHANNELS[cameraNum], TOKENS[cameraNum])
        database.child("Users").child(userName).child("devices").child(deviceId).child("cameras").child((cameraNum+1).toString()).setValue(camera).addOnSuccessListener {
            //refresh()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed!", Toast.LENGTH_SHORT).show()
        }
        textPair.setOnClickListener {
            PRESSED=cameraNum
            readBarcode()
        }
    }
    private fun refresh() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    private fun giveNotification(cameraNum: Int) {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera$cameraNum !!")
        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera$cameraNum !!")
        }
        notificationManager.notify(cameraNum, builder.build())
    }

}