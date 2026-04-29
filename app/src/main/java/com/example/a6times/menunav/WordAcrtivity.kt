package com.example.a6times

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class WordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener {
            finish()
        }

    }
}