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

    private var laughPlayer: MediaPlayer? = null
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

        // (اختياري) قراءة نوع المستخدم
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", null)
        Log.d("esraa", userType.toString())

        // تشغيل صوت الضحك أول ما تفتح الشاشة
        laughPlayer = MediaPlayer.create(this, R.raw.kids)
        laughPlayer?.start()

        handler = Handler(Looper.getMainLooper())
        handler?.postDelayed({
            // أوقف صوت الضحك وحرر الميديا بلاير
            try {
                laughPlayer?.stop()
            } catch (_: Exception) {
            }
            laughPlayer?.release()
            laughPlayer = null

            // شغّل موسيقى الخلفية بعد انتهاء الضحك
            startService(Intent(this, GameMusicService::class.java))

            // انتقل للشاشة التالية
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }, 5000)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler?.removeCallbacksAndMessages(null)
        try {
            laughPlayer?.stop()
        } catch (_: Exception) {
        }
        laughPlayer?.release()
        laughPlayer = null
    }
}
