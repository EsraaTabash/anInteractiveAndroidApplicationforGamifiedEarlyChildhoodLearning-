package com.example.final3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setContentView(R.layout.activity_home)

        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"
        val headerView = navigationView.getHeaderView(0)

        val profileImage = headerView.findViewById<ImageView>(R.id.profile_image)
        val childName = headerView.findViewById<TextView>(R.id.child_name)
        val childEmail = headerView.findViewById<TextView>(R.id.child_email)

        if (userType == "under6") {
            Toast.makeText(this, "العمر أقل من 6 سنوات", Toast.LENGTH_SHORT).show()
            val storedImageIndex = prefs.getInt("user_character", R.drawable.ch1)
            val defaultNames = mapOf(
                R.drawable.ch1 to "ربــانــزل",
                R.drawable.ch2 to "مــوانــا",
                R.drawable.ch3 to "الصديق المرح",
                R.drawable.ch4 to "البطل الشجاع",
                R.drawable.ch5 to "النجمة المضيئة",
                R.drawable.ch6 to "الفتى الذكي"
            )
            val defaultName = defaultNames[storedImageIndex] ?: "شخصية افتراضية"

            profileImage.setImageResource(storedImageIndex)
            childName.text = defaultName
            childEmail.visibility = View.GONE

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Under6AgeFragment())
                .commit()
        } else {
            // أكبر من 6 سنوات
            var storedName = prefs.getString("user_name", "") ?: ""
            var storedEmail = prefs.getString("user_email", "") ?: ""
            val storedImage = prefs.getInt("user_character", R.drawable.ch1)

            if (storedName.isBlank() || storedEmail.isBlank()) {
                // جلب البيانات من Firestore
                val firebaseUser = FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val uid = firebaseUser.uid
                    val db = FirebaseFirestore.getInstance()

                    db.collection("users").document(uid).get()
                        .addOnSuccessListener { document ->
                            if (document != null && document.exists()) {
                                storedName = document.getString("name") ?: "اسم غير متوفر"
                                storedEmail = document.getString("email") ?: "بريد غير متوفر"

                                // حفظ في SharedPreferences
                                val editor = prefs.edit()
                                editor.putString("user_name", storedName)
                                editor.putString("user_email", storedEmail)
                                editor.apply()

                                // تحديث الواجهة
                                profileImage.setImageResource(storedImage)
                                childName.text = storedName
                                childEmail.text = storedEmail
                                childEmail.visibility = View.VISIBLE
                            } else {
                                Toast.makeText(this, "بيانات المستخدم غير موجودة في قاعدة البيانات", Toast.LENGTH_SHORT).show()
                                showDefaultProfile(profileImage, childName, childEmail, storedImage)
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "خطأ في جلب البيانات: ${e.message}", Toast.LENGTH_SHORT).show()
                            showDefaultProfile(profileImage, childName, childEmail, storedImage)
                        }
                } else {
                    Toast.makeText(this, "المستخدم غير مسجل دخول!", Toast.LENGTH_SHORT).show()
                    showDefaultProfile(profileImage, childName, childEmail, storedImage)
                }
            } else {
                // البيانات موجودة في SharedPreferences
                profileImage.setImageResource(storedImage)
                childName.text = storedName
                childEmail.text = storedEmail
                childEmail.visibility = View.VISIBLE
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Above6AgeFragment())
                .commit()
        }

        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun showDefaultProfile(
        profileImage: ImageView,
        childName: TextView,
        childEmail: TextView,
        storedImage: Int
    ) {
        profileImage.setImageResource(storedImage)
        childName.text = "اسم غير متوفر"
        childEmail.text = "بريد غير متوفر"
        childEmail.visibility = View.VISIBLE
    }
}
