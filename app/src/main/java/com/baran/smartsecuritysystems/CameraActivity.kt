package com.baran.smartsecuritysystems

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import android.hardware.camera2.*
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityCameraBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity(){
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_CODE = 100
    }
    private lateinit var binding: ActivityCameraBinding

    private var userName: String=MainActivity.USERNAME
    private var deviceId: String= MainActivity.DEVICE_ID
    private var appID : String=MainActivity.APP_ID // Fill the App ID of your project generated on Agora Console.
    private var appCer :String = MainActivity.APP_CER
    private val token : String = MainActivity.TOKEN

    private var camera: Camera? = null
    private var database: DatabaseReference = Firebase.database.reference


    private val channelName = appID // Fill the channel name.
    private val uid = 0 // An integer that identifies the local user.

    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null    //SurfaceView to render local video in a Container.
    private var remoteSurfaceView: SurfaceView? = null //SurfaceView to render Remote video in a Container.

    private val permissionReqID = 22
    private val requestedPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(this,requestedPermissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,requestedPermissions[1]) != PackageManager.PERMISSION_GRANTED)
    }
    private fun setupVideoSDKEngine() {
        try {
            val config = RtcEngineConfig()
            config.mContext = baseContext
            config.mAppId = appID
            config.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(config)
            // By default, the video module is disabled, call enableVideo to enable it.
            agoraEngine!!.enableVideo()
        } catch (e: Exception) {
            showMessage(e.toString())
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            //showMessage("Remote user joined $uid")
            // Set the remote video view
            //runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
            showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
    }
    private fun setupLocalVideo() {
        val container = binding.cameraFrame
        // Create a SurfaceView object and add it as a child to the FrameLayout.
        localSurfaceView = SurfaceView(baseContext)
        container.addView(localSurfaceView)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        agoraEngine!!.setupLocalVideo(VideoCanvas(localSurfaceView,VideoCanvas.RENDER_MODE_HIDDEN,1))
    }
    fun joinChannel(view: View?) {
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
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT).show()
        }
    }
    @Suppress("DEPRECATION")
    fun leaveChannel(view: View?) {
        agoraEngine!!.leaveChannel()
        // Stop remote video rendering.
        if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
        // Stop local video rendering.
        if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
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

        setupVideoSDKEngine()
    }
    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

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
        val i = Intent(intent)
        startActivity(i)
        finish()
    }
    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
        }
    }
}