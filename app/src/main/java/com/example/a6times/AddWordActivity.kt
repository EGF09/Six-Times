package com.example.a6times

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class AddWordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_word)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        val btnSave = findViewById<Button>(R.id.btnSaveWord)
        btnSave.setOnClickListener {
            showSuccessDialog()
        }
    }

    private fun showSuccessDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Başarılı")
        builder.setMessage("Kelime kaydedildi.")

        builder.setPositiveButton("Ana Sayfaya Dön") { _, _ ->
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }

        builder.setNegativeButton("Yeni Kelime Ekle") { dialog, _ ->
            dialog.dismiss()
        }

        builder.create().show()
    }
}