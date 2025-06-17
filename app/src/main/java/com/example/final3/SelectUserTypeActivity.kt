package com.example.final3

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SelectUserTypeActivity : AppCompatActivity() {
    override fun onPause() {
        super.onPause()
        GameMusicService.pauseMusic()
    }
    override fun onResume() {
        super.onResume()
        GameMusicService.resumeMusic()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_user_type)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val textPlayDirectly = findViewById<TextView>(R.id.textPlayDirectly)
        val textCreateAccount = findViewById<TextView>(R.id.textCreateAccount)
        val cardUnder6 = findViewById<FrameLayout>(R.id.cardUnder6)
        val cardAbove6 = findViewById<FrameLayout>(R.id.cardAbove6)
        val starsUnder6 = findViewById<LottieAnimationView>(R.id.starEffectUnder6)
        val starsAbove6 = findViewById<LottieAnimationView>(R.id.starEffectAbove6)

        textPlayDirectly.setTextColor(Color.parseColor("#2196F3"))
        textCreateAccount.setTextColor(Color.parseColor("#2196F3"))

        fun applyMovingGradient(textView: TextView): Runnable {
            val paint = textView.paint
            val width = paint.measureText(textView.text.toString())
            val gradient = LinearGradient(
                0f, 0f, width, textView.textSize,
                intArrayOf(Color.parseColor("#FFD700"), Color.parseColor("#FFA500"), Color.parseColor("#FFD700")),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            val matrix = Matrix()
            var translateX = 0f

            return object : Runnable {
                override fun run() {
                    translateX += width / 20
                    if (translateX > 2 * width) translateX = -width
                    matrix.setTranslate(translateX, 0f)
                    gradient.setLocalMatrix(matrix)
                    textView.invalidate()
                    textView.postDelayed(this, 50)
                }
            }
        }

        var playDirectlyGradientRunnable: Runnable? = null
        var createAccountGradientRunnable: Runnable? = null

        textPlayDirectly.setOnTouchListener { _, event ->
            starsUnder6.visibility = View.VISIBLE
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    playDirectlyGradientRunnable = applyMovingGradient(textPlayDirectly)
                    textPlayDirectly.post(playDirectlyGradientRunnable!!)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    playDirectlyGradientRunnable?.let { textPlayDirectly.removeCallbacks(it) }
                    textPlayDirectly.paint.shader = null
                    textPlayDirectly.setTextColor(Color.parseColor("#2196F3"))
                    if (event.action == MotionEvent.ACTION_UP) {
                        starsUnder6.setAnimation(R.raw.stars)
                        starsUnder6.playAnimation()
                        textPlayDirectly.postDelayed({
                            starsUnder6.visibility = View.INVISIBLE
                            saveUserTypeAndNavigate("under6", SelectCharacterActivity::class.java)
                        }, 1500)
                    }
                }
            }
            true
        }

        textCreateAccount.setOnTouchListener { _, event ->
            starsAbove6.visibility = View.VISIBLE
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    createAccountGradientRunnable = applyMovingGradient(textCreateAccount)
                    textCreateAccount.post(createAccountGradientRunnable!!)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    createAccountGradientRunnable?.let { textCreateAccount.removeCallbacks(it) }
                    textCreateAccount.paint.shader = null
                    textCreateAccount.setTextColor(Color.parseColor("#2196F3"))
                    if (event.action == MotionEvent.ACTION_UP) {
                        starsAbove6.setAnimation(R.raw.stars)
                        starsAbove6.playAnimation()
                        textCreateAccount.postDelayed({
                            starsAbove6.visibility = View.INVISIBLE
                            saveUserTypeAndNavigate("above6", CreateAccountActivity::class.java)
                        }, 1500)
                    }
                }
            }
            true
        }

        cardUnder6.setOnClickListener {
            starsUnder6.visibility = View.VISIBLE
            starsUnder6.setAnimation(R.raw.stars)
            starsUnder6.playAnimation()
            Log.d("SelectUserType", "Card Under 6 Clicked")

            starsUnder6.removeAllAnimatorListeners()
            starsUnder6.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    starsUnder6.visibility = View.INVISIBLE
                    saveUserTypeAndNavigate("under6", SelectCharacterActivity::class.java)
                }
            })
        }

        // كارد فوق 6 سنوات
        cardAbove6.setOnClickListener {
            starsAbove6.visibility = View.VISIBLE
            starsAbove6.setAnimation(R.raw.stars)
            starsAbove6.playAnimation()
            Log.d("SelectUserType", "Card Above 6 Clicked")

            starsAbove6.removeAllAnimatorListeners()
            starsAbove6.addAnimatorListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    starsAbove6.visibility = View.INVISIBLE
                    saveUserTypeAndNavigate("above6", CreateAccountActivity::class.java)
                }
            })
        }
    }

    private fun saveUserTypeAndNavigate(userType: String, destination: Class<*>) {
        val prefs = getSharedPreferences("user_prefs", MODE_PRIVATE)
        prefs.edit()
            .putString("user_type", userType)
            .putBoolean("is_first_launch", false)
            .apply()
        Log.d("esraa", "Saved user_type: $userType")
        startActivity(Intent(this, destination))
        finish()
    }


}
