package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.example.a6times.WordActivity
import com.example.a6times.WordleActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val ivSettings = findViewById<ImageView>(R.id.ivSettings)
        val btnStartQuiz = findViewById<MaterialButton>(R.id.btnStartQuiz)
        val btnAddWord = findViewById<MaterialButton>(R.id.btnAddWord)
        val btnAnalysis = findViewById<MaterialButton>(R.id.btnAnalysis)
        val btnMyWords = findViewById<MaterialButton>(R.id.btnMyWords)
        val btnWordle = findViewById<MaterialButton>(R.id.btnWordle)
        val cardAiStory = findViewById<CardView>(R.id.cardAiStory)

        ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnStartQuiz.setOnClickListener {
            startActivity(Intent(this, ExamActivity::class.java))
        }

        btnAddWord.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }

        btnAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }

        btnMyWords.setOnClickListener {
            startActivity(Intent(this, WordActivity::class.java))
        }

        btnWordle.setOnClickListener {
            startActivity(Intent(this, WordleActivity::class.java))
        }

        cardAiStory.setOnClickListener {
            val intent = Intent(this, StoryDetailActivity::class.java)
            startActivity(intent)
        }
    }
}