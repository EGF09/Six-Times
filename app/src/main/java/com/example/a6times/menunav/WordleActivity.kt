package com.example.a6times.menunav

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
import com.example.a6times.R
import com.example.a6times.data.WordsRepository
import com.example.a6times.databinding.ActivityWordleBinding
import com.example.a6times.utils.Constants
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Kelime tahmin oyunu (Wordle) ekranı.
 * Kullanıcının öğrendiği kelimeler arasından seçilen günün kelimesini tahmin etmesini sağlar.
 */
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

        // Başlangıçta giriş alanlarını pasif yap
        binding.etGuess.isEnabled = false
        binding.btnSubmitGuess.isEnabled = false

        binding.WordBackButton.setOnClickListener { finish() }

        // Tahmin gönder butonu işlevi
        binding.btnSubmitGuess.setOnClickListener {
            val guess = binding.etGuess.text.toString().uppercase(Locale.ENGLISH)
            if (guess.length == targetWord.length) {
                // Geçerlilik kontrolü
                if (guess.toSet().size == 1 && guess.length > 1) {
                    Toast.makeText(this, getString(R.string.wordle_invalid_word), Toast.LENGTH_SHORT).show()
                } else {
                    checkGuess(guess)
                    binding.etGuess.text.clear()
                }
            } else {
                Toast.makeText(this, getString(R.string.wordle_length_error, targetWord.length), Toast.LENGTH_SHORT).show()
            }
        }

        // Giriş alanına sadece harf girilmesini sağla
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

        // Günün kelimesini veritabanından getir
        fetchDailyWord()
    }

    /**
     * Kullanıcının öğrendiği kelimeler arasından günün kelimesini belirler.
     */
    private fun fetchDailyWord() {
        wordsRepository.getWordsOnce(
            onDataChange = { wordsList ->
                if (isGameInitialized) return@getWordsOnce

                // Sadece tam öğrenilmiş kelimeleri seç
                val learnedWords = wordsList.filter { it.isLearned || it.progress >= Constants.MAX_WORD_PROGRESS }
                    .sortedBy { it.wordID }

                if (learnedWords.isEmpty()) {
                    Toast.makeText(this, getString(R.string.wordle_no_words_error), Toast.LENGTH_LONG).show()
                    return@getWordsOnce
                }

                // Günlük değişen bir indeks hesapla
                val epochDays = System.currentTimeMillis() / Constants.ONE_DAY_MS
                val todayWordIndex = (epochDays % learnedWords.size).toInt()
                targetWord = learnedWords[todayWordIndex].engWordName.uppercase(Locale.ENGLISH).filter { it.isLetter() }

                if (targetWord.isEmpty()) {
                    Toast.makeText(this, getString(R.string.wordle_invalid_format), Toast.LENGTH_SHORT).show()
                    return@getWordsOnce
                }
                
                // Oyun alanını hazırla
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

    /**
     * Tahminlerin görüntüleneceği ızgara (grid) yapısını dinamik olarak oluşturur.
     */
    private fun setupGrid() {
        binding.glWordleGrid.removeAllViews()
        binding.glWordleGrid.columnCount = targetWord.length
        binding.glWordleGrid.rowCount = maxTries

        binding.glWordleGrid.post {
            val marginSize = 8
            val displayMetrics = resources.displayMetrics

            // Konteynır boyutlarını al
            val containerWidth = binding.hsvWordle.width.takeIf { it > 0 } ?: displayMetrics.widthPixels
            val containerHeight = binding.hsvWordle.height.takeIf { it > 0 } ?: (displayMetrics.heightPixels * 0.5).toInt()

            val widthToUse = containerWidth - (32 * displayMetrics.density).toInt()
            val heightToUse = containerHeight

            // Hücre boyutlarını hesapla
            var cellWidth = (widthToUse / targetWord.length) - (marginSize * 2)
            val cellHeight = (heightToUse / maxTries) - (marginSize * 2)

            val minCellSize = (45 * displayMetrics.density).toInt()
            val maxCellSize = (70 * displayMetrics.density).toInt()

            var calculatedCellSize = minOf(cellWidth, cellHeight)
            if (calculatedCellSize < minCellSize) calculatedCellSize = minCellSize
            if (calculatedCellSize > maxCellSize) calculatedCellSize = maxCellSize

            val gridParams = binding.glWordleGrid.layoutParams
            gridParams.width = GridLayout.LayoutParams.WRAP_CONTENT
            binding.glWordleGrid.layoutParams = gridParams

            binding.glWordleGrid.alignmentMode = GridLayout.ALIGN_BOUNDS
            val dynamicTextSize = (calculatedCellSize / displayMetrics.density) * 0.4f

            // Izgaradaki her bir hücreyi (TextView) oluştur
            for (row in 0 until maxTries) {
                for (col in 0 until targetWord.length) {
                    val textView = createCellView(row, col, calculatedCellSize, marginSize, dynamicTextSize)
                    binding.glWordleGrid.addView(textView)
                    cells[row][col] = textView
                }
            }
        }
    }

    /**
     * Tek bir Wordle hücresini oluşturur.
     */
    private fun createCellView(row: Int, col: Int, size: Int, margin: Int, textSizeF: Float): TextView {
        val textView = TextView(this)
        val colSpec = GridLayout.spec(col, GridLayout.CENTER, 1f)
        val rowSpec = GridLayout.spec(row, GridLayout.CENTER, 1f)
        val params = GridLayout.LayoutParams(rowSpec, colSpec)

        params.width = size
        params.height = size
        params.setMargins(margin, margin, margin, margin)

        textView.layoutParams = params
        textView.gravity = Gravity.CENTER
        textView.textSize = textSizeF
        textView.setTypeface(null, android.graphics.Typeface.BOLD)
        textView.setTextColor(Color.WHITE)
        textView.includeFontPadding = false

        // Hücre arka planı
        val backgroundDrawable = ContextCompat.getDrawable(this, R.drawable.bg_word_cell)?.mutate()
        backgroundDrawable?.setTintList(null)
        textView.background = backgroundDrawable

        return textView
    }

    /**
     * Kullanıcının tahminini kontrol eder ve renklerle geri bildirim verir.
     * 
     * @param guess Kullanıcının girdiği tahmin kelimesi.
     */
    private fun checkGuess(guess: String) {
        if (currentTry >= maxTries) return

        binding.btnSubmitGuess.isEnabled = false
        binding.etGuess.isEnabled = false
        val currentRow = currentTry++

        // Her bir harf için doğruluk kontrolü ve animasyon
        for (i in 0 until targetWord.length) {
            val textView = cells[currentRow][i]
            textView?.text = guess[i].toString()

            val cellColor = when {
                guess[i] == targetWord[i] -> ContextCompat.getColor(this, R.color.wordle_correct_exact)
                targetWord.contains(guess[i]) -> ContextCompat.getColor(this, R.color.wordle_correct_wrong_place)
                else -> ContextCompat.getColor(this, R.color.wordle_wrong)
            }

            // Harf döndürme animasyonu
            textView?.animate()
                ?.rotationX(90f)
                ?.setDuration(Constants.WORDLE_ANIMATION_DURATION)
                ?.setStartDelay(i * Constants.WORDLE_ANIMATION_DELAY_MULTIPLIER)
                ?.withStartAction { playFlipSound() }
                ?.withEndAction {
                    textView.background?.setTint(cellColor)
                    textView.animate()
                        ?.rotationX(0f)
                        ?.setDuration(Constants.WORDLE_ANIMATION_DURATION)
                        ?.start()
                }
                ?.start()
        }

        // Animasyon bitiminde oyun durumunu kontrol et
        binding.glWordleGrid.postDelayed({
            onGuessAnimationFinished(guess, currentRow)
        }, (targetWord.length * Constants.WORDLE_ANIMATION_DELAY_MULTIPLIER) + Constants.WORDLE_END_DELAY)
    }

    /**
     * Tahmin animasyonu bittiğinde kazanma/kaybetme durumunu kontrol eder.
     */
    private fun onGuessAnimationFinished(guess: String, row: Int) {
        if (guess == targetWord) {
            showConfetti()
            Toast.makeText(this, getString(R.string.wordle_congrats), Toast.LENGTH_LONG).show()
        } else if (row == maxTries - 1) {
            Toast.makeText(this, getString(R.string.wordle_word_is, targetWord), Toast.LENGTH_LONG).show()
        } else {
            binding.btnSubmitGuess.isEnabled = true
            binding.etGuess.isEnabled = true
        }
    }

    /**
     * Başarı durumunda konfeti efekti gösterir.
     */
    private fun showConfetti() {
        findViewById<KonfettiView>(R.id.konfettiView).start(
            Party(spread = 360, colors = listOf(Color.YELLOW, Color.MAGENTA, Color.CYAN), emitter = Emitter(duration = 2, TimeUnit.SECONDS).perSecond(150))
        )
    }

    /**
     * Harf açılma sesini çalar.
     */
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
