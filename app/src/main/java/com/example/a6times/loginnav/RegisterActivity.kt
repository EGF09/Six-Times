package com.example.a6times.loginnav

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a6times.R
import android.widget.Button
import android.widget.ImageButton

class RegisterActivity : AppCompatActivity() {
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
        confirmRegisterButton.setOnClickListener {
            finish()
        }
        
        val RegisterBackButton = findViewById<ImageButton>(R.id.RegisterBackButton)
        RegisterBackButton.setOnClickListener {
            finish()
        }
    }

}