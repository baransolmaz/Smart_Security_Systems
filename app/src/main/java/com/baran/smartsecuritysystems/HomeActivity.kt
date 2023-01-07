package com.baran.smartsecuritysystems

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.integration.android.IntentIntegrator
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private var database: DatabaseReference = Firebase.database.reference
    private var userName: String =MainActivity.USERNAME
    private var deviceId: String=MainActivity.DEVICE_ID
    private var storage=FirebaseStorage.getInstance().reference
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
        alertsInit()
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

    private fun alertsInit() {
        val logFile = File(this.filesDir,"Log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        try{
            val buf = BufferedReader(FileReader(logFile))
            val alertTexts=binding.alerts
            do {
                val text=buf.readLine()
                if (text != null){
                    val textView=TextView(this)
                    textView.text=text.toString()
                    textView.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
                    alertTexts.addView(textView,0)
                }
              }while (text!=null)

            buf.close()
        }catch (e:IOException){
            e.printStackTrace()
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
        if (MainActivity.sp.getBoolean("logged",false)){
            MainActivity.sp.edit().putBoolean("thread$cameraNum",true).apply()
            startBackgroundTask(CHANNELS[cameraNum].toString(),cameraNum)
        }


        /*val i=Intent(this,NotificationService::class.java)
        i.putExtra("cameraNum",cameraNum)
        i.putExtra("devID",CHANNELS[cameraNum].toString())
        i.putExtra("size", 0)

        startService(i)*/
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
        MainActivity.sp.edit().putBoolean("thread$cameraNum",false).apply()

        //stopService(Intent(this,NotificationService::class.java))
        /*val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        jobScheduler.cancel(cameraNum)*/
    }
    private fun refresh() {
        val intent = Intent(applicationContext, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun giveNotification(cameraNum: Int) {
        val notificationIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(this, channelId)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera-$cameraNum !!")
        } else {
            builder = Notification.Builder(this)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera-$cameraNum !!")
        }
        notificationManager.notify(cameraNum, builder.build())
    }
    @SuppressLint("SimpleDateFormat")
    private fun checkFileSize(sp: SharedPreferences, storageRef: StorageReference, devID: String, num: Int) {
        var size=sp.getInt("size$num",0)
        storageRef.child(devID).listAll().addOnSuccessListener { listResult ->
            if(listResult.items.isNotEmpty()){
                if(listResult.items.size != size){
                    giveNotification(num)
                    sp.edit().putInt("size$num",listResult.items.size).apply()
                    val formatter=SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                    appendLog(num,formatter)
                }
                size=listResult.items.size
            }
        }
    }
    private fun startBackgroundTask(devID:String,cameraNum: Int) {
        if (MainActivity.sp.getBoolean("logged",false)) {
            thread(start = true,isDaemon = true){
                val storageRef=storage
                val sp=MainActivity.sp
                while (sp.getBoolean("thread$cameraNum", false)) {
                    checkFileSize(sp,storageRef,devID, cameraNum)
                    Thread.sleep(10000) // check every 10 seconds
                }
            }
        }
    }
    private fun alertThread(){
        val logFile = File(this.filesDir,"Log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        try{
            val buf = BufferedReader(FileReader(logFile))
            val text=buf.readLine()
            val alertTexts=binding.alerts
            val textView=TextView(this)
            textView.text=text.toString()
            textView.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
            alertTexts.addView(textView)
            buf.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun appendLog(num: Int, time: String){
        val logFile = File(this.filesDir,"Log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        try{
            val textV=binding.alerts
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append("$time -- Camera $num")
            val textView=TextView(this)
            textView.text= "$time -- Camera $num"
            textView.setTextColor(ContextCompat.getColor(this,R.color.light_cyan))
            textV.addView(textView,0)

            buf.newLine()
            buf.close()
        }catch (e:IOException){
            e.printStackTrace()
        }

    }

}