package com.baran.smartsecuritysystems
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.baran.smartsecuritysystems.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    //private lateinit var database: DatabaseReference

    @Suppress("DEPRECATION")
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