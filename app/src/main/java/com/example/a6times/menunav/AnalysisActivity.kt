package com.example.a6times.menunav

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.WordAdapter
import com.example.a6times.WordItem

class AnalysisActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analysis)

        val btnBack = findViewById<ImageButton>(R.id.btnAnalysisBack)
        btnBack.setOnClickListener { finish() }

        val btnPrint = findViewById<ImageButton>(R.id.btnPrintReport)
        btnPrint.setOnClickListener {

        }

        val tvTotalWords = findViewById<TextView>(R.id.tvTotalWordsCount)
        val tvAccuracy = findViewById<TextView>(R.id.tvAccuracyRate)
        val rvHardWords = findViewById<RecyclerView>(R.id.rvHardWords)

        updateStatistics(tvTotalWords, tvAccuracy)
        setupHardWordsList(rvHardWords)
    }

    private fun updateStatistics(tvTotal: TextView, tvRate: TextView) {
        val total = 45
        val rate = 72
        tvTotal.text = "Toplam Kelime\n$total"
        tvRate.text = "Başarı Oranı\n%$rate"
    }

    private fun setupHardWordsList(recyclerView: RecyclerView) {
        val hardWords = listOf(
            WordItem("Ambiguous", 15),
            WordItem("Persistence", 30),
            WordItem("Comprehensive", 25),
            WordItem("Volatility", 10)
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WordAdapter(hardWords)
    }
}