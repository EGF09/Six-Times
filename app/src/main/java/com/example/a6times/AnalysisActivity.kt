package com.example.a6times

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val btnBack = findViewById<ImageButton>(R.id.btnAnalysisBack)
        btnBack.setOnClickListener { finish() }

        val tvTotalWords = findViewById<TextView>(R.id.tvTotalWordsCount)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyRate)
        val rvHardWords = findViewById<RecyclerView>(R.id.rvHardWords)

        rvHardWords.layoutManager = LinearLayoutManager(this)

        updateStatistics(tvTotalWords, tvAccuracy)
    }

    private fun updateStatistics(tvTotal: TextView, tvRate: TextView) {
        val total = 45
        val rate = 72

        tvTotal.text = "Toplam Kelime\n$total"
        tvRate.text = "Başarı Oranı\n%$rate"
    }
}