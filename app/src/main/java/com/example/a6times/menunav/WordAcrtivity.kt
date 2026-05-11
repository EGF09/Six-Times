package com.example.a6times

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.data.WordsRepository

data class WordItem(
    val wordText: String,
    val progress: Int
)

class WordActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private val wordList = mutableListOf<WordItem>()
    private val wordsRepository = WordsRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener {
            finish()
        }

        recyclerView = findViewById(R.id.rvWords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        wordAdapter = WordAdapter(wordList)
        recyclerView.adapter = wordAdapter

        fetchWordsDynamically()
    }

    private fun fetchWordsDynamically() {
        wordsRepository.listenToWords(
            onDataChange = { words ->
                wordList.clear()
                for (word in words) {
                    val displayText = "${word.engWordName} - ${word.turWordName}"
                    wordList.add(WordItem(displayText, word.progress))
                }
                wordAdapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}