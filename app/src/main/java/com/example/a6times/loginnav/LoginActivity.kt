package com.example.a6times.loginnav

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.R
import com.example.a6times.menunav.HomeActivity
import com.google.firebase.auth.FirebaseAuth
import java.security.MessageDigest


class LoginActivity: AppCompatActivity() {

    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //region Giriş Yap Butonu
        val loginBtn = findViewById<Button>(R.id.GirisYapButton)
        loginBtn.setOnClickListener {
            login()// Giriş yap fonksiyonu
        }
        //endregion

        //region Kayıt Ol Butonu
        findViewById<Button>(R.id.KayitOlButton)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        // Kayıt ol sayfasına geçiş
        }
        //endregion
        //region Şifremi Unuttum Butonu
        findViewById<Button>(R.id.SifremiUnuttumButton)?.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        // Şifremi unuttum sayfasına geçiş
        }
        //endregion
    }

    //region giriş yap fonksiyonu
    fun login(){
        val email = findViewById<EditText>(R.id.emailLogin).text.toString()
        val pass = findViewById<EditText>(R.id.passLogin).text.toString()
        //region boş alan kontrolü
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){// Giriş başarılıysa

                    startActivity(Intent(this@LoginActivity,
                        HomeActivity::class.java))

                }else{
                    Toast.makeText(this@LoginActivity, "Hata Olustu!",
                        Toast.LENGTH_SHORT).show()// Giriş başarısızsa hata mesajı
                }
            }
        //endregion
        }
    //endregion
    }