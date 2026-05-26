package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a6times.R
import com.example.a6times.WordAdapter
import com.example.a6times.data.WordItem
import com.example.a6times.data.WordsRepository
import com.google.android.material.floatingactionbutton.FloatingActionButton

/**
 * Kullanıcının eklediği kelimeleri listeleyen ve yöneten (ekleme, düzenleme, silme) ana kelime ekranı.
 */
class WordActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var wordAdapter: WordAdapter
    private val wordList = mutableListOf<WordItem>()
    private val wordsRepository = WordsRepository()
    private var isEditModeActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_word)

        val fabAddWord = findViewById<FloatingActionButton>(R.id.fabAddWord)
        val fabEditWord = findViewById<FloatingActionButton>(R.id.fabEditWord)

        // Yeni kelime ekleme ekranına git
        fabAddWord.setOnClickListener {
            val intent = Intent(this, AddWordActivity::class.java)
            startActivity(intent)
        }

        val btnBack = findViewById<ImageButton>(R.id.WordBackButton)
        btnBack.setOnClickListener {
            if (isEditModeActive) {
                // Eğer düzenleme modundaysak önce o modu kapat
                toggleEditMode(fabAddWord, fabEditWord)
            } else {
                finish()
            }
        }

        // Düzenleme modunu aç/kapat
        fabEditWord.setOnClickListener {
            toggleEditMode(fabAddWord, fabEditWord)
        }

        recyclerView = findViewById(R.id.rvWords)
        recyclerView.layoutManager = LinearLayoutManager(this)
        wordAdapter = WordAdapter(wordList)
        recyclerView.adapter = wordAdapter

        // Kelimeleri Firebase'den çek
        fetchWordsDynamically()
    }

    /**
     * Liste üzerindeki düzenleme (edit/delete) modları arasında geçiş yapar.
     */
    private fun toggleEditMode(fabAddWord: FloatingActionButton, fabEditWord: FloatingActionButton) {
        isEditModeActive = !isEditModeActive
        wordAdapter.isEditMode = isEditModeActive
        wordAdapter.notifyDataSetChanged()

        if (isEditModeActive) {
            fabAddWord.hide()
            fabEditWord.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
            Toast.makeText(this, "Düzenleme modu aktif. Kelimelerin yanındaki kaleme dokunun.", Toast.LENGTH_SHORT).show()
        } else {
            fabAddWord.show()
            fabEditWord.setImageResource(android.R.drawable.ic_menu_edit)
            Toast.makeText(this, "Normal moda dönüldü.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Firebase'deki kelimeleri gerçek zamanlı olarak dinler ve listeyi günceller.
     */
    private fun fetchWordsDynamically() {
        wordsRepository.listenToWords(
            onDataChange = { words ->
                wordList.clear()
                for (word in words) {
                    val displayText = "${word.engWordName} - ${word.turWordName}"
                    wordList.add(WordItem(word.wordID.toString(), displayText, word.progress))
                }
                wordAdapter.notifyDataSetChanged()
            },
            onError = { errorMessage ->
                Toast.makeText(this, "Hata: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Resim seçme sonucunu adaptöre ilet
        wordAdapter.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        if (isEditModeActive) {
            toggleEditMode(findViewById(R.id.fabAddWord), findViewById(R.id.fabEditWord))
        } else {
            super.onBackPressed()
        }
    }
}
