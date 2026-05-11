package com.example.a6times.menunav

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.R

class StoryDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val ivFullStoryImage = findViewById<ImageView>(R.id.ivFullStoryImage)
        val tvStoryTitle = findViewById<TextView>(R.id.tvStoryTitle)
        val tvFullStoryText = findViewById<TextView>(R.id.tvFullStoryText)

        btnBack.setOnClickListener {
            finish()
        }

        tvStoryTitle.text = "Haftalık Kelime Hikayen"
        tvFullStoryText.text = "Öğrendiğin kelimelerle oluşturulan eşsiz hikayen burada görüntülenecek. " +
                "Yapay zeka, hafızanı güçlendirmek için bu kelimeleri anlamlı bir kurgu içinde birleştiriyor."
    }
}