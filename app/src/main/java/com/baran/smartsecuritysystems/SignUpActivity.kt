package com.baran.smartsecuritysystems

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.baran.smartsecuritysystems.databinding.ActivitySignUpBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    private lateinit var database: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createUserButton.setOnClickListener{
            database=FirebaseDatabase.getInstance("https://smart-security-systems-72f11-default-rtdb.europe-west1.firebasedatabase.app/").reference
            val user=getValues()

            database.child("Users").child(user.mail.toString()).setValue(user).addOnSuccessListener {
                binding.signName.text.clear()
                binding.signSurname.text.clear()
                binding.signUsername.text.clear()
                binding.signPassword.text.clear()
                binding.signMail.text.clear()
                Toast.makeText(this,"Successfully Saved!",Toast.LENGTH_LONG).show()
            }.addOnFailureListener{
                Toast.makeText(this,"Failed!",Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun getValues(): User {
        val name = binding.signName.text.toString()
        val surname = binding.signSurname.text.toString()
        val username = binding.signUsername.text.toString()
        val pass = binding.signPassword.text.toString()
        val mail = binding.signMail.text.toString()
        //TODO Mail database de var mı diye kontrol et yoksa ekle , varsa tekrar iste*/
        return User(name,surname,username,pass,mail)
    }
}