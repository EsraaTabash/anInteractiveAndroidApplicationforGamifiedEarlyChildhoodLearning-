package com.example.final3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.RenderEffect
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
class SigninActivity : AppCompatActivity() {
    private lateinit var rocket2: LottieAnimationView
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var loadingOverlaylayout: FrameLayout

    // المتغيرات اللي طلبتها تتحول لـ lateinit var هنا:
    private lateinit var emailFieldLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordFieldLayout: TextInputLayout
    private lateinit var passwordField: TextInputEditText
    private lateinit var signinBtn: ConstraintLayout
    private lateinit var createAccountText: TextView
    private lateinit var rocket: LottieAnimationView
    private lateinit var errorFont: Typeface

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // هنا تهيئة المتغيرات
        emailFieldLayout = findViewById(R.id.signin_emailFieldLayout)
        emailField = findViewById(R.id.signin_emailInput)
        passwordFieldLayout = findViewById(R.id.signin_passwordFieldLayout)
        passwordField = findViewById(R.id.signin_passwordInput)
        signinBtn = findViewById(R.id.signin_signinBtn)
        createAccountText = findViewById(R.id.signin_createAccountText)
        rocket = findViewById(R.id.signin_rocket)
        rocket2 = findViewById(R.id.signin_loadingRocket)
        mainLayout = findViewById(R.id.signin_main)
        loadingOverlaylayout = findViewById(R.id.signin_loadingOverlaylayout)

        errorFont = ResourcesCompat.getFont(this, R.font.tajawal_regular)!!

        // تحريك الصاروخ
        rocket.addLottieOnCompositionLoadedListener {
            rocket.translationX = -1000f
            rocket.animate()
                .translationX(0f)
                .setDuration(1200)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        // إزالة الأخطاء عند الكتابة
        emailField.addTextChangedListener(clearErrorOnTyping(emailFieldLayout))
        passwordField.addTextChangedListener(clearErrorOnTyping(passwordFieldLayout))

        signinBtn.setOnClickListener {
            hideKeyboard()
            if (validateFields(
                    emailFieldLayout, emailField,
                    passwordFieldLayout, passwordField,
                    errorFont
                )
            ) {
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()
                setContentBlur()
                signinBtn.isClickable = false
                loadingOverlaylayout.visibility = View.VISIBLE
                loadingOverlaylayout.isClickable = true
                animateRocket2In()

                FirebaseAuth.getInstance()
                    .signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = FirebaseAuth.getInstance().currentUser
                            Toast.makeText(this, "تم تسجيل الدخول: ${user?.email}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, HomeActivity::class.java))
                            finish()
                        } else {
                            // هنا إضافة تخصيص الأخطاء
                            emailFieldLayout.error = null
                            passwordFieldLayout.error = null

                            val exception = task.exception
                            when (exception) {
                                is com.google.firebase.auth.FirebaseAuthInvalidUserException -> {
                                    emailFieldLayout.error = "المستخدم غير موجود"
                                    emailField.requestFocus()
                                }
                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                    passwordFieldLayout.error = "كلمة المرور غير صحيحة"
                                    passwordField.requestFocus()
                                }
                                is com.google.firebase.FirebaseNetworkException -> {
                                    Toast.makeText(this, "تعذر الاتصال بالإنترنت. حاول مرة أخرى.", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(this, "فشل تسجيل الدخول: ${exception?.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            resetUIAfterLoading()
                        }
                    }
            }
        }

        createAccountText.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }
    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    private fun validateFields(
        emailLayout: TextInputLayout,
        emailField: TextInputEditText,
        passwordLayout: TextInputLayout,
        passwordField: TextInputEditText,
        errorFont: Typeface?
    ): Boolean {
        val email = emailField.text.toString().trim()
        val password = passwordField.text.toString().trim()

        val emailRegex = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+".toRegex()

        return when {
            email.isEmpty() -> {
                emailLayout.error = "الرجاء إدخال البريد الإلكتروني"
                emailField.typeface = errorFont
                emailField.requestFocus()
                false
            }
            !email.matches(emailRegex) -> {
                emailLayout.error = "صيغة البريد الإلكتروني غير صحيحة"
                emailField.typeface = errorFont
                emailField.requestFocus()
                false
            }
            password.isEmpty() -> {
                passwordLayout.error = "الرجاء إدخال كلمة المرور"
                passwordField.typeface = errorFont
                passwordField.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun clearErrorOnTyping(layout: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layout.isErrorEnabled = false
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun setContentBlur() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
            mainLayout.setRenderEffect(blurEffect)
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun resetUIAfterLoading() {
        signinBtn.isClickable = true
        loadingOverlaylayout.visibility = View.GONE
        loadingOverlaylayout.setRenderEffect(null)

        rocket2.animate()
            .translationX(-rocket2.width.toFloat() - 100f)
            .setDuration(500)
            .withEndAction {
                rocket2.visibility = View.GONE
            }
            .start()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            mainLayout.setRenderEffect(null)
        }
    }

    private fun animateRocketIn() {
        rocket.post {
            rocket.addLottieOnCompositionLoadedListener {
                rocket.translationX = -1000f
                rocket.animate()
                    .translationX(0f)
                    .setDuration(1200)
                    .setInterpolator(DecelerateInterpolator())
                    .start()
            }
        }
    }

    private fun animateRocket2In() {
        rocket2.visibility = View.VISIBLE
        rocket2.alpha = 0f

        val translateIn =
            ObjectAnimator.ofFloat(rocket2, "translationX", -rocket2.width.toFloat() - 100f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(rocket2, "alpha", 0f, 1f)

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(translateIn, fadeIn)
        animatorSet.duration = 500
        animatorSet.interpolator = LinearInterpolator()
        animatorSet.start()
    }
}
