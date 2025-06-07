package com.example.final3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat

class ProfileActivity : BaseActivity() {

    private lateinit var profileImageView: ImageView
    private lateinit var childNameTextView: TextView

    @SuppressLint("MissingInflatedId", "NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        profileImageView = findViewById(R.id.profile_image)
        childNameTextView = findViewById(R.id.child_name)

        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        // ربط الأزرار بالـ XML
        val editProfileBtn = findViewById<LinearLayout>(R.id.editProfileBtn)
        val addChildBtn = findViewById<LinearLayout>(R.id.addChildBtn)
        val logoutBtn = findViewById<LinearLayout>(R.id.logoutBtn)

        // عند الضغط على "تعديل الحساب"
        editProfileBtn.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        // عند الضغط على "إضافة طفل آخر"
        addChildBtn.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
            finish()
        }

        // عند الضغط على "تسجيل الخروج"
        logoutBtn.setOnClickListener {
            val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
            val dialog = AlertDialog.Builder(this).setView(dialogView).create()

            dialogView.findViewById<Button>(R.id.doneBtn).setOnClickListener {
                getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit().clear().apply()
                dialog.dismiss()
                startActivity(Intent(this, SigninActivity::class.java))
                finish()
            }

            dialogView.findViewById<Button>(R.id.cancelBtn).setOnClickListener {
                dialog.dismiss()
            }

            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadUserData()
    }

    private fun loadUserData() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val name = prefs.getString("user_name", "") ?: ""
        val characterResId = prefs.getInt("user_character", R.drawable.ch1)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

        profileImageView.setImageResource(characterResId)

        // إذا الاسم موجود ومكتوب يدويًا، اعرضه سواء كان under6 أو غيره
        if (name.isNotBlank()) {
            childNameTextView.text = "أهلاً بك يا $name"
        } else {
            // استخدم الاسم الافتراضي المرتبط بالصورة
            val defaultNames = mapOf(
                R.drawable.ch1 to "ربــانــزل",
                R.drawable.ch2 to "مــوانــا",
                R.drawable.ch3 to "الصديق المرح",
                R.drawable.ch4 to "البطل الشجاع",
                R.drawable.ch5 to "النجمة المضيئة",
                R.drawable.ch6 to "الفتى الذكي"
            )
            val defaultName = defaultNames[characterResId] ?: "شخصية افتراضية"
            childNameTextView.text = "يا $defaultName"
        }
    }

}
