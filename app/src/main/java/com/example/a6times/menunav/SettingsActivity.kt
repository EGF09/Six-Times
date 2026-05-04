package com.example.a6times.menunav

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.R
import com.example.a6times.loginnav.LoginActivity
import com.google.android.material.button.MaterialButton

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnClearData = findViewById<MaterialButton>(R.id.btnClearData)
        val btnLogout = findViewById<MaterialButton>(R.id.btnLogout)

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
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}