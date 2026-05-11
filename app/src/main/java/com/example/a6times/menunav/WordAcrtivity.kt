package com.example.a6times

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class WordItem(
    val wordText: String,
    val progress: Int
)

class WordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener {
            finish()
        }

        val wordList = listOf(
            WordItem("APPLE", 85),
            WordItem("TEACHER", 60),
            WordItem("PHONE", 100),
            WordItem("HUNGRY", 30),
            WordItem("DELİCİOUS", 50)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.rvWords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = WordAdapter(wordList)
    }
}