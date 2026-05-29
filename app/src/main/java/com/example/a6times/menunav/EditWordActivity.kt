package com.example.a6times.menunav

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.adapters.EditWordAdapter
import com.example.a6times.data.WordItem
import com.example.a6times.data.WordsRepository

/**
 * Kelimeleri listeleyen ve düzenleme moduna geçilmesini sağlayan ekran.
 * EditWordAdapter kullanarak kelimelerin hızlıca düzenlenmesine imkan tanır.
 */
class EditWordActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var editAdapter: EditWordAdapter
    private val wordList = mutableListOf<WordItem>()
    private val wordsRepository = WordsRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_word)

        val btnBack = findViewById<ImageButton>(R.id.btnEditWordsBack)
        btnBack.setOnClickListener { finish() }

        recyclerView = findViewById(R.id.rvEditWords)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Bu sayfa için özel tasarladığımız adaptörü bağlıyoruz
        editAdapter = EditWordAdapter(wordList)
        recyclerView.adapter = editAdapter

        // Kelimeleri Firebase'den çek
        fetchWordsForEditing()
    }

    /**
     * Veritabanındaki kelimeleri dinamik olarak dinler ve listeyi günceller.
     */
    private fun fetchWordsForEditing() {
        wordsRepository.listenToWords(
            onDataChange = { words ->
                wordList.clear()
                for (word in words) {
                    val displayText = "${word.engWordName} - ${word.turWordName}"
                    wordList.add(WordItem(word.wordID.toString(), displayText, word.progress))
                }
                editAdapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
