package com.baran.smartsecuritysystems

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.baran.smartsecuritysystems.databinding.ActivitySettingsBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.File

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding
    private var userName: String =MainActivity.USERNAME
    private var deviceId: String = MainActivity.DEVICE_ID
    private var database: DatabaseReference = Firebase.database.reference
    private var storage : StorageReference = Firebase.storage.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        overridePendingTransition(1,0)

        binding= ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.settingsDeleteAcc.setOnClickListener {
           askAgain()
        }
        binding.settingsChangePass.setOnClickListener {
            askNewTxt("Password")
        }
        binding.settingsChangeMail.setOnClickListener {
            askNewTxt("Mail")
        }
        binding.settingsLogout.setOnClickListener {
            MainActivity.sp.edit().putBoolean("logged",false).apply()
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
            finish()
        }

        val navBarHome=findViewById<BottomNavigationItemView>(R.id.home_nav)
        navBarHome.setOnClickListener{
            val intent = Intent(this, HomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        val navBarProfile=findViewById<BottomNavigationItemView>(R.id.profile_nav)
        navBarProfile.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        /*val navBarSettings=findViewById<BottomNavigationItemView>(R.id.settings_nav)
        navBarSettings.setOnClickListener{
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        }*/
    }
    override fun onResume() {
        super.onResume()
        overridePendingTransition(1,0)
    }
    private fun askAgain(){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Delete Account")
        alertDialog.setMessage("Do you really want to delete your account?")

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO") { dialog, _ -> dialog.dismiss() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes") { _, _ ->
            database.child("Users").child(userName).removeValue()
            storage.child(deviceId).delete()
            MainActivity.sp.edit().clear().apply()
            val logFile = File(this.filesDir,"${userName}_Log.txt")
            if (logFile.exists()) {
                logFile.delete()
            }
            finishAffinity()
        }

        alertDialog.show()
    }
    private fun askNewTxt(txt:String){
        val alertDialog = AlertDialog.Builder(this).create()
        alertDialog.setTitle("Change $txt")
        val input = EditText(this)
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.hint = "Enter New $txt"
        input.inputType = InputType.TYPE_CLASS_TEXT
        alertDialog.setView(input)

        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO") { dialog, _ -> dialog.dismiss() }
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes"){ _, _ ->
            // Here you get get input text from the Edittext
            val newPass = input.text.toString()
            if (txt=="Password" && newPass.length>5){
                database.child("Users").child(userName).child("passWord").setValue(newPass)
            }else if (txt=="Mail"){
                if (isValidEmail(newPass)){
                    database.child("Users").child(userName).child("mail").setValue(newPass)
                }else{
                    Toast.makeText(this,"Invalid Mail",Toast.LENGTH_LONG).show()
                }
            }else{
                Toast.makeText(this,"Password must be longer than 5 characters!",Toast.LENGTH_LONG).show()
            }
        }

        alertDialog.show()
    }
    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}