package com.baran.smartsecuritysystems

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.baran.smartsecuritysystems.databinding.ActivitySignUpBinding
import com.google.firebase.FirebaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignUpBinding
    private var database: DatabaseReference=Firebase.database.reference

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility= View.SYSTEM_UI_FLAG_FULLSCREEN
        binding=ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.createUserButton.setOnClickListener{
            val name = binding.signName.text.toString()
            val surname = binding.signSurname.text.toString()
            val username = binding.signUsername.text.toString()
            val pass = binding.signPassword.text.toString()
            val mail = binding.signMail.text.toString()
            val user=User(name, surname,username,pass,mail)
            if(name.isEmpty()||surname.isEmpty()||username.isEmpty()||pass.isEmpty()||mail.isEmpty()){
                Toast.makeText(this,"Please fill all fields!",Toast.LENGTH_SHORT).show()
            }else{
                database.child("Users").addListenerForSingleValueEvent( object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.hasChild(user.userName.toString())){
                            Toast.makeText(this@SignUpActivity,"Username is already registered!",Toast.LENGTH_SHORT).show()
                        }else{
                            database.child("Users").child(user.userName.toString()).setValue(user).addOnSuccessListener {
                                binding.signName.text.clear()
                                binding.signSurname.text.clear()
                                binding.signUsername.text.clear()
                                binding.signPassword.text.clear()
                                binding.signMail.text.clear()
                                Toast.makeText(this@SignUpActivity,"Successfully Saved!",Toast.LENGTH_SHORT).show()
                                finish()
                            }.addOnFailureListener{
                                Toast.makeText(this@SignUpActivity,"Failed!",Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        //TODO
                        //Log.d("error", databaseError.message)

                    }

                })

            }

        }
    }

}