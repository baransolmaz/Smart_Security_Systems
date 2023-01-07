package com.baran.smartsecuritysystems

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.baran.smartsecuritysystems.MainActivity.Companion.sp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class NotificationService : Service() {

    companion object {
        private const val TAG = "NotificationService"
        private var NOTIFICATION_ID :Int=0
        private const val NOTIFICATION_CHANNEL_ID = "i.apps.notifications"
        private lateinit var handler: Handler
        private lateinit var deviceID:String
        private var size=0
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        handler = Handler(Looper.getMainLooper())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        NOTIFICATION_ID= intent?.getIntExtra("cameraNum",0)!!
        deviceID= intent.getStringExtra("devID").toString()
        size= intent.getIntExtra("size", 0)

        //startForeground(1, buildNotification())

        // Check for new files in Firebase Storage every minute
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference
        val periodicTask = object : Runnable {
            override fun run() {
                // Check for new files in Firebase Storage
                Log.d(TAG, "check")
                checkForNewFiles(storageRef)
                // Schedule this task to run again in 1 minute
                handler.postDelayed(this, 8000)
            }
        }
        handler.post(periodicTask)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder?=null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
    }

    private fun checkForNewFiles(storageRef: StorageReference) {
        storageRef.child(deviceID).listAll().addOnSuccessListener { listResult ->
            if(listResult.items.isNotEmpty()){
                if(listResult.items.size != size){
                    //giveNotification(num)
                    showNotification()
                    sp.edit().putInt("size$NOTIFICATION_ID",listResult.items.size).apply()
                }
                size=listResult.items.size
            }
        }
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun showNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Motion Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Alert!!")
            .setContentText("Motion Detected At Camera-$NOTIFICATION_ID !!")
            .setSmallIcon(R.drawable.home_icon)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun buildNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Notification Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.home_icon)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources,R.mipmap.ic_launcher))
            .setContentIntent(pendingIntent)
            .build()
    }


}