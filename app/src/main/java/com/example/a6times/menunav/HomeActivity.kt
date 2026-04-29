package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val ivSettings = findViewById<ImageView>(R.id.ivSettings)

        ivSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        val btnStartQuiz = findViewById<MaterialButton>(R.id.btnStartQuiz)
        btnStartQuiz.setOnClickListener {
            startActivity(Intent(this, ExamActivity::class.java))
        }

        val btnAddWord = findViewById<MaterialButton>(R.id.btnAddWord)
        btnAddWord.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }

        val btnAnalysis = findViewById<MaterialButton>(R.id.btnAnalysis)
        btnAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }
    }
}