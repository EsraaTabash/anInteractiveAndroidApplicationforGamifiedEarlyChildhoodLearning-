package com.example.final3

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    private lateinit var tvScore: TextView
    private lateinit var prefs: SharedPreferences
    private var userType: String = "under6"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userType = prefs.getString("user_type", "under6") ?: "under6"
        tvScore = findViewById(R.id.tv_score)

        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        val profileImage = drawerProfileImage
        val childName = drawerUserName
        val childEmail = drawerUserEmail

        updateScore() // يستدعي السكور ويعرضه ويخزنه ويرسله لـ Unity

        if (userType == "under6") {
            showUnder6Profile(profileImage, childName, childEmail)
        } else {
            showAbove6Profile(profileImage, childName, childEmail)
        }

        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
    }

    private fun updateScore(sendToUnity: Boolean = true) {
        if (userType == "under6") {
            val score = prefs.getInt("game_score", 0)
            tvScore.text = "عدد النجــوم التي حصلت عليــها : $score"
            if (sendToUnity) sendScoreToUnity(score)
        } else {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener {
                        val score = it.getLong("score")?.toInt() ?: 0
                        prefs.edit().putInt("game_score", score).apply()
                        tvScore.text = "عدد النجــوم التي حصلت عليــها : $score"
                        if (sendToUnity) sendScoreToUnity(score)
                    }
                    .addOnFailureListener {
                        tvScore.text = "عدد النجوم: غير متوفر"
                    }
            } else {
                tvScore.text = "عدد النجوم: غير مسجل دخول"
            }
        }
    }

    private fun sendScoreToUnity(score: Int) {
        try {
            val unityPlayer = Class.forName("com.unity3d.player.UnityPlayer")
            val currentActivity = unityPlayer.getField("currentActivity").get(null)
            val unitySendMessage = unityPlayer.getMethod("UnitySendMessage", String::class.java, String::class.java, String::class.java)

            unitySendMessage.invoke(null, "GameManager", "ReceiveScoreFromAndroid", score.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "فشل إرسال السكور إلى اللعبة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUnder6Profile(image: ImageView, name: TextView, email: TextView) {
        val imgId = prefs.getInt("user_character", R.drawable.ch1)
        val defaultName = mapOf(
            R.drawable.ch1 to "ربانزل", R.drawable.ch2 to "موانا", R.drawable.ch3 to "آنا",
            R.drawable.ch4 to "كريستوف", R.drawable.ch5 to "هيرو", R.drawable.ch6 to "علاء الدين"
        )[imgId] ?: "شخصية افتراضية"

        image.setImageResource(imgId)
        name.text = defaultName
        email.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, Under6AgeFragment())
            .commit()
    }

    private fun showAbove6Profile(image: ImageView, name: TextView, email: TextView) {
        var userName = prefs.getString("user_name", "").orEmpty()
        var userEmail = prefs.getString("user_email", "").orEmpty()
        val imgId = prefs.getInt("user_character", R.drawable.ch1)

        if (userName.isBlank() || userEmail.isBlank()) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            FirebaseFirestore.getInstance().collection("users").document(uid)
                .get()
                .addOnSuccessListener {
                    userName = it.getString("name") ?: "اسم غير متوفر"
                    userEmail = it.getString("email") ?: "بريد غير متوفر"
                    prefs.edit().putString("user_name", userName).putString("user_email", userEmail).apply()
                    setProfile(image, name, email, imgId, userName, userEmail)
                }
                .addOnFailureListener {
                    setDefaultProfile(image, name, email, imgId)
                }
        } else {
            setProfile(image, name, email, imgId, userName, userEmail)
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, Above6AgeFragment())
            .commit()
    }

    private fun setProfile(image: ImageView, name: TextView, email: TextView, imgId: Int, userName: String, userEmail: String) {
        image.setImageResource(imgId)
        name.text = userName
        email.text = userEmail
        email.visibility = View.VISIBLE
    }

    private fun setDefaultProfile(image: ImageView, name: TextView, email: TextView, imgId: Int) {
        image.setImageResource(imgId)
        name.text = "اسم غير متوفر"
        email.text = "بريد غير متوفر"
        email.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
        selectDrawerItem(R.id.nav_home)
        updateScore()
    }

    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }
}
