package com.example.a6times

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.data.Words
import com.example.a6times.data.WordsRepository
import com.example.a6times.menunav.AnalysisActivity
import com.google.android.material.button.MaterialButton

class ExamActivity : AppCompatActivity() {

    private lateinit var tvQuestionCount: TextView
    private lateinit var tvQuestionWord: TextView
    private lateinit var btnOption1: MaterialButton
    private lateinit var btnOption2: MaterialButton
    private lateinit var btnOption3: MaterialButton
    private lateinit var btnNextQuestion: MaterialButton
    private lateinit var examProgressBar: ProgressBar

    private val wordsRepository = WordsRepository()
    private var allWords = listOf<Words>()
    private var examWords = listOf<Words>()
    
    private var currentQuestionIndex = 0
    private var correctAnswersCount = 0
    private var selectedOptionIndex = -1
    private var isAnswerChecked = false

    private lateinit var currentOptions: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)

        tvQuestionCount = findViewById(R.id.tvQuestionCount)
        tvQuestionWord = findViewById(R.id.tvQuestionWord)
        btnOption1 = findViewById(R.id.btnOption1)
        btnOption2 = findViewById(R.id.btnOption2)
        btnOption3 = findViewById(R.id.btnOption3)
        btnNextQuestion = findViewById(R.id.btnNextQuestion)
        examProgressBar = findViewById(R.id.examProgressBar)
        
        val btnFinishExam = findViewById<MaterialButton>(R.id.btnFinishExam)

        btnNextQuestion.isEnabled = false

        btnFinishExam.setOnClickListener {
            showFinishDialog()
        }

        btnNextQuestion.setOnClickListener {
            if (!isAnswerChecked) {
                checkAnswer()
            } else {
                goToNextQuestion()
            }
        }

        val options = listOf(btnOption1, btnOption2, btnOption3)
        for (i in options.indices) {
            options[i].setOnClickListener {
                if (!isAnswerChecked) {
                    selectOption(i)
                }
            }
        }

        loadWords()
    }

    private fun loadWords() {
        wordsRepository.listenToWords(
            onDataChange = { words ->
                if (allWords.isEmpty()) { 
                    allWords = words
                    val readyWords = wordsRepository.getExamReadyWords(words)
                    if (readyWords.size >= 3) {
                        startExam(readyWords)
                    } else {
                        Toast.makeText(this, "Sınav için en az 3 kelime gerekli.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }
            },
            onError = { error ->
                Toast.makeText(this, "Hata: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun startExam(readyWords: List<Words>) {
        // En fazla 20 soruluk sınav
        val maxQuestions = if (readyWords.size >= 20) 20 else readyWords.size
        examWords = readyWords.shuffled().take(maxQuestions)
        
        currentQuestionIndex = 0
        correctAnswersCount = 0
        examProgressBar.max = examWords.size
        loadQuestion()
    }

    private fun loadQuestion() {
        if (currentQuestionIndex >= examWords.size) {
            finishExam()
            return
        }

        isAnswerChecked = false
        selectedOptionIndex = -1
        btnNextQuestion.text = "Kontrol Et"
        btnNextQuestion.isEnabled = false

        val currentWord = examWords[currentQuestionIndex]
        tvQuestionWord.text = currentWord.engWordName
        
        tvQuestionCount.text = "Soru: ${currentQuestionIndex + 1} / ${examWords.size}"
        examProgressBar.progress = currentQuestionIndex

        val wrongAnswers = allWords.filter { it.wordID != currentWord.wordID }.shuffled().take(2).map { it.turWordName }
        // Şıkların sayısı her zaman 3 olması için wrongAnswers listesi 2 adet olmalı (toplam en az 3 kelime olduğu için güvenli)
        currentOptions = (wrongAnswers + currentWord.turWordName).shuffled()

        btnOption1.text = currentOptions[0]
        btnOption2.text = currentOptions[1]
        btnOption3.text = currentOptions[2]

        resetOptionStyles()
    }

    private fun selectOption(index: Int) {
        selectedOptionIndex = index
        btnNextQuestion.isEnabled = true
        resetOptionStyles()
        
        val options = listOf(btnOption1, btnOption2, btnOption3)
        // Seçili butonu vurgula
        options[index].strokeColor = ColorStateList.valueOf(Color.parseColor("#BB86FC")) // Mor çerçeve
        options[index].setTextColor(Color.parseColor("#BB86FC"))
    }

    private fun resetOptionStyles() {
        val options = listOf(btnOption1, btnOption2, btnOption3)
        for (btn in options) {
            btn.strokeColor = ColorStateList.valueOf(Color.parseColor("#333333")) // Normal çerçeve
            btn.setTextColor(Color.parseColor("#FFFFFF"))
            btn.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        }
    }

    private fun checkAnswer() {
        isAnswerChecked = true
        btnNextQuestion.text = "Sonraki"
        
        val currentWord = examWords[currentQuestionIndex]
        val options = listOf(btnOption1, btnOption2, btnOption3)
        
        if (selectedOptionIndex != -1) {
            val selectedAnswer = currentOptions[selectedOptionIndex]
            val isCorrect = selectedAnswer == currentWord.turWordName
            
            if (isCorrect) {
                // Doğru cevaplandı
                options[selectedOptionIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Yeşil
                options[selectedOptionIndex].setTextColor(Color.parseColor("#4CAF50"))
                // Background tint vererek içerisini de doldurabiliriz. (isteğe bağlı)
                options[selectedOptionIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A4CAF50"))
                correctAnswersCount++
            } else {
                // Yanlış cevaplandı
                options[selectedOptionIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#F44336")) // Kırmızı
                options[selectedOptionIndex].setTextColor(Color.parseColor("#F44336"))
                options[selectedOptionIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1AF44336"))
                
                // Doğru cevabı göster
                val correctIndex = currentOptions.indexOf(currentWord.turWordName)
                if (correctIndex != -1) {
                    options[correctIndex].strokeColor = ColorStateList.valueOf(Color.parseColor("#4CAF50")) // Yeşil
                    options[correctIndex].setTextColor(Color.parseColor("#4CAF50"))
                    options[correctIndex].backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A4CAF50"))
                }
            }

            // Aralıklı tekrar sistemini (Spaced Repetition) Firebase'de güncelle
            wordsRepository.updateWordProgress(currentWord, isCorrect)
        }
    }

    private fun goToNextQuestion() {
        currentQuestionIndex++
        loadQuestion()
    }

    private fun showFinishDialog() {
        val intent = Intent(this, AnalysisActivity::class.java)
        AlertDialog.Builder(this)
            .setTitle("Sınavı Bitir")
            .setMessage("Sınavdan çıkıp başarı raporuna gitmek istediğinize emin misiniz?")
            .setPositiveButton("Evet, Raporu Gör") { _, _ ->
                intent.putExtra("correctCount", correctAnswersCount)
                intent.putExtra("totalCount", currentQuestionIndex)
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Devam Et", null)
            .show()
    }
    
    private fun finishExam() {
        val intent = Intent(this, AnalysisActivity::class.java)
        intent.putExtra("correctCount", correctAnswersCount)
        intent.putExtra("totalCount", examWords.size)
        startActivity(intent)
        finish()
    }
}
