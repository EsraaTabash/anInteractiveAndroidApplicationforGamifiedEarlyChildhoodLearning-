package com.example.final3

import android.animation.*
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout

class WelcomeActivity : AppCompatActivity() {
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
        setContentView(R.layout.activity_welcome)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val textView1: TextView = findViewById(R.id.textView1)
        val character1: ImageView = findViewById(R.id.character1)
        val firstStone: ImageView = findViewById(R.id.first_stone)
        val leftStone: ImageView = findViewById(R.id.left_stone)
        val rightStone: ImageView = findViewById(R.id.right_stone)
        val lastStone: ImageView = findViewById(R.id.last_stone)
        val moon: ImageView = findViewById(R.id.moon)

        // ابدأ بتصفير جميع العناصر
        character1.translationX = -800f
        character1.translationY = 800f
        character1.alpha = 1f
        textView1.alpha = 1f

        val stones = listOf(firstStone, leftStone, rightStone, lastStone)
        stones.forEach {
            it.translationY = 400f
            it.alpha = 0f
        }
        moon.translationY = -400f
        moon.alpha = 0f

        textView1.visibility = View.VISIBLE
        character1.visibility = View.VISIBLE
        stones.forEach { it.visibility = View.VISIBLE }
        moon.visibility = View.VISIBLE

        val charAnimSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(character1, "translationX", 0f),
                ObjectAnimator.ofFloat(character1, "translationY", 0f)
            )
            duration = 1000
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animateStonesIn(stones, moon, textView1, character1)
                }
            })
        }
        charAnimSet.start()
    }

    private fun animateStonesIn(
        stones: List<ImageView>,
        moon: ImageView,
        textView1: TextView,
        character1: ImageView
    ) {
        // 2. حركات الصخور بشكل متسلسل مع قليل من التأخير
        for ((i, stone) in stones.withIndex()) {
            stone.postDelayed({
                stone.animate()
                    .translationY(0f)
                    .alpha(1f)
                    .setDuration(700)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }, (i * 120).toLong())
        }

        // 3. القمر يظهر بعد الصخور بقليل
        moon.postDelayed({
            moon.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(700)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    // fadeout النص والشخصية ثم انتقل للمرحلة 2
                    fadeOutCharacter1AndText(textView1, character1)
                }
                .start()
        }, 600)
    }

    private fun fadeOutCharacter1AndText(textView1: TextView, character1: ImageView) {
        val fadeOutTextView1 = ObjectAnimator.ofFloat(textView1, "alpha", 1f, 0f)
        val fadeOutCharacter1 = ObjectAnimator.ofFloat(character1, "alpha", 1f, 0f)

        AnimatorSet().apply {
            playTogether(fadeOutTextView1, fadeOutCharacter1)
            duration = 400
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    textView1.visibility = View.GONE
                    character1.visibility = View.GONE
                    startStage2()
                }
            })
            start()
        }
    }

    private fun startStage2() {
        val textView2: TextView = findViewById(R.id.textView2)
        val character2: ImageView = findViewById(R.id.character2)
        val moon: ImageView = findViewById(R.id.moon)
        val firstStone: ImageView = findViewById(R.id.first_stone)
        val leftStone: ImageView = findViewById(R.id.left_stone)
        val rightStone: ImageView = findViewById(R.id.right_stone)
        val lastStone: ImageView = findViewById(R.id.last_stone)

        textView2.visibility = View.VISIBLE
        character2.visibility = View.VISIBLE
        moon.visibility = View.VISIBLE
        firstStone.visibility = View.VISIBLE
        leftStone.visibility = View.VISIBLE
        rightStone.visibility = View.VISIBLE
        lastStone.visibility = View.VISIBLE

        val moveTextView2X = ObjectAnimator.ofFloat(textView2, "translationX", 0f)
        val moveTextView2Y = ObjectAnimator.ofFloat(textView2, "translationY", -100f)

        val moveCharacter2X = ObjectAnimator.ofFloat(character2, "translationX", -60f)
        val moveCharacter2Y = ObjectAnimator.ofFloat(character2, "translationY", 0f)

        val moveMoonX = ObjectAnimator.ofFloat(moon, "translationX", -180f)
        val moveMoonY = ObjectAnimator.ofFloat(moon, "translationY", -1500f)

        val moveFirstStoneX = ObjectAnimator.ofFloat(firstStone, "translationX", 180f)
        val moveFirstStoneY = ObjectAnimator.ofFloat(firstStone, "translationY", 0f)

        val moveLeftStoneX = ObjectAnimator.ofFloat(leftStone, "translationX", 180f)
        val moveLeftStoneY = ObjectAnimator.ofFloat(leftStone, "translationY", 0f)

        val moveRightStoneX = ObjectAnimator.ofFloat(rightStone, "translationX", -150f)
        val moveRightStoneY = ObjectAnimator.ofFloat(rightStone, "translationY", 0f)

        val moveLastStoneX = ObjectAnimator.ofFloat(lastStone, "translationX", -200f)
        val moveLastStoneY = ObjectAnimator.ofFloat(lastStone, "translationY", 0f)

        AnimatorSet().apply {
            playTogether(
                moveTextView2X, moveTextView2Y,
                moveCharacter2X, moveCharacter2Y,
                moveMoonX, moveMoonY,
                moveFirstStoneX, moveFirstStoneY,
                moveLeftStoneX, moveLeftStoneY,
                moveRightStoneX, moveRightStoneY,
                moveLastStoneX, moveLastStoneY
            )
            duration = 1200
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    startStage3()
                }
            })
            start()
        }
    }

    private fun startStage3() {
        val character1: ImageView = findViewById(R.id.character1)
        val character2: ImageView = findViewById(R.id.character2)
        val textView2: TextView = findViewById(R.id.textView2)
        val moon: ImageView = findViewById(R.id.moon)
        val firstStone: ImageView = findViewById(R.id.first_stone)
        val leftStone: ImageView = findViewById(R.id.left_stone)
        val rightStone: ImageView = findViewById(R.id.right_stone)
        val lastStone: ImageView = findViewById(R.id.last_stone)

        character1.visibility = View.VISIBLE
        character1.alpha = 1f
        character1.bringToFront()

        val moveCharacter1X = ObjectAnimator.ofFloat(character1, "translationX", -300f).apply {
            duration = 1200
        }

        val moveCharacter1Y = ObjectAnimator.ofFloat(character1, "translationY", 1000f).apply {
            duration = 1200
        }

        val moveCharacter2X = ObjectAnimator.ofFloat(character2, "translationX", -80f)
        val moveCharacter2Y = ObjectAnimator.ofFloat(character2, "translationY", -120f)

        val moveTextView2X = ObjectAnimator.ofFloat(textView2, "translationX", 0f)
        val moveTextView2Y = ObjectAnimator.ofFloat(textView2, "translationY", 0f)

        val moveMoonX = ObjectAnimator.ofFloat(moon, "translationX", 300f)
        val moveMoonY = ObjectAnimator.ofFloat(moon, "translationY", -2500f)

        val moveFirstStoneX = ObjectAnimator.ofFloat(firstStone, "translationX", 0f)
        val moveFirstStoneY = ObjectAnimator.ofFloat(firstStone, "translationY", 150f)

        val moveLeftStoneX = ObjectAnimator.ofFloat(leftStone, "translationX", 0f)
        val moveLeftStoneY = ObjectAnimator.ofFloat(leftStone, "translationY", 150f)

        val moveRightStoneX = ObjectAnimator.ofFloat(rightStone, "translationX", 0f)
        val moveRightStoneY = ObjectAnimator.ofFloat(rightStone, "translationY", 0f)

        val moveLastStoneX = ObjectAnimator.ofFloat(lastStone, "translationX", 100f)
        val moveLastStoneY = ObjectAnimator.ofFloat(lastStone, "translationY", 0f)



        AnimatorSet().apply {
            playTogether(
                moveCharacter1X, moveCharacter1Y,
                moveCharacter2X, moveCharacter2Y,
                moveTextView2X, moveTextView2Y,
                moveMoonX, moveMoonY,
                moveFirstStoneX, moveFirstStoneY,
                moveLeftStoneX, moveLeftStoneY,
                moveRightStoneX, moveRightStoneY,
                moveLastStoneX, moveLastStoneY
            )
            duration = 1200
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val intent = Intent(this@WelcomeActivity, HelloActivity::class.java)
                   startActivity(intent)
                    finish()
                }
            })
            start()
        }
    }
}