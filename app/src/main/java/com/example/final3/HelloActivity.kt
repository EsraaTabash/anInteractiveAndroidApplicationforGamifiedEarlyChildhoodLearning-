package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class HelloActivity : AppCompatActivity() {
    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hello)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
//        val data = prefs.all.map { "${it.key}=${it.value}" }.joinToString("\n")
//
//        val file = File(getExternalFilesDir(null), "prefs_dump.txt")
//        file.writeText(data)

        val btnStart = findViewById<ImageView>(R.id.btnStart)
        btnStart.setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val userType = prefs.getString("user_type", null)

            if (userType.isNullOrEmpty()) {
                Log.d("esraa", userType.toString())
                startActivity(Intent(this, SelectUserTypeActivity::class.java))
            } else {
                startActivity(Intent(this, HomeActivity::class.java))
            }
            finish()
        }
    }
}
