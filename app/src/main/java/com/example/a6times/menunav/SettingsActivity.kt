package com.example.a6times.menunav

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.a6times.R
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnClearData = findViewById<MaterialButton>(R.id.btnClearData)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
        val etExamQuestionLimit = findViewById<EditText>(R.id.etExamQuestionLimit)

        // SharedPreferences kullanarak kayıtlı limit değerini alalım (varsayılan 10)
        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val currentLimit = sharedPref.getInt("ExamQuestionLimit", 10)
        etExamQuestionLimit.setText(currentLimit.toString())

        // Kullanıcı değeri değiştirdikçe anında kaydedelim
        etExamQuestionLimit.addTextChangedListener { text ->
            val newLimit = text.toString().toIntOrNull()
            if (newLimit != null && newLimit > 0) {
                sharedPref.edit().putInt("ExamQuestionLimit", newLimit).apply()
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnClearData.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Verileri Sıfırla")
                .setMessage("Tüm kayıtlı kelimeleriniz ve başarı geçmişiniz silinecek. Emin misiniz?")
                .setPositiveButton("Evet, Sil") { _, _ ->
                    Toast.makeText(this, "Veriler temizlendi", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("İptal", null)
                .show()
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}