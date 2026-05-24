package com.example.a6times.menunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.example.a6times.WordActivity
import com.example.a6times.WordleActivity
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private lateinit var tvStreakCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        tvStreakCount = findViewById(R.id.tvStreakCount)

        val ivSettings = findViewById<ImageView>(R.id.ivSettings)
        val btnStartQuiz = findViewById<MaterialButton>(R.id.btnStartQuiz)
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

    override fun onResume() {
        super.onResume()
        updateStreakSystem()
    }

    private fun updateStreakSystem() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)

        val lastLoginDay = prefs.getLong("lastLoginDay", 0L)
        var currentStreak = prefs.getInt("currentStreak", 0)

        if (lastLoginDay == 0L) {
            currentStreak = 1
        } else {
            val dayDifference = currentDay - lastLoginDay

            when (dayDifference) {
                1L -> {
                    currentStreak++
                }
                0L -> {
                    // Aynı gün, değişiklik yok
                }
                else -> {
                    currentStreak = 1
                }
            }
        }

        prefs.edit().apply {
            putLong("lastLoginDay", currentDay)
            putInt("currentStreak", currentStreak)
            apply()
        }

        tvStreakCount.text = currentStreak.toString()
    }
}