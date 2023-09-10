package com.example.chatapplication.reglogin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.databinding.ActivityLoginBinding
import com.example.chatapplication.messages.LatestMessagesActivity
import com.google.firebase.auth.FirebaseAuth

class LoginActivity: AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.textViewloginToReg.setOnClickListener {
            finish()
        }

        binding.buttonLogin.setOnClickListener{
            loginUser()
        }
    }

    private fun loginUser() {
        val mail = binding.editTextMailLogin.text.toString()
        val password = binding.editTextPasswordLogin.text.toString()

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните необходимые поля", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().signInWithEmailAndPassword(mail, password)
                .addOnCompleteListener{
                    val intent = Intent(this, LatestMessagesActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                .addOnFailureListener{
                    Log.d("loginActivity", "Не Удалось авторизироваться")
                }

    }
}