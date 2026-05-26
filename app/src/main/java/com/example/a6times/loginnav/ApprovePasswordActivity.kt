package com.example.a6times.loginnav

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.a6times.R

/**
 * Şifre onaylama işlemlerinin yapıldığı ekran.
 */
class ApprovePasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Kenardan kenara görünümü etkinleştir
        enableEdgeToEdge()
        setContentView(R.layout.activity_approvepassword)
        
        // Sistem çubukları için pencere iç paylarını ayarla
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        val ConfirmPasswordButton = findViewById<Button>(R.id.ConfirmPasswordButton)
        val ApprovePasswordBackButton = findViewById<ImageButton>(R.id.ApprovePasswordBackButton)

        //region şifre onayla butonu işlevi
        ConfirmPasswordButton.setOnClickListener {
            // Şifre onaylandığında veya işlem bittiğinde önceki sayfaya dön
            finish()
        }
        //endregion

        //region geri butonu işlevi
        ApprovePasswordBackButton.setOnClickListener {
            // İşlemi iptal et ve geri dön
            finish()
        }
        //endregion
    }
}
