package com.baran.smartsecuritysystems

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.baran.smartsecuritysystems.databinding.ActivityArchiveBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage


class ArchiveActivity : AppCompatActivity() {
    private var camNum: Int =HomeActivity.PRESSED //Camera Number
    private var deviceID: String? = HomeActivity.CHANNELS[camNum]
    private lateinit var binding: ActivityArchiveBinding
    private var storage : StorageReference = Firebase.storage.reference.child(deviceID.toString())
    private lateinit var imageList:ArrayList<String>
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        binding= ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val rec=binding.rec

        val listRef = storage.child(deviceID.toString())
    }
}