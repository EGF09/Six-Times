package com.example.a6times.loginnav

import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.a6times.R
import com.example.a6times.data.Users
import com.example.a6times.data.UsersRepository
import com.example.a6times.utils.Constants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Yeni kullanıcı kaydı oluşturma ekranı.
 * Kullanıcı adı, e-posta ve şifre bilgilerini alarak Firebase üzerine kayıt yapar.
 */
class RegisterActivity : AppCompatActivity() {
    private val userRepo = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Kenar paylarını ayarla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val confirmRegisterButton = findViewById<Button>(R.id.ConfirmRegisterButton)
        val registerBackButton = findViewById<ImageButton>(R.id.RegisterBackButton)

        // Kaydı onayla butonu
        confirmRegisterButton.setOnClickListener {
            registerUser()
        }

        // Geri dön butonu
        registerBackButton.setOnClickListener {
            finish()
        }
    }

    /**
     * Girdileri doğrular ve kullanıcıyı sisteme kaydeder.
     */
    fun registerUser() {
        val userNameInput = findViewById<EditText>(R.id.editTextText8)
        val userEmail = findViewById<EditText>(R.id.email)
        val userPassword = findViewById<EditText>(R.id.editTextTextPassword2)
        val userPasswordAgain = findViewById<EditText>(R.id.editTextTextPassword3)

        // Boş alan kontrolü
        if (userNameInput.text.isEmpty() || userEmail.text.isEmpty() || userPassword.text.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            return
        }

        // Parola eşleşme kontrolü
        if (userPassword.text.toString() != userPasswordAgain.text.toString()) {
            MaterialAlertDialogBuilder(this@RegisterActivity)
                .setTitle("Hata")
                .setMessage("Parolalar eşleşmiyor!")
                .setPositiveButton("Tamam") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
            return
        }

        val newUser = Users(
            userName = userNameInput.text.toString(),
            userEmail = userEmail.text.toString(),
            userPassword = userPassword.text.toString()
        )

        // Coroutine başlatarak kayıt işlemini gerçekleştir
        lifecycleScope.launch {
            val result = userRepo.saveUser(newUser)
            if (result) {
                // Kayıt başarılıysa kullanıcı adını yerel tercihlere kaydet
                val uid = FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val sharedPref = getSharedPreferences("${Constants.PREFS_USER}_$uid", Context.MODE_PRIVATE)
                    sharedPref.edit().putString(Constants.PREFS_KEY_USER_NAME, newUser.userName).apply()
                }
                
                // Kullanıcıya bilgi ver ve ekranı kapat
                Toast.makeText(this@RegisterActivity, "Kayıt Başarılı!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this@RegisterActivity, "Hata Oluştu, kayıt yapılamadı!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
