package com.example.final3

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class GameMusicService : Service() {

    companion object {
        private var mediaPlayer: MediaPlayer? = null
        private var isPaused: Boolean = false

        fun pauseMusic() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                    isPaused = true
                }
            }
        }

        fun resumeMusic() {
            mediaPlayer?.let {
                if (isPaused) {
                    it.start()
                    isPaused = false
                }
            }
        }

        fun stopMusic() {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
            isPaused = false
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.background)
            mediaPlayer?.isLooping = true
            mediaPlayer?.start()
            isPaused = false
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        stopMusic()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
