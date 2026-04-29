package com.example.a6times

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.a6times.menunav.HomeActivity
import com.example.a6times.loginnav.ForgotActivity
import com.example.a6times.loginnav.RegisterActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.GirisYapButton)?.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }


        findViewById<Button>(R.id.KayitOlButton)?.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<Button>(R.id.SifremiUnuttumButton)?.setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }

    }
}