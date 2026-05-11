package com.example.a6times.loginnav

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a6times.R
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.a6times.data.Users
import com.example.a6times.data.UsersRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.security.MessageDigest

class RegisterActivity : AppCompatActivity() {
    private val userRepo = UsersRepository()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val confirmRegisterButton = findViewById<Button>(R.id.ConfirmRegisterButton)
        val RegisterBackButton = findViewById<ImageButton>(R.id.RegisterBackButton)
        //region kayıt ol butonu
        confirmRegisterButton.setOnClickListener {
            registerUser()// Kayıt ol fonksiyonu çağrılır
        }
        //endregion
        
        //region geri butonu
        RegisterBackButton.setOnClickListener {
            finish()// Ana sayfaya dön
        }
        //endregion
    }


    //region kayıt ol fonksiyonu
    fun registerUser(){
        val userNameInput = findViewById<EditText>(R.id.editTextText8)
        val userEmail = findViewById<EditText>(R.id.email)
        val userPassword = findViewById<EditText>(R.id.editTextTextPassword2)
        val userPasswordAgain = findViewById<EditText>(R.id.editTextTextPassword3)

        //region boş alan kontrolü
        if (userPassword.text.toString() != userPasswordAgain.text.toString()){
            MaterialAlertDialogBuilder(this@RegisterActivity) // Hata mesajı
                .setTitle("Hata")
                .setMessage("Parola dogrulanmadi!")
                .setPositiveButton("Tamam") { dialog, _ ->
                    dialog.dismiss()
                    finish()
                }
                .setCancelable(false)
                .show()
            return
        }
        //endregion

        //region parola şifreleme
        val securePassword = userPassword.text.toString()
        //endregion
        //region kullanıcı nesnesi
        val newUser = Users(
            userName = userNameInput.text.toString(),
            userEmail = userEmail.text.toString(),
            userPassword = securePassword

        )
        //endregion
        //region kullanıcı kaydı
        lifecycleScope.launch {
            val result = userRepo.saveUser(newUser)
            if(result){
                finish()
            }else{
                Toast.makeText(this@RegisterActivity, "Hata Olustu!", Toast.LENGTH_SHORT).show()
            }
        }
        //endregion
    }
    //endregion
}