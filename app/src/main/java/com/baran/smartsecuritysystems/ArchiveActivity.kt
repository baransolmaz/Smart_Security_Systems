package com.baran.smartsecuritysystems

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.baran.smartsecuritysystems.databinding.ActivityArchiveBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class ArchiveActivity : AppCompatActivity() {
    private var camNum: Int =HomeActivity.PRESSED //Camera Number
    private var deviceID: String? = HomeActivity.CHANNELS[camNum]
    private lateinit var binding: ActivityArchiveBinding
    private var storage : StorageReference = Firebase.storage.reference
    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        binding= ActivityArchiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val recyclerView = binding.rec
        val layoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = layoutManager
        val images = ArrayList<String>()
        val adapter = ImageAdapter(images,this)
        //recyclerView.adapter = adapter
        val progressBar=binding.progress
        progressBar.visibility = View.VISIBLE
        // Retrieve a list of images from Firebase Storage and convert them to Bitmaps
        val listRef = storage.child(deviceID.toString())

        listRef.listAll().addOnSuccessListener { listResult ->
            if(listResult.items.isEmpty()){
                progressBar.visibility = View.GONE
                Toast.makeText(this,"No Image Found",Toast.LENGTH_LONG).show()
            }else{
                for ((k, file) in listResult.items.reversed().withIndex()) {
                    if(k<20){
                        file.downloadUrl.addOnSuccessListener { uri ->
                            images.add(uri.toString())
                            //Log.i("Item value", uri.toString())
                        }.addOnSuccessListener {
                            recyclerView.adapter = adapter
                            progressBar.visibility = View.GONE
                        }
                    }else {
                        file.downloadUrl.addOnSuccessListener {
                            storage.child(deviceID.toString()).child(file.name).delete()
                        }
                    }
                    if(listResult.items.size>=20) {
                        MainActivity.sp.edit().putInt("size$camNum", 20).apply()
                    }else
                        MainActivity.sp.edit().putInt("size$camNum",listResult.items.size).apply()
                }
            }
        }

    }
}