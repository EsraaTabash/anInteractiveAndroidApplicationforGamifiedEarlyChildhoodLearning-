package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private var handler: Handler? = null
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
        setContentView(R.layout.activity_splash)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", null)
        Log.d("esraa", userType.toString())

        mediaPlayer = MediaPlayer.create(this, R.raw.kids)
        mediaPlayer?.start()

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            try {
                mediaPlayer?.stop()
            } catch (_: Exception) {
            }
            mediaPlayer?.release()
            mediaPlayer = null

            startService(Intent(this, GameMusicService::class.java))

            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
