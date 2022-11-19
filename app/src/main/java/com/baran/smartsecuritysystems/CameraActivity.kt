package com.baran.smartsecuritysystems

import android.hardware.Camera
import android.hardware.camera2.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import com.baran.smartsecuritysystems.databinding.ActivityCameraBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException

@Suppress("DEPRECATION")
class CameraActivity : AppCompatActivity() , SurfaceHolder.Callback, Camera.PictureCallback {
    private lateinit var binding: ActivityCameraBinding
    private lateinit var userName: String
    private lateinit var deviceId: String

    private lateinit var cameraFrame:SurfaceView
    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null

    private var database: DatabaseReference = Firebase.database.reference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cameraFrame = binding.cameraFrame
        //mCamera = Camera.open()
        val extras = intent.extras
        if (extras != null) {
            userName = extras.getString("USERNAME").toString()
            deviceId = extras.getString("DEVICE_ID").toString()
        }
        setupSurfaceHolder()

    }
    private fun setupSurfaceHolder() {
        surfaceHolder = binding.cameraFrame.holder
        binding.cameraFrame.holder.addCallback(this)
    }

    private fun captureImage() {
        if (camera != null) {
            camera!!.takePicture(null, null, this)
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        startCamera()
    }

    private fun startCamera() {
        camera = Camera.open()
        camera!!.setDisplayOrientation(90)
        try {
            camera!!.setPreviewDisplay(surfaceHolder)
            camera!!.startPreview()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
        resetCamera()
    }

    private fun resetCamera() {
        if (surfaceHolder!!.surface == null) {
            // Return if preview surface does not exist
            return
        }

        // Stop if preview surface is already running.
        camera!!.stopPreview()
        try {
            // Set preview display
            camera!!.setPreviewDisplay(surfaceHolder)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Start the camera preview...
        camera!!.startPreview()
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        releaseCamera()
    }

    private fun releaseCamera() {
        camera!!.stopPreview()
        camera!!.release()
        camera = null
    }

    override fun onPictureTaken(bytes: ByteArray, camera: Camera) {
        resetCamera()
    }


    companion object {
        const val REQUEST_CODE = 100
    }




}