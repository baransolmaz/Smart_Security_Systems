package com.baran.smartsecuritysystems
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.ActionBar
import com.baran.smartsecuritysystems.databinding.ActivityMainBinding
import com.baran.smartsecuritysystems.databinding.ActivitySignUpBinding
import com.google.firebase.database.DatabaseReference

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mainSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            //finish()
        }
        binding.mainLogIn.setOnClickListener {
            val intent = Intent(this, SeparationActivity::class.java)
            startActivity(intent)
            //finish()
        }
        binding.mainHowTo.setOnClickListener {
            /*val intent = Intent(this, SeparationActivity::class.java)
            startActivity(intent)
            //finish()*/
        }

    }
}