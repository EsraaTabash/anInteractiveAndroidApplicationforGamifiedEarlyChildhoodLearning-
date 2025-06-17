package com.example.final3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : BaseActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var childNameTextView: TextView
    private lateinit var editProfileBtn: LinearLayout
    private lateinit var addChildBtn: LinearLayout
    private lateinit var logoutBtn: LinearLayout

    // عناصر كتم الصوت
    private lateinit var muteBtn: LinearLayout
    private lateinit var muteText: TextView
    private var isMuted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // ربط العناصر
        profileImageView = findViewById(R.id.profile_image)
        childNameTextView = findViewById(R.id.child_name)
        editProfileBtn = findViewById(R.id.editProfileBtn)
        addChildBtn = findViewById(R.id.addChildBtn)
        logoutBtn = findViewById(R.id.logoutBtn)
        muteBtn = findViewById(R.id.muteBtn)
        muteText = findViewById(R.id.muteText)

        // قراءة حالة الصوت من SharedPreferences
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        isMuted = prefs.getBoolean("is_muted", false)
        updateMuteUI()

        muteBtn.setOnClickListener {
            isMuted = !isMuted
            prefs.edit().putBoolean("is_muted", isMuted).apply()
            updateMuteUI()
            if (isMuted) {
                GameMusicService.pauseMusic()
            } else {
                GameMusicService.resumeMusic()
            }
        }

        editProfileBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }
        addChildBtn.setOnClickListener {
            startActivity(Intent(this, SelectUserTypeActivity::class.java))
            finish()
        }
        logoutBtn.setOnClickListener {
            showLogoutDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
        updateDrawerProfile()
        selectDrawerItem(R.id.nav_profile)
        GameMusicService.resumeMusic()

    }


    private fun loadUserData() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"
        val characterId = prefs.getInt("character_id", 1)
        val localName = prefs.getString("user_name", "")

        // دائماً، اعرض من SharedPreferences (سريع)
        profileImageView.setImageResource(CharacterUtils.getDrawableByCharacterId(characterId))
        childNameTextView.text = if (!localName.isNullOrBlank())
            "أهلاً بك يا $localName"
        else
            "يا ${CharacterUtils.getNameByCharacterId(characterId)}"

        // فقط إذا أكبر من 6 سنوات، حدث من الفيرستور
        if (userType != "under6") {
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            if (!userId.isNullOrBlank()) {
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(userId).get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val name = doc.getString("name") ?: localName ?: ""
                            val characterIdFS = doc.getLong("character_id")?.toInt() ?: characterId

                            profileImageView.setImageResource(CharacterUtils.getDrawableByCharacterId(characterIdFS))
                            childNameTextView.text = if (name.isNotBlank())
                                "أهلاً بك يا $name"
                            else
                                "يا ${CharacterUtils.getNameByCharacterId(characterIdFS)}"

                            // تحديث البريف لضمان التزامن
                            val editor = prefs.edit()
                            editor.putString("user_name", name)
                            editor.putInt("character_id", characterIdFS)
                            editor.apply()
                        }
                    }
            }
        }
    }

    private fun updateMuteUI() {
        muteText.text = if (isMuted) "تشغيل الصوت" else "كتم الصوت"
    }

    private fun showLogoutDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.doneBtn).setOnClickListener {
            // الحفاظ على is_first_launch
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val isFirstLaunch = prefs.getBoolean("is_first_launch", false)
            prefs.edit().clear().apply()
            prefs.edit().putBoolean("is_first_launch", isFirstLaunch).apply()
            try {
                FirebaseAuth.getInstance().signOut()
            } catch (_: Exception) {}
            dialog.dismiss()
            val intent = Intent(this, HelloActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        dialogView.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }
}
