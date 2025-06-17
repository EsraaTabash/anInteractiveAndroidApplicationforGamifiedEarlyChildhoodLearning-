package com.example.final3

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : BaseActivity() {
    override fun onResume() {
        super.onResume()
        selectDrawerItem(R.id.nav_about)
        GameMusicService.resumeMusic()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        supportActionBar?.hide()
    }
}
