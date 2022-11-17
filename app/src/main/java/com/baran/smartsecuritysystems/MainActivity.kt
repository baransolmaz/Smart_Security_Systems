package com.baran.smartsecuritysystems

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.baran.smartsecuritysystems.databinding.ActivityMainBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    companion object {
        private const val CAMERA_PERMISSION_CODE = 100
        private const val STORAGE_PERMISSION_CODE = 101
    }
    private lateinit var binding:ActivityMainBinding
    private var database: DatabaseReference =Firebase.database.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,STORAGE_PERMISSION_CODE)
        checkPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION_CODE)

        binding.mainSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            //finish()
        }
        binding.mainLogIn.setOnClickListener {

            var inputUsername=binding.mainUsername.text.toString()
            var inputPass=binding.mainPassword.text.toString()

            if (inputUsername.isEmpty()||inputPass.isEmpty()){
                Toast.makeText(this,"Please Enter Username/Password",Toast.LENGTH_SHORT).show()
            }else{
                database.child("Users").addListenerForSingleValueEvent( object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(inputUsername)){
                            val pass=dataSnapshot.child(inputUsername).child("passWord").value.toString()
                            if (pass == inputPass) {
                                Toast.makeText(this@MainActivity,"Log in successful!",Toast.LENGTH_SHORT).show()
                                val intent =Intent(this@MainActivity, SeparationActivity::class.java)
                                startActivity(intent)
                            }
                        }else{
                            binding.mainUsername.text.clear()
                            binding.mainPassword.text.clear()
                            Toast.makeText(this@MainActivity,"Wrong username or password",Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        Log.d("error", databaseError.message)

                    }

                })
            }
        }

    }
    // Function to check and request permission.
    private fun checkPermission(permission: String, requestCode: Int) {
        if (ContextCompat.checkSelfPermission(this@MainActivity, permission) == PackageManager.PERMISSION_DENIED) {
            // Requesting the permission
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(permission), requestCode)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "Storage Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this@MainActivity, "Storage Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun dialogHowTo(view: View) {

        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("How To Use")
        alertDialog.setMessage("1. Log in or Sign up\n" +
                "2. Choose device type\n" +
                "\t\t3.1 As Camera\n" +
                "-To start streaming,press start button\n" +
                "-To set timer,press timer button\n" +
                "-To stop streaming,press stop button\n" +
                "-To generate Qr code,press Qr button\n" +
                "\t\t3.2 As Monitoring\n" +
                "-You have to pair at least 1 camera\n" +
                "-To monitor,press watch live\n" +
                "-To see archive,press archive\n" +
                "To unpair camera, long press pair button\n" +
                "-You can see your account in profile menu\n" +
                "-You can edit settings")

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK") { dialog, _ -> dialog.dismiss() }

        alertDialog.show()

        val btnPositive = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE)

        val layoutParams = btnPositive.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 10f
        btnPositive.layoutParams = layoutParams
    }
}
