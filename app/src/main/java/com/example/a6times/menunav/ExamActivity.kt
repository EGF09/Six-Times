package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.R
import com.google.android.material.button.MaterialButton

/**
 * Kelime sınavı ekranı.
 * Kullanıcının kelime bilgisini test ettiği ve sonuçları takip edebildiği bölümdür.
 */
class ExamActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exam)

        val btnFinishExam = findViewById<MaterialButton>(R.id.btnFinishExam)
        val btnNextQuestion = findViewById<MaterialButton>(R.id.btnNextQuestion)

        //region Sınavı Bitir Buton İşlevi
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
        //endregion

        //region Sonraki Soru Buton İşlevi
        btnNextQuestion.setOnClickListener {
            // TODO: Sonraki soruya geçme mantığı uygulanacak
        }
        //endregion
    }
}
