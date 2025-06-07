package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.GridView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SelectCharacterActivity : AppCompatActivity() {

    private val characterNumberToDrawable = mapOf(
        1 to R.drawable.ch1,
        2 to R.drawable.ch2,
        3 to R.drawable.ch3,
        4 to R.drawable.ch4,
        5 to R.drawable.ch5,
        6 to R.drawable.ch6
    )

    private var selectedDrawable: Int = -1
    private lateinit var userType: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_character)

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        userType = prefs.getString("user_type", "under6") ?: "under6"

        val gridView: GridView = findViewById(R.id.characterGrid)
        val characterImages = characterNumberToDrawable.values.toIntArray()

        val adapter = CharactersAdapter(this, characterImages) { selectedResId ->
            selectedDrawable = selectedResId
        }
        gridView.adapter = adapter

        val btnStart: ImageView = findViewById(R.id.btnStart)
        btnStart.setOnClickListener {
            if (selectedDrawable == -1) {
                Toast.makeText(this, "يرجى اختيار شخصية أولاً", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val editor = prefs.edit()
            editor.putInt("user_character", selectedDrawable)

            if (userType == "under6") {
                val nameByDrawable = mapOf(
                    R.drawable.ch1 to "ربـــانــزل",
                    R.drawable.ch2 to "مــوانــا",
                    R.drawable.ch3 to "الصديق المرح",
                    R.drawable.ch4 to "البطل الشجاع",
                    R.drawable.ch5 to "النجمة المضيئة",
                    R.drawable.ch6 to "الفتى الذكي"
                )
                val defaultName = nameByDrawable[selectedDrawable] ?: "شخصية افتراضية"
                editor.putString("user_name", defaultName)
//                editor.putString("user_email", "")
            }

            editor.apply()

            // 🔥 حفظ البيانات في Firestore
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()

            if (firebaseUser != null) {
                val uid = firebaseUser.uid
                val email = firebaseUser.email ?: ""

                val name = prefs.getString("user_name", "") ?: ""
                val character = selectedDrawable
                val gameId = 0

                val userData = hashMapOf(
                    "uid" to uid,
                    "email" to email,
                    "name" to name,
                    "character" to character,
                    "gameId" to gameId
                )

                db.collection("users").document(uid)
                    .set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "تم حفظ بيانات المستخدم", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "فشل في حفظ البيانات: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "المستخدم غير مسجل دخول!", Toast.LENGTH_SHORT).show()
            }

            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }
}
