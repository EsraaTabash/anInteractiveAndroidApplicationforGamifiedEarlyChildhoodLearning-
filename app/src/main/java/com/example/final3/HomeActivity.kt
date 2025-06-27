package com.example.final3

import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.GravityCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    private lateinit var loadingOverlaylayout: FrameLayout
    private lateinit var loadingRocket: LottieAnimationView
    private lateinit var tvScore: TextView
    private lateinit var prefs: SharedPreferences
    private var userType: String = "under6"

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)

        loadingOverlaylayout = findViewById(R.id.loadingOverlaylayout)
        loadingRocket = findViewById(R.id.loadingRocket)
        tvScore = findViewById(R.id.tv_score)

        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        val profileImage = drawerProfileImage
        val childName = drawerUserName
        val childEmail = drawerUserEmail

        userType = prefs.getString("user_type", "under6") ?: "under6"

        showLoading()

        if (userType == "under6") {
            // بيانات محلية فقط
            updateScore()
            showUnder6Profile(profileImage, childName, childEmail)

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Under6AgeFragment())
                .commit()

            hideLoading()
        } else {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener { doc ->
                        val score = doc.getLong("score")?.toInt() ?: prefs.getInt("game_score", 0)
                        val userName = doc.getString("name") ?: prefs.getString("user_name", "") ?: ""
                        val userEmail = doc.getString("email") ?: prefs.getString("user_email", "") ?: ""
                        val characterIndex = doc.getLong("character_id")?.toInt() ?: prefs.getInt("user_character_index", 1)
                        val characterDrawableId = when (characterIndex) {
                            1 -> R.drawable.ch1
                            2 -> R.drawable.ch2
                            3 -> R.drawable.ch3
                            4 -> R.drawable.ch4
                            5 -> R.drawable.ch5
                            6 -> R.drawable.ch6
                            else -> R.drawable.ch1
                        }

                        // احفظ القيم في SharedPreferences للتشغيل بدون نت لاحقًا
                        prefs.edit()
                            .putInt("game_score", score)
                            .putString("user_name", userName)
                            .putString("user_email", userEmail)
                            .putInt("user_character_index", characterIndex)
                            .putInt("user_character", characterDrawableId)
                            .apply()

                        updateScore()
                        showAbove6Profile(profileImage, childName, childEmail)

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, Above6AgeFragment())
                            .commit()

                        hideLoading()
                    }
                    .addOnFailureListener {
                        // فشل في الاتصال، استخدم البيانات المحلية
                        Toast.makeText(this, "فشل استرجاع بيانات المستخدم، سيتم تحميل البيانات المحلية.", Toast.LENGTH_SHORT).show()

                        updateScore()
                        showAbove6Profile(profileImage, childName, childEmail)

                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, Above6AgeFragment())
                            .commit()

                        hideLoading()
                    }
            } else {
                // لا يوجد uid، اعتمد على البيانات المحلية
                updateScore()
                showAbove6Profile(profileImage, childName, childEmail)

                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, Above6AgeFragment())
                    .commit()

                hideLoading()
            }
        }

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun showLoading() {
        loadingOverlaylayout.visibility = FrameLayout.VISIBLE
        loadingRocket.playAnimation()
    }

    private fun hideLoading() {
        loadingRocket.cancelAnimation()
        loadingOverlaylayout.visibility = FrameLayout.GONE
    }

    fun updateScore() {
        val score = prefs.getInt("game_score", 0)
        tvScore.text = "عدد النجــوم التي حصلت عليــها : $score"
        sendScoreToUnity(score)
    }

    private fun sendScoreToUnity(score: Int) {
        try {
            val unityPlayer = Class.forName("com.unity3d.player.UnityPlayer")
            val currentActivity = unityPlayer.getField("currentActivity").get(null)
            val unitySendMessage = unityPlayer.getMethod(
                "UnitySendMessage",
                String::class.java,
                String::class.java,
                String::class.java
            )

            unitySendMessage.invoke(null, "GameManager", "ReceiveScoreFromAndroid", score.toString())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "فشل إرسال السكور إلى اللعبة", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showUnder6Profile(image: ImageView, name: TextView, email: TextView) {
        val imgId = prefs.getInt("user_character", R.drawable.ch1)
        val defaultName = mapOf(
            R.drawable.ch1 to "ربانزل",
            R.drawable.ch2 to "موانا",
            R.drawable.ch3 to "آنا",
            R.drawable.ch4 to "كريستوف",
            R.drawable.ch5 to "هيرو",
            R.drawable.ch6 to "علاء الدين"
        )[imgId] ?: "شخصية افتراضية"

        image.setImageResource(imgId)
        name.text = defaultName
        email.visibility = TextView.GONE
    }

    private fun showAbove6Profile(image: ImageView, name: TextView, email: TextView) {
        val imgId = prefs.getInt("user_character", R.drawable.ch1)
        val userName = prefs.getString("user_name", "").orEmpty()
        val userEmail = prefs.getString("user_email", "").orEmpty()

        image.setImageResource(imgId)
        name.text = userName.ifEmpty { "بدون اسم" }
        email.text = userEmail
        email.visibility = TextView.VISIBLE
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
