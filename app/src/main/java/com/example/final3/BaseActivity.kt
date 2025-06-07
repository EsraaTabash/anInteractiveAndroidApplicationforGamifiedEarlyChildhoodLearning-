package com.example.final3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var menuIcon: ImageView

    override fun setContentView(layoutResID: Int) {
        val fullView = layoutInflater.inflate(R.layout.activity_base, null) as DrawerLayout
        val activityContainer = fullView.findViewById<FrameLayout>(R.id.activity_content)
        layoutInflater.inflate(layoutResID, activityContainer, true)
        super.setContentView(fullView)

        drawerLayout = fullView
        navigationView = fullView.findViewById(R.id.navigation_view)
        menuIcon = fullView.findViewById(R.id.menu_icon)

        setupDrawer()
    }

    private fun setupDrawer() {
        menuIcon.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.END)
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            menuItem.isChecked = true
            drawerLayout.closeDrawers()

            when (menuItem.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                }
//                R.id.nav_settings -> {
//                    startActivity(Intent(this, SettingsActivity::class.java))
//                }
                R.id.nav_share -> {
                    shareApp()
                }
                R.id.nav_about -> {
                    showAboutDialog()
                }
                R.id.nav_logout -> {
                    logout()
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

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("حول التطبيق")
            .setMessage("هذا التطبيق تم تطويره لمساعدة الطلاب في إدارة وقتهم ومتابعة مهامهم اليومية بسهولة.")
            .setPositiveButton("حسناً", null)
            .show()
    }

    private fun logout() {
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
