package com.example.a6times.loginnav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.a6times.R
import com.example.a6times.data.UsersRepository
import com.example.a6times.menunav.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Uygulamanın giriş ekranı.
 * Kullanıcıların e-posta ve şifre ile sisteme giriş yapmasını, kayıt ekranına veya şifre sıfırlama ekranına geçmesini sağlar.
 */
class LoginActivity: AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val userRepo = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //region Giriş Yap Butonu
        val loginBtn = findViewById<Button>(R.id.GirisYapButton)
        loginBtn.setOnClickListener {
            login() // Giriş yap fonksiyonunu çağır
        }
        //endregion

        //region Kayıt Ol Butonu
        findViewById<Button>(R.id.KayitOlButton)?.setOnClickListener {
            // Kayıt ol sayfasına geçiş yap
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        //endregion

        //region Şifremi Unuttum Butonu
        findViewById<Button>(R.id.SifremiUnuttumButton)?.setOnClickListener {
            // Şifremi unuttum sayfasına geçiş yap
            startActivity(Intent(this, ForgotActivity::class.java))
        }
        //endregion
    }

    /**
     * Kullanıcının girdiği bilgilerle Firebase üzerinden giriş işlemini gerçekleştirir.
     */
    fun login(){
        val email = findViewById<EditText>(R.id.emailLogin).text.toString().trim()
        val pass = findViewById<EditText>(R.id.passLogin).text.toString().trim()
        
        // Alanların boş olup olmadığını Firebase Auth kendisi de kontrol eder ancak burada başlatıyoruz
        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Lütfen tüm alanları doldurun!", Toast.LENGTH_SHORT).show()
            return
        }

        // Firebase ile kimlik doğrulama başlat
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    val uid = auth.currentUser?.uid
                    if (uid != null) {
                        // Kullanıcı bilgilerini çek ve SharedPreferences'a kaydet
                        lifecycleScope.launch {
                            val user = userRepo.getUser(uid)
                            if (user != null) {
                                val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
                                sharedPref.edit().putString("UserName", user.userName).apply()
                            }
                            // Giriş başarılıysa ana ekrana yönlendir
                            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                            finish() // Giriş ekranını kapat
                        }
                    } else {
                        // Giriş başarılıysa ana ekrana yönlendir
                        startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                        finish()
                    }
                } else {
                    // Giriş başarısızsa kullanıcıya hata mesajı göster
                    Toast.makeText(this@LoginActivity, "Hata Oluştu: ${task.exception?.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }
}
