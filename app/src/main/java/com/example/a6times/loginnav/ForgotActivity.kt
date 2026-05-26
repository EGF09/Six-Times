package com.example.a6times.loginnav

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a6times.R
import com.google.firebase.auth.FirebaseAuth

/**
 * Şifresini unutan kullanıcılar için şifre sıfırlama e-postası gönderme ekranı.
 */
class ForgotActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot)
        
        // Pencere kenar paylarını ayarla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.emailForgot)
        val btnChangePassword = findViewById<Button>(R.id.ChangePasswordButton)
        val btnBack = findViewById<ImageButton>(R.id.ForgotBackButton)

        // Şifre sıfırlama butonuna tıklama işlemi
        btnChangePassword.setOnClickListener {
            val email = etEmail.text.toString().trim()

            // E-posta alanı boş mu kontrol et
            if (email.isEmpty()) {
                Toast.makeText(this, "Lütfen kayıtlı e-posta adresinizi girin!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Firebase Auth üzerinden şifre sıfırlama e-postası gönder
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Şifre sıfırlama bağlantısı e-postanıza gönderildi!", Toast.LENGTH_LONG).show()
                        finish() // Giriş ekranına geri dön
                    } else {
                        // Hata durumunda kullanıcıyı bilgilendir
                        Toast.makeText(this, "Hata: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // Geri butonu
        btnBack.setOnClickListener {
            finish()
        }
    }
}
