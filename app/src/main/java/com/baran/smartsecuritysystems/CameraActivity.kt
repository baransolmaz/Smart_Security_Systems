package com.baran.smartsecuritysystems

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.hardware.camera2.*
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityCameraBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import io.agora.base.VideoFrame
import io.agora.rtc2.*
import io.agora.rtc2.video.IVideoFrameObserver
import io.agora.rtc2.video.VideoCanvas
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.concurrent.thread
import kotlin.math.abs


@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity(){
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        var timeDif:Long=0
        var pressed:Int =-1
    }
    private var database: DatabaseReference = Firebase.database.reference
    private var storage : StorageReference =Firebase.storage.reference
    private lateinit var binding: ActivityCameraBinding

    private var userName: String=MainActivity.USERNAME
    private var deviceId: String= MainActivity.DEVICE_ID
    private var appID : String= MainActivity.APP_ID // Fill the App ID of your project generated on Agora Console.
    private var token : String = MainActivity.TOKEN

    private val channelName = deviceId // Fill the channel name.
    private val uid = 0 // An integer that identifies the local user.

    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null    //SurfaceView to render local video in a Container.
    private var remoteSurfaceView: SurfaceView? = null //SurfaceView to render Remote video in a Container.

    private val requestedPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )
    private var referenceBitmap: Bitmap?=null
    private var frameCounter = 1
    //private val isRunning = false
    private val videoFrameObserver = object : IVideoFrameObserver {
        override fun onCaptureVideoFrame(videoFrame: VideoFrame): Boolean {
            // This method is called every time a new video frame is captured by the camera
            // You can access the frame data here and do something with it, such as process the frame or display it on the screen
            if (frameCounter%10==0){
                val buffer = videoFrame.buffer
                val i420Buffer = buffer.toI420()
                val width = i420Buffer.width
                val height = i420Buffer.height

                val bufferY = i420Buffer.dataY
                val bufferU = i420Buffer.dataU
                val bufferV = i420Buffer.dataV

                val i420 = YUVUtils.toWrappedI420(bufferY, bufferU, bufferV, width, height)
                val bitmap = YUVUtils.NV21ToBitmap(baseContext,
                    YUVUtils.I420ToNV21(i420, width, height),
                    width,height)

                val matrix = Matrix()
                matrix.setRotate(270F)
                // Rotate around a specific spot
                val newBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, false)
                if (referenceBitmap==null){
                    referenceBitmap=newBitmap
                }
                thread(start = true,isDaemon = true){
                    bitmapOperation(referenceBitmap,newBitmap)
                }
                // save to file
                bitmap.recycle()
                // Release
                i420Buffer.release()
                if(frameCounter%4==0)
                    referenceBitmap=newBitmap
            }
            if(frameCounter%20==0)
                frameCounter=0
            frameCounter++
            return true
        }
        private fun bitmapOperation(ref: Bitmap?, current: Bitmap?):Boolean{
            return if (!compareBitmap(ref,current)){
                //Motion Detected
                saveBitmap2Gallery(current)
                false
            }else
                true
        }
        private fun compareBitmap(ref: Bitmap?, current: Bitmap?):Boolean {
            if (ref == null || current == null) {
                return false
            }
            var total=0
            for (i in 0 until ref.width) {
                for (j in 0 until ref.height) {
                    val pixel: Int = ref.getPixel(i, j)
                    val pixel2: Int = current.getPixel(i, j)
                    val k=abs(sumRGB(pixel2) - sumRGB(pixel))
                    if(k>20)
                        total++
                    if(total>=23040)//640*360/10 -> %10
                        return false
                }
            }
            return true
        }
        private fun sumRGB(pixel :Int):Int{
            return Color.red(pixel)+ Color.blue(pixel)+ Color.green(pixel)
        }
        override fun onPreEncodeVideoFrame(videoFrame: VideoFrame?): Boolean {
            return false
        }
        override fun onScreenCaptureVideoFrame(videoFrame: VideoFrame?): Boolean {
            return false
        }
        override fun onPreEncodeScreenVideoFrame(videoFrame: VideoFrame?): Boolean {
            return false
        }
        override fun onMediaPlayerVideoFrame(videoFrame: VideoFrame?, mediaPlayerId: Int): Boolean {
            return false
        }
        override fun onRenderVideoFrame(channelId: String?,uid: Int,videoFrame: VideoFrame?): Boolean {
            return false
        }
        override fun getVideoFrameProcessMode(): Int {
            // The process mode of the video frame. 0 means read-only, and 1 means read-and-write.
            return 1
        }
        override fun getVideoFormatPreference(): Int {
            return 1
        }
        override fun getRotationApplied(): Boolean {
            return false
        }
        override fun getMirrorApplied(): Boolean {
            return false
        }
        override fun getObservedFramePosition(): Int {
            return 0
        }
    }
    ///

    private fun saveBitmap2Gallery(newBitmap: Bitmap?) {
        /*val en= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path+"/SSS"
        val f = File(en)
        if (!f.exists())
            f.mkdir()*/
        val name =System.currentTimeMillis().toString()
        /*val outputStream = FileOutputStream("$en/$name.jpg")
        newBitmap?.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
        outputStream.close()*/
        val byteOutputStream = ByteArrayOutputStream()
        newBitmap?.compress(Bitmap.CompressFormat.PNG, 90, byteOutputStream)
        storage.child(deviceId).child(name).putBytes(byteOutputStream.toByteArray())
    }
    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(this,requestedPermissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,requestedPermissions[1]) != PackageManager.PERMISSION_GRANTED)
    }
    private fun setupVideoSDKEngine() {
        checkNetworkState()
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = MainActivity.APP_ID
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
            //refresh()
        }
    }
    private fun checkNetworkState() {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)!!.state == NetworkInfo.State.CONNECTED||connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)!!
            .state == NetworkInfo.State.CONNECTED
        if (connected){
            return
        }else{
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("Connection")
            alertDialog.setMessage("You need internet connection!!")

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK") { dialog, _ ->
                dialog.dismiss()
                finishAffinity()
            }

            alertDialog.show()

            val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL)

            val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 10f
            btnPositive.layoutParams = layoutParams
        }
    }
    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
       // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            //runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
            setToken(SeparationActivity.generateToken(MainActivity.APP_ID,MainActivity.APP_CER,MainActivity.DEVICE_ID))
        }

    }
    fun setToken(newValue: String):String {
        token = newValue
        MainActivity.TOKEN=token
        database.child("Users").child(userName).child("devices").child(deviceId).child("token").setValue(token)
        agoraEngine!!.renewToken(token)
        showMessage("Token renewed")
        return newValue
    }
    private fun setupLocalVideo() {
        val container = binding.cameraFrame
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(baseContext)
        container.addView(localSurfaceView)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        agoraEngine!!.setupLocalVideo(VideoCanvas(localSurfaceView,VideoCanvas.RENDER_MODE_HIDDEN,1))
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
    }
    fun joinChannel(view: View?) {
        if (timeDif!=(0).toLong()){
            Thread.sleep(timeDif)
        }

        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
            setupLocalVideo()
            localSurfaceView!!.visibility = View.VISIBLE
            // Start local preview.
            agoraEngine!!.startPreview()
            agoraEngine!!.disableAudio()
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.

            agoraEngine!!.registerVideoFrameObserver(videoFrameObserver)

            agoraEngine!!.joinChannel(token, channelName, uid, options)

            binding.camStop.setBackgroundResource(R.drawable.rounded_button)
            binding.camStop.isClickable=true

            binding.camStart.setBackgroundResource(R.drawable.disabled_button)
            binding.camStart.isClickable=false

            binding.camTimer.setBackgroundResource(R.drawable.disabled_button)
            binding.camTimer.isClickable=false

        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT).show()
        }
    }
    @Suppress("DEPRECATION")
    fun leaveChannel(view: View) {
        agoraEngine!!.registerVideoFrameObserver(null)
        agoraEngine!!.leaveChannel()
        // Stop remote video rendering.
        if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
        // Stop local video rendering.
        if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE

        binding.camStart.setBackgroundResource(R.drawable.rounded_button)
        binding.camStart.isClickable=true

        binding.camTimer.setBackgroundResource(R.drawable.rounded_button)
        binding.camTimer.isClickable=true

        binding.camStop.setBackgroundResource(R.drawable.disabled_button)
        binding.camStop.isClickable=false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        binding = ActivityCameraBinding.inflate(layoutInflater)

        setContentView(binding.root)

         binding.camQr.setOnClickListener{
            val intent=Intent(this,QrActivity::class.java)
            startActivity(intent)
         }
        binding.camLogout.setOnClickListener {
            MainActivity.sp.edit().putBoolean("logged",false).apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }
        binding.camTimer.setOnClickListener{
            TimePickerFragment().show(supportFragmentManager, "timePicker")
        }
        checkPermission()

        setupVideoSDKEngine()
    }
    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()
        // Stop the frame loop and release resources
        referenceBitmap = null

        // Destroy the engine in a sub-thread to avoid congestion
        Thread {
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@CameraActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                refresh()
            } else {
                Toast.makeText(this@CameraActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun refresh(){
        finishAffinity()
    }
    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
        }
    }
}