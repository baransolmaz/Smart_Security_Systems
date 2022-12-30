package com.baran.smartsecuritysystems

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityMonitorBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas

class MonitorActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMonitorBinding
    private var database: DatabaseReference = Firebase.database.reference

    private var appID: String=MainActivity.APP_ID
    private var camNum: Int =HomeActivity.PRESSED //Camera Number
    private var token: String? = HomeActivity.TOKENS[camNum]
    private var channelName: String? = HomeActivity.CHANNELS[camNum]
    private var userName: String? = HomeActivity.USERNAMES[camNum]

    private lateinit var cameraFrame: FrameLayout
    private val uid = 0     // An integer that identifies the local user.
    private var agoraEngine: RtcEngine? = null
    private var localSurfaceView: SurfaceView? = null     //SurfaceView to render local video in a Container.
    private var remoteSurfaceView: SurfaceView? = null  //SurfaceView to render Remote video in a Container.

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
            runOnUiThread { setupRemoteVideo(uid) }
        }

        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
             showMessage("Joined Channel $channel")
        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread { remoteSurfaceView!!.visibility = View.GONE }
        }
        override fun onTokenPrivilegeWillExpire(token: String?) {
            showMessage("Token Will Expire")
            super.onTokenPrivilegeWillExpire(token)
        }
    }
    private fun setupRemoteVideo(uid: Int) {
        val container =binding.cameraFrame
        remoteSurfaceView = SurfaceView(baseContext)
        remoteSurfaceView!!.setZOrderMediaOverlay(true)
        container.addView(remoteSurfaceView)
        agoraEngine!!.setupRemoteVideo(
            VideoCanvas(remoteSurfaceView,VideoCanvas.RENDER_MODE_FIT,uid)
        )
        // Display RemoteSurfaceView.
        remoteSurfaceView!!.visibility = View.VISIBLE
    }
    private fun joinChannel() {
        if (checkSelfPermission()) {
            val options = ChannelMediaOptions()
            // For a Video call, set the channel profile as COMMUNICATION.
            options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            // Set the client role as BROADCASTER or AUDIENCE according to the scenario.
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            // Display LocalSurfaceView.
                //setupLocalVideo()
                //localSurfaceView!!.visibility = View.VISIBLE
            // Start local preview.
            agoraEngine!!.startPreview()
            agoraEngine!!.disableAudio()//?????
            // Join the channel with a temp token.
            // You need to specify the user ID yourself, and ensure that it is unique in the channel.
            agoraEngine!!.joinChannel(token, channelName, uid, options)
        } else {
            Toast.makeText(applicationContext, "Permissions was not granted", Toast.LENGTH_SHORT).show()
        }
    }
    @Suppress("DEPRECATION")
    fun leaveChannel() {
        agoraEngine!!.leaveChannel()
        // Stop remote video rendering.
        if (remoteSurfaceView != null) remoteSurfaceView!!.visibility = View.GONE
        // Stop local video rendering.
        //if (localSurfaceView != null) localSurfaceView!!.visibility = View.GONE
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        overridePendingTransition(1,0)

        binding= ActivityMonitorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraFrame = binding.cameraFrame
        binding.monWatch.setOnClickListener{
            joinChannel()
        }
        binding.monBack.setOnClickListener{
            leaveChannel()
            finish()
        }
        if (!checkSelfPermission()) {
            ActivityCompat.requestPermissions(this, requestedPermissions, permissionReqID);
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
    private val permissionReqID = 22
    private val requestedPermissions = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(): Boolean {
        return !(ContextCompat.checkSelfPermission(this,requestedPermissions[0]) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,requestedPermissions[1]) != PackageManager.PERMISSION_GRANTED)
    }
    fun showMessage(message: String?) {
        runOnUiThread {
            Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
        }
    }


}