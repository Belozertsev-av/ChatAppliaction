package com.example.chatapplication.reglogin

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapplication.databinding.ActivityRegBinding
import com.example.chatapplication.messages.LatestMessagesActivity
import com.example.chatapplication.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID


class RegActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.regButton.setOnClickListener{
            registerUser()
        }

        binding.buttonImgSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }

        binding.textViewRegToLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private var selectedPhotoUri: Uri? = null

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            binding.circleImageView.setImageBitmap(bitmap)
            binding.buttonImgSelect.alpha = 0f
//            val bitmapDrawable = BitmapDrawable(bitmap)
//            binding.buttonImgSelect.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun registerUser(){
        val mail = binding.editTextMailReg.text.toString()
        val password = binding.editTextPasswordReg.text.toString()

        if (mail.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, заполните необходимые поля", Toast.LENGTH_SHORT).show()
            return
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail, password)
            .addOnCompleteListener{
                if (!it.isSuccessful) return@addOnCompleteListener

                Log.d("RegActivity", "Пользователь успешно создан с Id: ${it.result.user?.uid}")
                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener{
                Toast.makeText(this, "Не удалось создать пользователя. Причина: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegActivity", "Изображение успешно загружено: ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {
                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Log.d("RegActivity", "Изображение не было сохранено. Причина: ${it.message}")

            }
    }

    private fun saveUserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance("https://chatapplication-666eb-default-rtdb.europe-west1.firebasedatabase.app/").getReference("/users/$uid")
        val user = User(uid, binding.editTextLoginReg.text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("RegActivity", "Данные пользоваетля успешно сохранены в базе данных")
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Log.d("RegActivity", "Данные пользоваетля не были сохранены. Причина: ${it.message}")
            }
    }
}
