package com.baran.smartsecuritysystems
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)

        val buttonMainSignUp = findViewById<Button>(R.id.main_sign_up)
        buttonMainSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            //finish()
        }
        val buttonMainLogIn = findViewById<Button>(R.id.main_log_in)
        buttonMainLogIn.setOnClickListener {
            val intent = Intent(this, SeparationActivity::class.java)
            startActivity(intent)
            //finish()
        }

    }
}