package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.example.a6times.WordActivity
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

        //region Settings Button Function
        ivSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        //endregion

        //region Quiz Button Function
        btnStartQuiz.setOnClickListener {
            startActivity(Intent(this, ExamActivity::class.java))
        }
        //endregion

        //region Add Word Button Function
        btnAddWord.setOnClickListener {
            startActivity(Intent(this, AddWordActivity::class.java))
        }
        //endregion

        //region Analysis Button Function
        btnAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }
        //endregion

        //region My Words Button Function
        btnMyWords.setOnClickListener {
            val intent = Intent(this, WordActivity::class.java)
            startActivity(intent)
        }
        //endregion
    }
}