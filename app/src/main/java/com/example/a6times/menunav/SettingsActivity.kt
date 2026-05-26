package com.example.a6times.menunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.a6times.R
import com.example.a6times.loginnav.LoginActivity
import com.google.android.material.button.MaterialButton

/**
 * Uygulama ayarlarının (Sınav limitleri, Çıkış yapma vb.) yönetildiği ekran.
 */
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val etExamQuestionLimit = findViewById<EditText>(R.id.etExamQuestionLimit)

        // Mevcut ayarları SharedPreferences'tan oku
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentLimit = sharedPref.getInt("ExamQuestionLimit", 10)
        etExamQuestionLimit.setText(currentLimit.toString())

        // Klavyeyi kapatma yönetimi
        etExamQuestionLimit.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(etExamQuestionLimit.windowToken, 0)
            }
        }

        // Sınav soru limitini dinamik olarak güncelle ve kaydet
        etExamQuestionLimit.addTextChangedListener { text ->
            val input = text.toString()
            if (input.isNotEmpty()) {
                val newLimit = input.toIntOrNull()
                if (newLimit != null) {
                    // Limit değerini 1 ile 100 arasında sınırla
                    val finalValue = when {
                        newLimit < 1 -> 1
                        newLimit > 100 -> 100
                        else -> newLimit
                    }

                    if (newLimit != finalValue) {
                        etExamQuestionLimit.setText(finalValue.toString())
                        etExamQuestionLimit.setSelection(finalValue.toString().length)
                    }

                    // Yeni limiti kaydet
                    sharedPref.edit().putInt("ExamQuestionLimit", finalValue).apply()
                }
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        // Çıkış yap butonu
        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            // Tüm aktivite yığınını temizle ve login ekranına dön
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
