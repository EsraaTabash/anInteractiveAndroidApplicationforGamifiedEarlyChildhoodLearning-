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
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : BaseActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userType = prefs.getString("user_type", "under6") ?: "under6"

       // val navigationView = findViewById<NavigationView>(R.id.navigation_view)
        val menuIcon = findViewById<ImageView>(R.id.menu_icon)
      //  val header = navigationView.getHeaderView(0)
        val profileImage = drawerProfileImage
        val childName = drawerUserName
        val childEmail = drawerUserEmail

     //   val profileImage = header.findViewById<ImageView>(R.id.profile_image)
   //     val childName = header.findViewById<TextView>(R.id.child_name)
     //   val childEmail = header.findViewById<TextView>(R.id.child_email)

        if (userType == "under6") {
            showUnder6Profile(prefs, profileImage, childName, childEmail)
        } else {
           showAbove6Profile(prefs, profileImage, childName, childEmail)
        }

        menuIcon.setOnClickListener { drawerLayout.openDrawer(GravityCompat.END) }
    }

    private fun showUnder6Profile(prefs: SharedPreferences, image: ImageView, name: TextView, email: TextView) {
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

    private fun showAbove6Profile(prefs: SharedPreferences, image: ImageView, name: TextView, email: TextView) {
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

    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
        selectDrawerItem(R.id.nav_home)
    }
}
