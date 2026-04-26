package com.example.a6times

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class ExamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)

        val btnFinishExam = findViewById<MaterialButton>(R.id.btnFinishExam)
        val btnNextQuestion = findViewById<MaterialButton>(R.id.btnNextQuestion)

        btnFinishExam.setOnClickListener {
            val intent = Intent(this, AnalysisActivity::class.java)

            AlertDialog.Builder(this)
                .setTitle("Sınavı Bitir")
                .setMessage("Sınavdan çıkıp başarı raporuna gitmek istediğinize emin misiniz?")
                .setPositiveButton("Evet, Raporu Gör") { _, _ ->
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Devam Et", null)
                .show()
        }

        btnNextQuestion.setOnClickListener {
            // Buraya sonraki soruya geçme mantığı gelecek
        }
    }
}