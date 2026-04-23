package com.example.a6times

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Button
import androidx.annotation.IdRes
import com.example.a6times.uinav.ForgotActivity
import com.example.a6times.uinav.RegisterActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main))
        { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val registerButton = findViewById<Button>(R.id.KayıtOlButton)
        registerButton.setOnClickListener {
            val regIntent = Intent(this, RegisterActivity::class.java)
            startActivity(regIntent)
        }
        val forgotButton = findViewById<Button>(R.id.SifremiUnuttumButton)
        forgotButton.setOnClickListener {
            val forgotIntent = Intent(this, ForgotActivity::class.java)
            startActivity(forgotIntent)
        }
    }
}