package com.example.a6times

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Gravity
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.a6times.data.WordsRepository
import com.example.a6times.databinding.ActivityWordleBinding
import java.util.Locale

class WordleActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWordleBinding
    private var targetWord = ""
    private val maxTries = 6
    private var currentTry = 0
    private lateinit var cells: Array<Array<TextView?>>
    private val wordsRepository = WordsRepository()
    private var isGameInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etGuess.isEnabled = false // Başlangıçta girişleri devre dışı bırak
        binding.btnSubmitGuess.isEnabled = false

        binding.WordBackButton.setOnClickListener {
            finish()
        }
        //region Oyunun Başlatılması
        binding.btnSubmitGuess.setOnClickListener {
            val guess = binding.etGuess.text.toString().uppercase(Locale.ENGLISH)

            if (guess.length == targetWord.length) {
                // Sadece aynı harften oluşan (örn: AAA) kelimelerin girişini engelleme
                if (guess.toSet().size == 1 && guess.length > 1) {
                    Toast.makeText(this, "Lütfen geçerli bir kelime giriniz!", Toast.LENGTH_SHORT).show()
                } else {
                    checkGuess(guess)
                    binding.etGuess.text.clear()
                }
            } else {
                Toast.makeText(this, "Kelime ${targetWord.length} harfli olmalı!", Toast.LENGTH_SHORT).show()
            }
        }
        //endregion
        //region Giriş Kontrolü
        binding.etGuess.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.isNotEmpty() && input.any { !it.isLetter() }) {
                    Toast.makeText(this@WordleActivity, "Lütfen sadece harf giriniz!", Toast.LENGTH_SHORT).show()
                    val filtered = input.filter { it.isLetter() }
                    binding.etGuess.setText(filtered)
                    binding.etGuess.setSelection(filtered.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        //endregion
        fetchDailyWord()
    }

    //region Kelime Kontrolü
    private fun fetchDailyWord() {
        wordsRepository.listenToWords(
            onDataChange = { wordsList ->
                if (isGameInitialized) return@listenToWords
                
                val learnedWords = wordsList.filter { it.isLearned || it.progress >= 6 }
                    .sortedBy { it.wordID }

                if (learnedWords.isEmpty()) {
                    Toast.makeText(this, "Henüz 6 aşamayı tamamlamış kelimeniz yok!", Toast.LENGTH_LONG).show()
                    return@listenToWords
                }

                val epochDays = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                val todayWordIndex = (epochDays % learnedWords.size).toInt()
                val selectedWord = learnedWords[todayWordIndex].engWordName

                targetWord = selectedWord.uppercase(Locale.ENGLISH).filter { it.isLetter() }

                if (targetWord.isEmpty()) {
                    Toast.makeText(this, "Kelime formatı uygun değil.", Toast.LENGTH_SHORT).show()
                    return@listenToWords
                }
                
                isGameInitialized = true

                val prefs = getSharedPreferences("WordlePrefs", Context.MODE_PRIVATE)
                val lastPlayedDay = prefs.getLong("lastPlayedDay", 0L)

                cells = Array(maxTries) { arrayOfNulls<TextView>(targetWord.length) }
                setupGrid()

                if (lastPlayedDay == epochDays) {
                    Toast.makeText(this, "Bugünün kelimesini zaten oynadınız!", Toast.LENGTH_LONG).show()
                } else {
                    binding.etGuess.isEnabled = true
                    binding.btnSubmitGuess.isEnabled = true
                    binding.etGuess.filters = arrayOf(InputFilter.LengthFilter(targetWord.length))
                }
            },
            onError = { errorMsg ->
                Toast.makeText(this, "Hata: $errorMsg", Toast.LENGTH_SHORT).show()
            }
        )
    }
    //endregion

    //region Oyunun Görünümü
    private fun setupGrid() {
        binding.glWordleGrid.removeAllViews()
        binding.glWordleGrid.columnCount = targetWord.length
        binding.glWordleGrid.rowCount = maxTries

        binding.glWordleGrid.post {
            val marginSize = 8
            
            val displayMetrics = resources.displayMetrics
            
            val containerWidth = binding.hsvWordle.width.takeIf { it > 0 } ?: displayMetrics.widthPixels
            val containerHeight = binding.hsvWordle.height.takeIf { it > 0 } ?: (displayMetrics.heightPixels * 0.5).toInt()
            
            val widthToUse = containerWidth - (32 * displayMetrics.density).toInt()
            val heightToUse = containerHeight

            var cellWidth = (widthToUse / targetWord.length) - (marginSize * 2)
            val cellHeight = (heightToUse / maxTries) - (marginSize * 2)
            
            val minCellSize = (45 * displayMetrics.density).toInt()
            val maxCellSize = (70 * displayMetrics.density).toInt()

            // Hücrelerin hem ekrana sığması hem de her zaman kare formunda kalması için min değeri alıyoruz
            var calculatedCellSize = minOf(cellWidth, cellHeight)
            
            // 7 harften veya uzun kelimelerde ekranın dışına çıkabilmesi (kaydırma için) min boyut zorlaması
            if (calculatedCellSize < minCellSize) {
                calculatedCellSize = minCellSize
            }
            // Çok kısa kelimelerde hücrelerin devasa boyutlara ulaşmasını engelliyoruz
            if (calculatedCellSize > maxCellSize) {
                calculatedCellSize = maxCellSize
            }

            binding.glWordleGrid.alignmentMode = GridLayout.ALIGN_BOUNDS

            // Yazı boyutunu da kutu boyutuna göre dinamik hesapla
            // Kutu boyutunun %40'ı civarı iyi bir yazı boyutu verir (sp cinsinden)
            val dynamicTextSize = (calculatedCellSize / displayMetrics.density) * 0.4f
            for (row in 0 until maxTries) {
                for (col in 0 until targetWord.length) {
                    val textView = TextView(this)
                    val params = GridLayout.LayoutParams()

                    params.width = calculatedCellSize
                    params.height = calculatedCellSize
                    params.setMargins(marginSize, marginSize, marginSize, marginSize)

                    params.columnSpec = GridLayout.spec(col, GridLayout.CENTER)
                    params.rowSpec = GridLayout.spec(row, GridLayout.CENTER)

                    textView.layoutParams = params
                    textView.gravity = Gravity.CENTER
                    textView.textSize = dynamicTextSize
                    textView.setTypeface(null, android.graphics.Typeface.BOLD)
                    textView.setTextColor(Color.WHITE)
                    
                    // Yazıların kenarlara fazla yapışıp taşmasını engellemek için padding
                    textView.setPadding(0, 0, 0, 0)
                    textView.includeFontPadding = false

                    val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.bg_word_cell)?.mutate()
                    backgroundDrawable?.setTintList(null)
                    textView.background = backgroundDrawable

                    binding.glWordleGrid.addView(textView)
                    cells[row][col] = textView
                }
            }
        }
    }
    //endregion

    //region Tahmin Kontrolü
    private fun checkGuess(guess: String) {
        if (currentTry >= maxTries) return

        // Animasyon oynarken girişleri devre dışı bırak
        binding.btnSubmitGuess.isEnabled = false
        binding.etGuess.isEnabled = false

        val currentRow = currentTry
        currentTry++
        
        var completedAnimations = 0
        for (i in 0 until targetWord.length) {
            val textView = cells[currentRow][i]
            val char = guess[i]
            textView?.text = char.toString()

            val colorHex = when {
                char == targetWord[i] -> "#6AAA64"
                targetWord.contains(char) -> "#C9B458"
                else -> "#3A3A3C"
            }
            
            val delay = i * 250L // Her harf için gecikme

            // Dönme (Flip) Efekti
            textView?.animate()
                ?.rotationX(90f)
                ?.setDuration(200)
                ?.setStartDelay(delay)
                ?.withEndAction {
                    // Yarı yolda arkaplan rengini değiştir
                    textView.background?.setTint(Color.parseColor(colorHex))
                    textView.rotationX = -90f
                    
                    // Geri kalan dönüşü tamamla
                    textView.animate()
                        ?.rotationX(0f)
                        ?.setDuration(200)
                        ?.setStartDelay(0)
                        ?.withEndAction {
                            completedAnimations++
                            // Tüm harflerin animasyonu bittiğinde kontrolü yap
                            if (completedAnimations == targetWord.length) {
                                onGuessAnimationFinished(guess, currentRow)
                            }
                        }
                        ?.start()
                }
                ?.start()
        }
    }
    //endregion

    //region Oyun Bitiş Kontrolü
    private fun onGuessAnimationFinished(guess: String, row: Int) {
        var gameOver = false
        if (guess == targetWord) {
            Toast.makeText(this, "Tebrikler!", Toast.LENGTH_LONG).show()
            gameOver = true
        } else if (row == maxTries - 1) {
            Toast.makeText(this, "Kelime: $targetWord", Toast.LENGTH_LONG).show()
            gameOver = true
        }

        if (gameOver) {
            val epochDays = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
            val prefs = getSharedPreferences("WordlePrefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("lastPlayedDay", epochDays).apply()
        } else {
            // Oyun bitmediyse yeni tahmin için girişleri tekrar aktifleştir
            binding.btnSubmitGuess.isEnabled = true
            binding.etGuess.isEnabled = true
        }
    }
    //endregion
}