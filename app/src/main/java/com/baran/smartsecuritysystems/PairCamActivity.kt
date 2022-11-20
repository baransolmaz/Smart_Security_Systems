package com.baran.smartsecuritysystems

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Camera
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityCameraBinding
import com.baran.smartsecuritysystems.databinding.ActivityMainBinding
import com.baran.smartsecuritysystems.databinding.ActivityPairCamBinding
import com.baran.smartsecuritysystems.databinding.ActivityPlanBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.io.IOException

class PairCamActivity : AppCompatActivity(), SurfaceHolder.Callback, Camera.PictureCallback  {
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val REQUEST_CODE = 100
    }

    private lateinit var binding: ActivityPairCamBinding

    private lateinit var userName: String
    private lateinit var deviceId: String

    private lateinit var cameraFrame: SurfaceView
    private var camera: Camera? = null
    private var surfaceHolder: SurfaceHolder? = null

    private var database: DatabaseReference = Firebase.database.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN

        binding=ActivityPairCamBinding.inflate(layoutInflater)
        checkPermission(Manifest.permission.CAMERA, PairCamActivity.CAMERA_PERMISSION_CODE)
        setContentView(binding.root)
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

    @Deprecated("Deprecated in Java")
    override fun onPictureTaken(bytes: ByteArray, camera: Camera) {
        resetCamera()
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@PairCamActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@PairCamActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PairCamActivity.CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@PairCamActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
                refresh()
            } else {
                Toast.makeText(this@PairCamActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun refresh(){
        val i = Intent(intent)
        i.putExtra("USERNAME",userName)
        i.putExtra("DEVICE_ID",deviceId)
        startActivity(i)
        finish()
    }

}