package com.example.a6times

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.uinav.ForgotActivity
import com.example.a6times.uinav.RegisterActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginButton = findViewById<Button>(R.id.GirisYapButton)
        loginButton.setOnClickListener {
            val intent = Intent(this, com.example.a6times.HomeActivity::class.java)
            startActivity(intent)
        }

        val registerButton = findViewById<Button>(R.id.KayitOlButton)
        registerButton.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        val forgotButton = findViewById<Button>(R.id.SifremiUnuttumButton)
        forgotButton.setOnClickListener {
            val intent = Intent(this, ForgotActivity::class.java)
            startActivity(intent)
        }
    }
}