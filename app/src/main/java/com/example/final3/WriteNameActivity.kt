package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import androidx.appcompat.app.AppCompatActivity

class WriteNameActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_write_name)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val nameFieldLayout = findViewById<TextInputLayout>(R.id.select_nameFieldLayout)
        val nameInput = findViewById<TextInputEditText>(R.id.select_nameInput)
        val btnStart = findViewById<ImageView>(R.id.btnStart)

        btnStart.setOnClickListener {
            val name = nameInput.text.toString().trim()
            if (name.isEmpty()) {
                nameFieldLayout.error = "من فضلك أدخل الاسم"
            } else {
                nameFieldLayout.error = null

                val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                prefs.edit().putString("user_name", name).apply()

                startActivity(Intent(this, SelectCharacterActivity::class.java))
                finish()
            }
        }
    }
}
