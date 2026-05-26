package com.example.a6times.menunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.a6times.ExamActivity
import com.example.a6times.R
import com.example.a6times.WordleActivity
import com.example.a6times.data.UsersRepository
import com.example.a6times.data.WordsRepository
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

/**
 * Uygulamanın ana ekranı.
 * Kullanıcı ilerlemesini, günlük seriyi (streak) gösterir ve diğer bölümlere erişim sağlar.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var tvStreakCount: TextView
    private val wordsRepository = WordsRepository()
    private val userRepo = UsersRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Hoş geldin ismi için dinamik atama
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        var userName = sharedPref.getString("UserName", "Misafir")
        tvWelcome.text = "Hoş geldin $userName"

        // Kullanıcı oturum açmışsa ismi her ihtimale karşı Firebase'den güncel olarak çek
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            lifecycleScope.launch {
                val user = userRepo.getUser(currentUser.uid)
                if (user != null) {
                    userName = user.userName
                    tvWelcome.text = "Hoş geldin $userName"
                    sharedPref.edit().putString("UserName", userName).apply()
                }
            }
        }

        // Diğer bileşenlerin tanımlanması
        tvStreakCount = findViewById(R.id.tvStreakCount)
        val ivSettings = findViewById<ImageView>(R.id.ivSettings)
        val btnStartQuiz = findViewById<MaterialButton>(R.id.btnStartQuiz)
        val btnAnalysis = findViewById<MaterialButton>(R.id.btnAnalysis)
        val btnMyWords = findViewById<MaterialButton>(R.id.btnMyWords)
        val btnWordle = findViewById<MaterialButton>(R.id.btnWordle)
        val cardAiStory = findViewById<CardView>(R.id.cardAiStory)

        // Buton tıklamaları ve navigasyon işlemleri
        ivSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btnStartQuiz.setOnClickListener {
            startActivity(Intent(this, ExamActivity::class.java))
        }

        btnAnalysis.setOnClickListener {
            startActivity(Intent(this, AnalysisActivity::class.java))
        }

        btnMyWords.setOnClickListener {
            startActivity(Intent(this, WordActivity::class.java))
        }

        btnWordle.setOnClickListener {
            startActivity(Intent(this, WordleActivity::class.java))
        }

        cardAiStory.setOnClickListener {
            startActivity(Intent(this, StoryDetailActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()

        // 1. İSİM GÜNCELLEME (Kayıt ekranından gelen ismi burada alıyoruz)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val userName = userPrefs.getString("UserName", "Misafir")
        tvWelcome.text = "Hoş geldin $userName"

        // 2. STREAK SİSTEMİ: Kullanıcının her gün giriş yapıp yapmadığını kontrol eder
        updateStreakSystem()

        // 3. İLERLEME ÇUBUĞU: Toplam öğrenilen kelime sayısını gösterir
        val tvProgressPercent = findViewById<TextView>(R.id.tvProgressPercent)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        wordsRepository.getWordsOnce(
            onDataChange = { wordsList ->
                if (wordsList.isEmpty()) {
                    tvProgressPercent.text = "%0 (0/0)"
                    progressBar.progress = 0
                } else {
                    val totalWords = wordsList.size
                    // Öğrenilmiş (progress >= 6) veya isLearned işaretli kelimeleri say
                    val learnedWords = wordsList.count { it.isLearned || it.progress >= 6 }
                    val percent = if (totalWords > 0) ((learnedWords.toDouble() / totalWords) * 100).toInt() else 0

                    tvProgressPercent.text = "%$percent ($learnedWords/$totalWords)"
                    progressBar.progress = percent
                }
            },
            onError = {
                tvProgressPercent.text = "%0"
                progressBar.progress = 0
            }
        )

        // 4. HİKAYE RESMİ YÜKLEME: Son oluşturulan hikaye görselini yükler
        val sharedPrefs = getSharedPreferences("WordChainPrefs", Context.MODE_PRIVATE)
        val lastImageUrl = sharedPrefs.getString("lastImageUrl", null)

        if (lastImageUrl != null) {
            val ivStoryPreview = findViewById<ImageView>(R.id.ivStoryPreview)
            ivStoryPreview.load(lastImageUrl) {
                crossfade(true)
            }
        }
    }

    /**
     * Kullanıcının günlük serisini (streak) günceller.
     * Eğer kullanıcı üst üste günlerde giriş yaparsa seri artar, ara verirse 1'e döner.
     */
    private fun updateStreakSystem() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val currentDay = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastLoginDay = prefs.getLong("lastLoginDay", 0L)
        var currentStreak = prefs.getInt("currentStreak", 0)

        if (lastLoginDay == 0L) {
            // İlk giriş
            currentStreak = 1
        } else {
            val dayDifference = currentDay - lastLoginDay
            when (dayDifference) {
                1L -> currentStreak++ // Tam bir gün geçmiş, seri devam ediyor
                0L -> { /* Aynı gün içinde giriş yapılmış, seri değişmez */ }
                else -> currentStreak = 1 // Bir günden fazla ara verilmiş, seri sıfırlandı
            }
        }

        // Yeni değerleri kaydet
        prefs.edit().apply {
            putLong("lastLoginDay", currentDay)
            putInt("currentStreak", currentStreak)
            apply()
        }

        tvStreakCount.text = currentStreak.toString()
    }
}
