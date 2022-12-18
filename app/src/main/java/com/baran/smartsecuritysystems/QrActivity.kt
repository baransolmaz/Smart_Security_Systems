package com.baran.smartsecuritysystems

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.baran.smartsecuritysystems.databinding.ActivityQrBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
 
class QrActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrBinding
    private var deviceId: String = MainActivity.DEVICE_ID
    private var token: String = MainActivity.TOKEN
    private var username: String = MainActivity.USERNAME
    lateinit var bitmap: Bitmap

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN

        binding=ActivityQrBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var qrImage=binding.qrImage
        var closeBut=binding.closeBut

        try {
            val barcodeEncoder = BarcodeEncoder()
            val screenWidth = resources.displayMetrics.widthPixels
            bitmap = barcodeEncoder.encodeBitmap("$username::$deviceId::$token",BarcodeFormat.QR_CODE, screenWidth,screenWidth)
            qrImage.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            Log.e("generateQR()", e.message.toString())
        }

        closeBut.setOnClickListener{
            finish()
        }
    }
}