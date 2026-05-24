package com.example.a6times.menunav

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.a6times.R
import com.example.a6times.data.WordsRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var chipGroupWords: ChipGroup
    private lateinit var btnGenerate: MaterialButton
    private lateinit var btnSave: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvFullStoryText: TextView
    private lateinit var ivFullStoryImage: ImageView
    private lateinit var tvStoryTitle: TextView
    private val wordsRepository = WordsRepository()
    
    private var generatedImageUrl: String? = null
    private var generatedStoryText: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_detail)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        chipGroupWords = findViewById(R.id.chipGroupWords)
        btnGenerate = findViewById(R.id.btnGenerate)
        btnSave = findViewById(R.id.btnSave)
        progressBar = findViewById(R.id.progressBar)
        tvFullStoryText = findViewById(R.id.tvFullStoryText)
        ivFullStoryImage = findViewById(R.id.ivFullStoryImage)
        tvStoryTitle = findViewById(R.id.tvStoryTitle)

        btnBack.setOnClickListener {
            finish()
        }

        // Load previously saved story and image if they exist
        val sharedPrefs = getSharedPreferences("WordChainPrefs", Context.MODE_PRIVATE)
        val lastImageUrl = sharedPrefs.getString("lastImageUrl", null)
        val lastStoryText = sharedPrefs.getString("lastStoryText", null)

        if (lastImageUrl != null && lastStoryText != null) {
            tvStoryTitle.text = "Son Hikayeniz"
            tvFullStoryText.text = lastStoryText
            ivFullStoryImage.load(lastImageUrl) {
                crossfade(true)
                placeholder(R.drawable.ic_launcher_background)
                error(R.drawable.ic_launcher_background)
            }
        } else {
            tvStoryTitle.text = "Yeni Hikaye Oluştur"
        }

        // Fetch words from repository
        loadUserWords()

        btnGenerate.setOnClickListener {
            val selectedWords = mutableListOf<String>()
            for (i in 0 until chipGroupWords.childCount) {
                val chip = chipGroupWords.getChildAt(i) as? Chip
                if (chip != null && chip.isChecked) {
                    selectedWords.add(chip.text.toString())
                }
            }

            if (selectedWords.isEmpty()) {
                Toast.makeText(this, "Lütfen en az bir kelime seçin.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            generateStoryAndImage(selectedWords)
        }

        btnSave.setOnClickListener {
            if (generatedImageUrl != null && generatedStoryText != null) {
                val prefs = getSharedPreferences("WordChainPrefs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("hasPreviousImage", true)
                    .putString("lastImageUrl", generatedImageUrl)
                    .putString("lastStoryText", generatedStoryText)
                    .apply()
                
                Toast.makeText(this, "Hikaye ve görsel başarıyla kaydedildi!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Kaydedilecek bir içerik yok.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserWords() {
        wordsRepository.listenToWords(
            onDataChange = { wordsList ->
                chipGroupWords.removeAllViews()
                if (wordsList.isEmpty()) {
                    val emptyChip = Chip(this@StoryDetailActivity).apply {
                        id = View.generateViewId()
                        text = "Henüz kelimeniz yok."
                        isCheckable = false
                        isClickable = false
                        chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.darker_gray))
                        setTextColor(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.white))
                    }
                    chipGroupWords.addView(emptyChip)
                } else {
                    wordsList.forEach { word ->
                        val chip = Chip(this).apply {
                            id = View.generateViewId()
                            text = word.engWordName // or turWordName
                            isCheckable = true
                            isClickable = true
                            chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.black))
                            setTextColor(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.white))
                        }
                        chipGroupWords.addView(chip)
                    }
                }
            },
            onError = { error ->
                chipGroupWords.removeAllViews()
                val errorChip = Chip(this@StoryDetailActivity).apply {
                    id = View.generateViewId()
                    text = "Kelimeler yüklenemedi."
                    isCheckable = false
                    isClickable = false
                    chipBackgroundColor = ColorStateList.valueOf(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.holo_red_dark))
                    setTextColor(ContextCompat.getColor(this@StoryDetailActivity, android.R.color.white))
                }
                chipGroupWords.addView(errorChip)
            }
        )
    }

    private fun generateStoryAndImage(words: List<String>) {
        lifecycleScope.launch {
            // UI State: Loading
            btnGenerate.isEnabled = false
            btnGenerate.text = "Eşzamanlı Olarak Oluşturuluyor..."
            progressBar.visibility = View.VISIBLE
            tvFullStoryText.text = ""
            btnSave.visibility = View.GONE

            try {
                coroutineScope {
                    // 1. Generate Story in Turkish using keyless Pollinations Text API via POST
                    val storyDeferred = async(Dispatchers.IO) {
                        try {
                            val url = URL("https://text.pollinations.ai/")
                            val connection = url.openConnection() as HttpURLConnection
                            connection.requestMethod = "POST"
                            connection.setRequestProperty("Content-Type", "application/json")
                            connection.doOutput = true

                            val jsonPayload = JSONObject().apply {
                                put("messages", JSONArray().apply {
                                    put(JSONObject().apply {
                                        put("role", "system")
                                        put("content", "Sen yaratıcı bir Türkçe hikaye yazarısın. SADECE TEK BİR HİKAYE YAZACAKSIN. Kesinlikle madde imi (bullet point) kullanma, numaralandırma yapma, birden fazla seçenek sunma. Çıktıların SADECE düz metin (plain text) olmalıdır. KESİNLİKLE JSON formatında dönme, hiçbir ek açıklama veya başlık yazma.")
                                    })
                                    put(JSONObject().apply {
                                        put("role", "user")
                                        put("content", "Aşağıdaki kelimeleri kullanarak 3-5 cümlelik yaratıcı, akıcı ve ilgi çekici tek bir Türkçe kısa hikaye yaz: ${words.joinToString(", ")}")
                                    })
                                })
                                put("model", "openai")
                                put("jsonMode", false)
                            }

                            connection.outputStream.use { os ->
                                val input = jsonPayload.toString().toByteArray(Charsets.UTF_8)
                                os.write(input, 0, input.size)
                            }

                            val rawResponse = connection.inputStream.bufferedReader().readText()
                            
                            // Pollinations occasionally returns a broken reasoning block or json instead of plain text.
                            // We use regex/string parsing to forcefully extract just the story content if it does this.
                            var cleanResponse = rawResponse
                            
                            if (cleanResponse.contains("\"content\":")) {
                                try {
                                    // It returned JSON despite being told not to. Parse it out.
                                    val json = JSONObject(cleanResponse)
                                    if (json.has("content")) {
                                        cleanResponse = json.getString("content")
                                    } else if (json.has("choices")) {
                                        cleanResponse = json.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content")
                                    }
                                } catch (e: Exception) {
                                    // If JSON parsing fails (broken JSON), use regex to find the content field
                                    val regex = "\"content\"\\s*:\\s*\"(.*?)\"".toRegex()
                                    val match = regex.find(cleanResponse)
                                    if (match != null) {
                                        cleanResponse = match.groupValues[1]
                                    }
                                }
                            }
                            
                            // Unescape common JSON artifacts if any survived
                            cleanResponse = cleanResponse.replace("\\n", "\n").replace("\\\"", "\"").trim()
                            
                            // Remove Markdown bolding and headers just in case
                            cleanResponse = cleanResponse.replace("**", "").replace("###", "")
                            
                            // If it STILL somehow includes the reasoning block, strip it manually
                            if (cleanResponse.contains("reasoning")) {
                                val split = cleanResponse.split("\"content\"")
                                if (split.size > 1) {
                                    val dirtyContent = split[1]
                                    val regex = ":\\s*\"(.*?)\"".toRegex()
                                    val match = regex.find(dirtyContent)
                                    if (match != null) {
                                        cleanResponse = match.groupValues[1].replace("\\n", "\n")
                                    }
                                }
                            }

                            cleanResponse
                        } catch (e: Exception) {
                            "Hikaye oluşturulamadı: ${e.message}"
                        }
                    }

                    // 2. Generate Image using keyless Pollinations Image API
                    val imagePromptDeferred = async(Dispatchers.IO) {
                        // For image, we use the words directly + art styles. 
                        // Added seed, dimensions, and nologo to force a fresh generation and bypass browser/coil cache
                        val promptText = "${words.joinToString(", ")}, epic fantasy, cinematic lighting, highly detailed, digital art"
                        val encodedPrompt = URLEncoder.encode(promptText, "UTF-8")
                        val randomSeed = System.currentTimeMillis()
                        "https://image.pollinations.ai/prompt/$encodedPrompt?seed=$randomSeed&width=1024&height=1024&nologo=true"
                    }

                    generatedStoryText = storyDeferred.await()
                    generatedImageUrl = imagePromptDeferred.await()
                    
                    // UI State: Success
                    progressBar.visibility = View.GONE
                    btnGenerate.visibility = View.GONE
                    tvStoryTitle.text = "İşte Hikayeniz"
                    tvFullStoryText.text = generatedStoryText
                    btnSave.visibility = View.VISIBLE
                    
                    // Load Image using Coil
                    ivFullStoryImage.load(generatedImageUrl) {
                        crossfade(true)
                        placeholder(R.drawable.ic_launcher_background) // fallback placeholder
                        error(R.drawable.ic_launcher_background)
                    }
                }
            } catch (e: Throwable) {
                Log.e("StoryDetailActivity", "Generation failed", e)
                progressBar.visibility = View.GONE
                btnGenerate.isEnabled = true
                btnGenerate.text = "Tekrar Dene"
                
                Toast.makeText(this@StoryDetailActivity, "Oluşturulamadı: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                tvStoryTitle.text = "Hata Oluştu"
                tvFullStoryText.text = "Bağlantı sorunu yaşandı:\n\n${e.localizedMessage}"
            }
        }
    }
}