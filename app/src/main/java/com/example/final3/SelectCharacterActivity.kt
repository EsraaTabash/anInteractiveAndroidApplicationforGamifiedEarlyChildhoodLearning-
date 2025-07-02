package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectCharacterActivity : AppCompatActivity() {

    private var selectedCharacterId: Int = -1
    private lateinit var prefs: SharedPreferences
    private lateinit var userType: String

    // شاشة التحميل
    private lateinit var loadingOverlaylayout: FrameLayout
    private lateinit var rocket2: LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_character)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // ربط الـ SharedPreferences
        prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userType = getUserPrefs("user_type", "under6")

        // ربط مكونات التحميل
        loadingOverlaylayout = findViewById(R.id.select_loadingOverlaylayout)
        rocket2 = findViewById(R.id.select_loadingRocket)

        setupCharacterGrid()
        setupStartButton()
    }

    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
    }

    private fun getUserPrefs(key: String, defaultValue: String): String {
        return prefs.getString(key, defaultValue) ?: defaultValue
    }

    private fun setupCharacterGrid() {
        val gridView: GridView = findViewById(R.id.characterGrid)
        val adapter = CharactersAdapter(this, selectedCharacterId) { selectedId ->
            selectedCharacterId = selectedId
        }
        gridView.adapter = adapter
    }

    private fun setupStartButton() {
        val btnStart: ImageView = findViewById(R.id.btnStart)
        btnStart.setOnClickListener {
            if (selectedCharacterId == -1) {
                Toast.makeText(this, "يرجى اختيار شخصية أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            saveCharacterSelectionToPrefs()
            saveUserToFirestoreOrNavigate()
        }
    }

    private fun saveCharacterSelectionToPrefs() {
        prefs.edit().apply {
            putInt("character_id", selectedCharacterId)
            if (userType == "under6") {
                putString("user_name", CharacterUtils.getNameByCharacterId(selectedCharacterId))
            }
            apply()
        }
    }

    private fun saveUserToFirestoreOrNavigate() {
        showLoading()

        val name = getUserPrefs("user_name", "")
        val email = getUserPrefs("user_email", "")
        val gameId = 0

        // المستخدم تحت 6 سنوات لا يحتاج Firebase
        if (userType == "under6") {
            prefs.edit().putBoolean("user_completed_onboarding", true).apply()
            hideLoading()
            navigateToHome()
            return
        }

        FirebaseAuth.getInstance().addAuthStateListener { auth ->
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to name,
                    "character_id" to selectedCharacterId,
                    "gameId" to gameId,
                    "score" to 0
                )


                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        prefs.edit().putBoolean("user_completed_onboarding", true).apply()
                        hideLoading()
                        navigateToHome()
                    }
                    .addOnFailureListener { e ->
                        hideLoading()
                        Log.e("FirestoreError", "فشل الحفظ: ", e)
                        Toast.makeText(this, "فشل في حفظ البيانات: ${e.message}", Toast.LENGTH_LONG).show()
                    }
            } else {
                hideLoading()
                Toast.makeText(this, "لم يتم تسجيل الدخول!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun showLoading() {
        loadingOverlaylayout.visibility = View.VISIBLE
        loadingOverlaylayout.isClickable = true
        rocket2.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        loadingOverlaylayout.visibility = View.GONE
        rocket2.visibility = View.GONE
    }
}
