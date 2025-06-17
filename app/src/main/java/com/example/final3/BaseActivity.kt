package com.example.final3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
open class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var menuIcon: ImageView
    lateinit var drawerProfileImage: ImageView
    lateinit var drawerUserName: TextView
    lateinit var drawerUserEmail: TextView

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        val activityContainer = fullView.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(fullView)

        drawerLayout = fullView
        navigationView = fullView.findViewById(R.id.navigation_view)
        menuIcon = fullView.findViewById(R.id.menu_icon)

        val headerView = navigationView.getHeaderView(0)
        drawerProfileImage = headerView.findViewById(R.id.profile_image)
        drawerUserName = headerView.findViewById(R.id.child_name)
        drawerUserEmail = headerView.findViewById(R.id.child_email)

        setupDrawer()
    }

    fun selectDrawerItem(itemId: Int) {
        navigationView.setCheckedItem(itemId)
    }

    open fun updateDrawerProfile() {
        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val characterId = prefs.getInt("character_id", 1)
        val userType = prefs.getString("user_type", "under6") ?: "under6"
        val userName = prefs.getString("user_name", CharacterUtils.getNameByCharacterId(characterId))

        drawerProfileImage.setImageResource(CharacterUtils.getDrawableByCharacterId(characterId))
        drawerUserName.text = "$userName"

        if (userType != "under6") {
            val email = prefs.getString("user_email", "") ?: ""
            drawerUserEmail.text = email
            drawerUserEmail.visibility = View.VISIBLE
        } else {
            drawerUserEmail.text = ""
            drawerUserEmail.visibility = View.GONE
        }
    }

    private fun setupDrawer() {
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()

            val targetActivity = when (menuItem.itemId) {
                R.id.nav_home -> HomeActivity::class.java
                R.id.nav_profile -> ProfileActivity::class.java
                R.id.nav_about -> AboutActivity::class.java
                else -> null
            }

            if (targetActivity != null) {
                if (this::class.java != targetActivity) {
                    val intent = Intent(this, targetActivity)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                when (menuItem.itemId) {
                    R.id.nav_share -> shareApp()
                    R.id.nav_logout -> logout()
                }
            }

            true
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "تطبيقنا الرائع!")
            putExtra(Intent.EXTRA_TEXT, "https://play.google.com/store/apps/details?id=com.example.final2")
        }
        startActivity(Intent.createChooser(intent, "مشاركة التطبيق عبر"))
    }

    private fun logout() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_logout, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        dialogView.findViewById<Button>(R.id.doneBtn).setOnClickListener {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val isFirstLaunch = prefs.getBoolean("is_first_launch", false)
            prefs.edit().clear().apply()
            prefs.edit().putBoolean("is_first_launch", isFirstLaunch).apply()

            try {
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
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

    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }

    override fun onResume() {
        super.onResume()
        updateDrawerProfile()
        GameMusicService.resumeMusic()
    }
}
