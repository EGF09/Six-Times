package com.example.a6times

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import coil.load
import com.example.a6times.data.Words
import com.example.a6times.data.WordsRepository
import com.example.a6times.menunav.AnalysisActivity
import com.example.a6times.utils.Constants
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

/**
 * Kelime sınavı uygulamasının temel mantığını yürüten ekran.
 * Kelimeleri havuzdan seçer, soruları oluşturur ve ilerlemeyi kaydeder.
 */
class ExamActivity : AppCompatActivity() {

    private val PRIMARY_COLOR = "#00F0FF"

    private lateinit var tvQuestionCount: TextView
    private lateinit var tvQuestionWord: TextView

    private lateinit var btnOption1: MaterialButton
    private lateinit var btnOption2: MaterialButton
    private lateinit var btnOption3: MaterialButton

    private lateinit var btnNextQuestion: MaterialButton
    private lateinit var btnFinishExam: MaterialButton

    private lateinit var examProgressBar: ProgressBar
    private lateinit var ivQuestionImage: ImageView
    private lateinit var btnToggleImage: MaterialButton
    private lateinit var btnToggleHint: MaterialButton
    private lateinit var tvHintSentence: TextView

    private var isImageVisible = false
    private var isHintVisible = false
    private lateinit var currentWord: Words

    private val wordsRepository = WordsRepository()

    private var allWords = listOf<Words>()
    private var examWords = listOf<Words>()

    private var currentQuestionIndex = 0
    private var correctAnswersCount = 0

    private var selectedOptionIndex = -1
    private var isAnswerChecked = false

    private lateinit var currentOptions: List<String>

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)

        // Görünüm bileşenlerini tanımla
        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        tvQuestionWord = findViewById(R.id.tvQuestionWord)

        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)

        btnNextQuestion = findViewById(R.id.btnNextQuestion)
        btnFinishExam = findViewById(R.id.btnFinishExam)

        examProgressBar = findViewById(R.id.examProgressBar)
        ivQuestionImage = findViewById(R.id.ivQuestionImage)
        btnToggleImage = findViewById(R.id.btnToggleImage)
        btnToggleHint = findViewById(R.id.btnToggleHint)
        tvHintSentence = findViewById(R.id.tvHintSentence)

        btnNextQuestion.isEnabled = false

        btnFinishExam.setOnClickListener { showFinishDialog() }

        btnNextQuestion.setOnClickListener {
            if (!isAnswerChecked) checkAnswer()
            else goToNextQuestion()
        }

        // Görseli aç/kapat butonu
        btnToggleImage.setOnClickListener {
            isImageVisible = !isImageVisible
            ivQuestionImage.visibility = if (isImageVisible) View.VISIBLE else View.GONE
            btnToggleImage.text = if (isImageVisible) "Görseli Kapat" else "Görseli Aç"
        }

        // İpucu cümlesini aç/kapat butonu
        btnToggleHint.setOnClickListener {
            isHintVisible = !isHintVisible
            tvHintSentence.visibility = if (isHintVisible) View.VISIBLE else View.GONE
            btnToggleHint.text = if (isHintVisible) "İpucunu Kapat" else "İpucu Cümlesi"
        }

        val options = listOf(btnOption1, btnOption2, btnOption3)
        options.forEachIndexed { index, button ->
            button.setOnClickListener {
                if (!isAnswerChecked) selectOption(index)
            }
        }

        // Kelimeleri yükle ve sınavı başlat
        loadWords()
    }

    /**
     * Veritabanından kelimeleri çeker ve sınav limitine göre filtreler.
     */
    private fun loadWords() {
        wordsRepository.getWordsOnce(
            onDataChange = { words ->
                if (allWords.isEmpty()) {
                    allWords = words
                    val readyWords = wordsRepository.getExamReadyWords(words)

                    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "guest"
                    val limit = getSharedPreferences("${Constants.PREFS_SETTINGS}_$userId", MODE_PRIVATE)
                        .getInt(Constants.PREFS_KEY_EXAM_LIMIT, 10)

                    if (allWords.size < 3) {
                        Toast.makeText(this, "Sınav için havuzda en az 3 kelime olmalı!", Toast.LENGTH_SHORT).show()
                        finish()
                        return@getWordsOnce
                    }

                    if (readyWords.size >= limit) {
                        startExam(readyWords, limit)
                    } else {
                        Toast.makeText(this, "Sınava hazır yeterli kelimeniz yok!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            },
            onError = { finish() }
        )
    }

    /**
     * Rastgele seçilen kelimelerle sınavı başlatır.
     */
    private fun startExam(readyWords: List<Words>, limit: Int) {
        examWords = readyWords.shuffled().take(limit)
        currentQuestionIndex = 0
        correctAnswersCount = 0
        examProgressBar.max = examWords.size
        loadQuestion()
    }

    /**
     * Mevcut soru indeksindeki kelimeyi yükler ve arayüzü hazırlar.
     */
    private fun loadQuestion() {
        if (currentQuestionIndex >= examWords.size) {
            showExamCompleteDialog()
            return
        }

        isAnswerChecked = false
        selectedOptionIndex = -1
        btnNextQuestion.text = "Kontrol Et"
        btnNextQuestion.isEnabled = false

        currentWord = examWords[currentQuestionIndex]
        tvQuestionWord.text = currentWord.engWordName
        tvQuestionCount.text = "Soru: ${currentQuestionIndex + 1} / ${examWords.size}"
        examProgressBar.progress = currentQuestionIndex

        // Arayüzü sıfırla
        isImageVisible = false
        isHintVisible = false
        ivQuestionImage.visibility = View.GONE
        tvHintSentence.visibility = View.GONE
        btnToggleImage.text = "Görseli Aç"
        btnToggleHint.text = "İpucu Cümlesi"
        tvHintSentence.text = ""

        // Görsel yükleme
        btnToggleImage.visibility = View.VISIBLE
        if (currentWord.picture.isNotEmpty()) {
            ivQuestionImage.load(currentWord.picture) {
                crossfade(true)
                placeholder(android.R.color.transparent)
                error(android.R.color.transparent)
            }
        } else {
            ivQuestionImage.load(android.R.drawable.ic_menu_help) { crossfade(true) }
        }

        // İpucu cümlelerini getir
        btnToggleHint.visibility = View.GONE
        wordsRepository.getWordSamples(currentWord.wordID) { samples ->
            if (samples.isNotEmpty()) {
                tvHintSentence.text = samples.joinToString("\n")
                btnToggleHint.visibility = View.VISIBLE
            }
        }

        // Çeldiricileri oluştur
        val wrong = allWords
            .filter { it.wordID != currentWord.wordID }
            .shuffled()
            .take(2)
            .map { it.turWordName }

        val optionsList = (wrong + currentWord.turWordName).shuffled()
        if (optionsList.size < 3) {
            Toast.makeText(this, "Yeterli seçenek oluşturulamadı.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        currentOptions = optionsList

        btnOption1.text = currentOptions[0]
        btnOption2.text = currentOptions[1]
        btnOption3.text = currentOptions[2]

        resetUI()
    }

    /**
     * Bir seçeneği işaretler ve vurgular.
     */
    private fun selectOption(index: Int) {
        selectedOptionIndex = index
        btnNextQuestion.isEnabled = true
        resetUI()
        val buttons = listOf(btnOption1, btnOption2, btnOption3)
        buttons[index].strokeColor = ColorStateList.valueOf(Color.parseColor(PRIMARY_COLOR))
        buttons[index].setTextColor(Color.parseColor(PRIMARY_COLOR))
    }

    /**
     * Seçeneklerin renk ve vurgularını temizler.
     */
    private fun resetUI() {
        val buttons = listOf(btnOption1, btnOption2, btnOption3)
        buttons.forEach {
            it.strokeColor = ColorStateList.valueOf(Color.parseColor("#333333"))
            it.setTextColor(Color.WHITE)
            it.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    /**
     * Seçilen cevabın doğruluğunu kontrol eder ve puanı günceller.
     */
    private fun checkAnswer() {
        if (selectedOptionIndex == -1) return

        isAnswerChecked = true
        btnNextQuestion.text = "Sonraki"
        val buttons = listOf(btnOption1, btnOption2, btnOption3)
        val selected = currentOptions[selectedOptionIndex]
        val correct = currentWord.turWordName
        val correctIndex = currentOptions.indexOf(correct)

        if (selected == correct) {
            playRawSound(R.raw.correct_sound)
            buttons[selectedOptionIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
            buttons[selectedOptionIndex].setTextColor(Color.parseColor("#4CAF50"))
            correctAnswersCount++
        } else {
            playRawSound(R.raw.exam_fail)
            buttons[selectedOptionIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#F44336"))
            buttons[selectedOptionIndex].setTextColor(Color.parseColor("#F44336"))
            if (correctIndex != -1) {
                buttons[correctIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                buttons[correctIndex].setTextColor(Color.parseColor("#4CAF50"))
            }
        }
        // İlerlemeyi veritabanında güncelle
        wordsRepository.updateWordProgress(currentWord, selected == correct)
    }

    private fun goToNextQuestion() {
        currentQuestionIndex++
        loadQuestion()
    }

    private fun showFinishDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sınavı Bitir")
            .setMessage("Sınavı bitir ve başarı analiz raporunu görüntüle?")
            .setPositiveButton("Evet") { _, _ -> finishExam() }
            .setNegativeButton("Devam Et", null)
            .show()
    }

    private fun showExamCompleteDialog() {
        AlertDialog.Builder(this)
            .setTitle("Sınav Tamamlandı! 🎉")
            .setMessage("Tebrikler, sınavı başarıyla bitirdiniz! Başarı analiz raporunuzu görüntülemek ister misiniz?")
            .setCancelable(false)
            .setPositiveButton("Raporu Gör") { _, _ -> finishExam() }
            .setNegativeButton("Daha Sonra") { _, _ -> finish() }
            .show()
    }

    private fun finishExam() {
        val intent = Intent(this, AnalysisActivity::class.java)
        intent.putExtra("correctCount", correctAnswersCount)
        intent.putExtra("totalCount", examWords.size)
        startActivity(intent)
        finish()
    }

    /**
     * Ses efektlerini çalar.
     */
    private fun playRawSound(resourceId: Int) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(this, resourceId)
            mediaPlayer?.setOnPreparedListener { it.start() }
        } catch (e: Exception) { e.printStackTrace() }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
