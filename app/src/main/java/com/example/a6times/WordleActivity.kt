package com.example.a6times

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
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
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import java.util.concurrent.TimeUnit

class WordleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWordleBinding
    private var targetWord = ""
    private val maxTries = 6
    private var currentTry = 0
    private lateinit var cells: Array<Array<TextView?>>
    private val wordsRepository = WordsRepository()
    private var isGameInitialized = false
    private var flipMediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWordleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.etGuess.isEnabled = false
        binding.btnSubmitGuess.isEnabled = false

        binding.WordBackButton.setOnClickListener { finish() }

        binding.btnSubmitGuess.setOnClickListener {
            val guess = binding.etGuess.text.toString().uppercase(Locale.ENGLISH)
            if (guess.length == targetWord.length) {
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

        binding.etGuess.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString()
                if (input.isNotEmpty() && input.any { !it.isLetter() }) {
                    val filtered = input.filter { it.isLetter() }
                    binding.etGuess.setText(filtered)
                    binding.etGuess.setSelection(filtered.length)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        fetchDailyWord()
    }

    private fun fetchDailyWord() {
        wordsRepository.getWordsOnce(
            onDataChange = { wordsList ->
                if (isGameInitialized) return@listenToWords
                val learnedWords = wordsList.filter { it.isLearned || it.progress >= 6 }.sortedBy { it.wordID }
                if (learnedWords.isEmpty()) return@listenToWords
                if (isGameInitialized) return@getWordsOnce

                val learnedWords = wordsList.filter { it.isLearned || it.progress >= 6 }
                    .sortedBy { it.wordID }

                if (learnedWords.isEmpty()) {
                    Toast.makeText(this, "Henüz 6 aşamayı tamamlamış kelimeniz yok!", Toast.LENGTH_LONG).show()
                    return@getWordsOnce
                }

                val epochDays = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
                val todayWordIndex = (epochDays % learnedWords.size).toInt()
                targetWord = learnedWords[todayWordIndex].engWordName.uppercase(Locale.ENGLISH).filter { it.isLetter() }

                targetWord = selectedWord.uppercase(Locale.ENGLISH).filter { it.isLetter() }

                if (targetWord.isEmpty()) {
                    Toast.makeText(this, "Kelime formatı uygun değil.", Toast.LENGTH_SHORT).show()
                    return@getWordsOnce
                }
                
                isGameInitialized = true
                cells = Array(maxTries) { arrayOfNulls<TextView>(targetWord.length) }
                setupGrid()
                binding.etGuess.isEnabled = true
                binding.btnSubmitGuess.isEnabled = true
                binding.etGuess.filters = arrayOf(InputFilter.LengthFilter(targetWord.length))
            },
            onError = { Toast.makeText(this, "Hata: $it", Toast.LENGTH_SHORT).show() }
        )
    }

    private fun setupGrid() {

        binding.glWordleGrid.removeAllViews()

        binding.glWordleGrid.columnCount = targetWord.length
        binding.glWordleGrid.rowCount = maxTries

        binding.glWordleGrid.post {

            val marginSize = 8
            val displayMetrics = resources.displayMetrics

            val containerWidth =
                binding.hsvWordle.width.takeIf { it > 0 }
                    ?: displayMetrics.widthPixels

            val containerHeight =
                binding.hsvWordle.height.takeIf { it > 0 }
                    ?: (displayMetrics.heightPixels * 0.5).toInt()

            val widthToUse =
                containerWidth - (32 * displayMetrics.density).toInt()

            val heightToUse = containerHeight

            var cellWidth =
                (widthToUse / targetWord.length) - (marginSize * 2)

            val cellHeight =
                (heightToUse / maxTries) - (marginSize * 2)

            val minCellSize =
                (45 * displayMetrics.density).toInt()

            val maxCellSize =
                (70 * displayMetrics.density).toInt()

            var calculatedCellSize =
                minOf(cellWidth, cellHeight)

            if (calculatedCellSize < minCellSize) {
                calculatedCellSize = minCellSize
            }

            if (calculatedCellSize > maxCellSize) {
                calculatedCellSize = maxCellSize
            }

            // --- DEĞİŞİKLİK 1: GridLayout'ın kendisini ekranda ortalıyoruz ---
            val gridParams = binding.glWordleGrid.layoutParams
            gridParams.width = GridLayout.LayoutParams.WRAP_CONTENT
            binding.glWordleGrid.layoutParams = gridParams

            binding.glWordleGrid.alignmentMode =
                GridLayout.ALIGN_BOUNDS

            val dynamicTextSize =
                (calculatedCellSize / displayMetrics.density) * 0.4f

            for (row in 0 until maxTries) {

                for (col in 0 until targetWord.length) {

                    val textView = TextView(this)

                    // --- DEĞİŞİKLİK 2: columnSpec ve rowSpec'e weight (1f) ekliyoruz ---
                    // spec(index, alignment, weight) yapısı hücrelerin kaymasını önler ve eşit dağıtır.
                    val colSpec = GridLayout.spec(col, GridLayout.CENTER, 1f)
                    val rowSpec = GridLayout.spec(row, GridLayout.CENTER, 1f)

                    val params = GridLayout.LayoutParams(rowSpec, colSpec)

                    params.width = calculatedCellSize
                    params.height = calculatedCellSize

                    params.setMargins(
                        marginSize,
                        marginSize,
                        marginSize,
                        marginSize
                    )

                    textView.layoutParams = params

                    textView.gravity = Gravity.CENTER

                    textView.textSize = dynamicTextSize

                    textView.setTypeface(
                        null,
                        android.graphics.Typeface.BOLD
                    )

                    textView.setTextColor(Color.WHITE)

                    textView.setPadding(0, 0, 0, 0)

                    textView.includeFontPadding = false

                    val backgroundDrawable =
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.bg_word_cell
                        )?.mutate()

                    backgroundDrawable?.setTintList(null)

                    textView.background = backgroundDrawable

                    binding.glWordleGrid.addView(textView)

                    cells[row][col] = textView
                }
            }
        }
    }

    private fun checkGuess(guess: String) {
        if (currentTry >= maxTries) return

        binding.btnSubmitGuess.isEnabled = false
        binding.etGuess.isEnabled = false
        val currentRow = currentTry++

        for (i in 0 until targetWord.length) {
            val textView = cells[currentRow][i]
            textView?.text = guess[i].toString()

            val colorHex = when {
                guess[i] == targetWord[i] -> "#FF007A" // Doğru yer
                targetWord.contains(guess[i]) -> "#C9B458" // Yanlış yer
                else -> "#3A3A3C" // Yok
            }

            // Animasyon bloğu
            textView?.animate()
                ?.rotationX(90f)
                ?.setDuration(300)
                ?.setStartDelay(i * 200L) // Sıralı tetikleme (Burada zaten delay var)
                ?.withStartAction {
                    playFlipSound()
                }
                ?.withEndAction {
                    textView.background?.setTint(Color.parseColor(colorHex))

                    textView.animate()
                        ?.rotationX(0f)
                        ?.setDuration(300)
                        ?.start()
                }
                ?.start()
        }

        // Animasyonların toplam süresi kadar bekle ve oyunu devam ettir
        binding.glWordleGrid.postDelayed({
            onGuessAnimationFinished(guess, currentRow)
        }, (targetWord.length * 200L) + 600L)
    }

    private fun onGuessAnimationFinished(guess: String, row: Int) {
        if (guess == targetWord) {
            showConfetti()
            Toast.makeText(this, "Tebrikler!", Toast.LENGTH_LONG).show()
        } else if (row == maxTries - 1) {
            Toast.makeText(this, "Kelime: $targetWord", Toast.LENGTH_LONG).show()
        } else {
            binding.btnSubmitGuess.isEnabled = true
            binding.etGuess.isEnabled = true
        }
    }

    private fun showConfetti() {
        findViewById<KonfettiView>(R.id.konfettiView).start(
            Party(spread = 360, colors = listOf(Color.YELLOW, Color.MAGENTA, Color.CYAN), emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(150))
        )
    }

    private fun playFlipSound() {
        try {
            flipMediaPlayer?.release()
            flipMediaPlayer = MediaPlayer.create(this, R.raw.card_flip)
            flipMediaPlayer?.start()
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        flipMediaPlayer?.release()
    }
}