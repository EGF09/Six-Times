package com.example.a6times

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a6times.databinding.ActivityWordleBinding

class WordleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordleBinding
    private val targetWord = "YAZILIM"
    private val maxTries = 6
    private var currentTry = 0
    private lateinit var cells: Array<Array<TextView?>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cells = Array(maxTries) { arrayOfNulls<TextView>(targetWord.length) }
        setupGrid()

        binding.btnSubmitGuess.setOnClickListener {
            val guess = binding.etGuess.text.toString().uppercase()

            if (guess.isNotEmpty()) {
                checkGuess(guess)
                binding.etGuess.text.clear()
            } else {
                Toast.makeText(this, "Lütfen bir kelime girin", Toast.LENGTH_SHORT).show()
            }
        }

        binding.WordBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setupGrid() {
        binding.glWordleGrid.removeAllViews()
        binding.glWordleGrid.columnCount = targetWord.length

        binding.glWordleGrid.post {
            val marginSize = 8
            val totalWidth = binding.glWordleGrid.width

            // Genişlik 0 gelirse ekran genişliğini baz al
            val widthToUse = if (totalWidth > 0) totalWidth else resources.displayMetrics.widthPixels - 100

            // Sütun başına düşen boşlukları hesaplayıp hücre boyutunu buluyoruz
            val cellSize = (widthToUse / targetWord.length) - (marginSize * 2)

            for (row in 0 until maxTries) {
                for (col in 0 until targetWord.length) {
                    val textView = TextView(this)
                    val params = GridLayout.LayoutParams()

                    params.width = cellSize
                    params.height = cellSize
                    params.setMargins(marginSize, marginSize, marginSize, marginSize)
                    params.setGravity(Gravity.CENTER)

                    params.columnSpec = GridLayout.spec(col)
                    params.rowSpec = GridLayout.spec(row)

                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    textView.textSize = if (targetWord.length > 6) 16f else 20f
                    textView.setTypeface(null, android.graphics.Typeface.BOLD)
                    textView.setTextColor(Color.WHITE)
                    textView.background = ContextCompat.getDrawable(this, R.drawable.bg_word_cell)

                    binding.glWordleGrid.addView(textView)
                    cells[row][col] = textView
                }
            }
        }
    }
    private fun checkGuess(guess: String) {
        if (currentTry >= maxTries) return

        for (i in 0 until targetWord.length) {
            val textView = cells[currentTry][i]

            if (i < guess.length) {
                val char = guess[i]
                textView?.text = char.toString()

                val colorHex = when {
                    char == targetWord[i] -> "#6AAA64"
                    targetWord.contains(char) -> "#C9B458"
                    else -> "#3A3A3C"
                }
                textView?.background?.setTint(Color.parseColor(colorHex))
            }
        }

        if (guess == targetWord) {
            Toast.makeText(this, "Tebrikler!", Toast.LENGTH_LONG).show()
            binding.btnSubmitGuess.isEnabled = false
        } else if (currentTry == maxTries - 1) {
            Toast.makeText(this, "Kelime: $targetWord", Toast.LENGTH_LONG).show()
            binding.btnSubmitGuess.isEnabled = false
        }

        currentTry++
    }
}