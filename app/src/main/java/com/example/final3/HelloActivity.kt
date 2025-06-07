package com.example.final3

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HelloActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_hello)
        supportActionBar?.hide() // إخفاء شريط العنوان
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val btnStart = findViewById<ImageView>(R.id.btnStart)
        btnStart.setOnClickListener {
            val intent = Intent(this, SelectUserTypeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

}
