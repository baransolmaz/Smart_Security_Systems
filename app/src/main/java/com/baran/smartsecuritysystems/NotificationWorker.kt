package com.baran.smartsecuritysystems

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.baran.smartsecuritysystems.MainActivity.Companion.sp
import com.google.firebase.storage.FirebaseStorage
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class NotificationWorker(context: Context,params: WorkerParameters):Worker(context,params) {
    override fun doWork(): Result {
        try {
            val storageRef=FirebaseStorage.getInstance().reference
            val devID=inputData.getString("devID").toString()
            val num = inputData.getInt("cameraNum",0)
            var size=sp.getInt("size$num",0)
            storageRef.child(devID).listAll().addOnSuccessListener { listResult ->
                if(listResult.items.isNotEmpty()){
                    if(listResult.items.size != size){
                        giveNotification(num)
                        sp.edit().putInt("size$num",listResult.items.size).apply()
                        val formatter=
                            SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                        appendLog(num,formatter)
                    }
                    size=listResult.items.size
                }
            }

            return Result.success()
        }catch (e :Exception){
            Toast.makeText(applicationContext,"Failure",Toast.LENGTH_LONG).show()
            return  Result.failure()
        }

    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun giveNotification(cameraNum: Int) {
        val channelId = "i.apps.notifications"
        val description = "Motion Notification"
        val builder:Notification.Builder
        val notificationIntent = Intent(applicationContext, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationManager.createNotificationChannel(notificationChannel)

            builder = Notification.Builder(applicationContext, channelId)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera-$cameraNum !!")
        } else {
            builder = Notification.Builder(applicationContext)
                .setSmallIcon(R.drawable.home_icon)
                .setLargeIcon(BitmapFactory.decodeResource(applicationContext.resources,R.mipmap.ic_launcher))
                .setContentIntent(pendingIntent)
                .setContentTitle("Alert!!")
                .setContentText("Motion Detected At Camera-$cameraNum !!")
        }
        notificationManager.notify(cameraNum, builder.build())
    }
    @SuppressLint("SetTextI18n")
    private fun appendLog(num: Int, time: String) {
        val logFile = File(applicationContext.filesDir, "Log.txt")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append("$time -- Camera $num")
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
