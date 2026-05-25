package com.example.a6times.menunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import coil.load
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.example.a6times.WordActivity
import com.example.a6times.WordleActivity
import com.example.a6times.data.WordsRepository
import com.google.android.material.button.MaterialButton

class HomeActivity : AppCompatActivity() {

    private val wordsRepository = WordsRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

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
        
        // Update learning progress
        val tvProgressPercent = findViewById<TextView>(R.id.tvProgressPercent)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        
        wordsRepository.getWordsOnce(
            onDataChange = { wordsList ->
                if (wordsList.isEmpty()) {
                    tvProgressPercent.text = "%0 (0/0)"
                    progressBar.progress = 0
                } else {
                    val totalWords = wordsList.size
                    val learnedWords = wordsList.count { it.isLearned || it.progress >= 6 }
                    val percent = if (totalWords > 0) ((learnedWords.toDouble() / totalWords) * 100).toInt() else 0
                    
                    tvProgressPercent.text = "%$percent ($learnedWords/$totalWords)"
                    progressBar.progress = percent
                }
            },
            onError = {
                tvProgressPercent.text = "%0"
                progressBar.progress = 0
            }
        )
        
        // Load the previous image state to show the previous visual on the Home screen
        val sharedPrefs = getSharedPreferences("WordChainPrefs", Context.MODE_PRIVATE)
        val lastImageUrl = sharedPrefs.getString("lastImageUrl", null)
        
        if (lastImageUrl != null) {
            val ivStoryPreview = findViewById<ImageView>(R.id.ivStoryPreview)
            
            // Load the actual saved image URL into the ImageView
            // The black overlay in the XML layout will act as the "dimming/blurring" effect
            ivStoryPreview.load(lastImageUrl) {
                crossfade(true)
            }
        }
    }
}