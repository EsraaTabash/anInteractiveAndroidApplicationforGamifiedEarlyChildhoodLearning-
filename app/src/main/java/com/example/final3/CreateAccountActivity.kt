package com.example.final3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.airbnb.lottie.LottieAnimationView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

open class CreateAccountActivity : AppCompatActivity() {

    // UI Elements
    private lateinit var rocket: LottieAnimationView
    private lateinit var rocket2: LottieAnimationView
    private lateinit var loadingOverlay: FrameLayout
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var createAccountBtn: ConstraintLayout
    private lateinit var emailFieldLayout: TextInputLayout
    private lateinit var passwordFieldLayout: TextInputLayout
    private lateinit var confirmPasswordFieldLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var confirmPasswordField: TextInputEditText

    // Firebase
    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // Bind views
        bindViews()

        // Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Clear error when user types
        setupErrorClearing()

        animateRocketIn()

        createAccountBtn.setOnClickListener {
            hideKeyboard()
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            // استخدام validateFields مع المعاملات
            if (validateFields(email, password, confirmPassword)) {
                createAccount(email, password)
            }
        }

        findViewById<TextView>(R.id.textView2).setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
    }

    private fun bindViews() {
        emailFieldLayout = findViewById(R.id.create_emailFieldLayout)
        passwordFieldLayout = findViewById(R.id.create_passwordFieldLayout)
        confirmPasswordFieldLayout = findViewById(R.id.create_confirmPasswordFieldLayout)
        emailField = findViewById(R.id.create_emailInput)
        passwordField = findViewById(R.id.create_passwordInput)
        confirmPasswordField = findViewById(R.id.create_confirmPasswordInput)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        rocket = findViewById(R.id.rocket)
        rocket2 = findViewById(R.id.loadingRocket)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        mainLayout = findViewById(R.id.main)
    }

    private fun setupErrorClearing() {
        emailField.addTextChangedListener(clearErrorOnTyping(emailFieldLayout))
        passwordField.addTextChangedListener(clearErrorOnTyping(passwordFieldLayout))
        confirmPasswordField.addTextChangedListener(clearErrorOnTyping(confirmPasswordFieldLayout))
    }

    private fun createAccount(email: String, password: String) {
        showLoading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveEmailToPrefs(email)
                    Toast.makeText(this, "تم إنشاء الحساب بنجاح", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, WriteNameActivity::class.java))
                    finish()
                } else {
                    handleCreateAccountErrors(task.exception)
                    hideLoading()
                }
            }
    }

    private fun handleCreateAccountErrors(exception: Exception?) {
        emailFieldLayout.error = null
        passwordFieldLayout.error = null
        confirmPasswordFieldLayout.error = null

        when (exception) {
            is FirebaseAuthWeakPasswordException -> {
                passwordFieldLayout.error = "كلمة المرور ضعيفة جدًا. استخدم 6 أحرف على الأقل"
                passwordField.requestFocus()
            }
            is FirebaseAuthUserCollisionException -> {
                emailFieldLayout.error = "البريد الإلكتروني مستخدم بالفعل"
                emailField.requestFocus()
            }
            is FirebaseAuthInvalidCredentialsException -> {
                emailFieldLayout.error = "صيغة البريد غير صحيحة"
                emailField.requestFocus()
            }
            is FirebaseNetworkException -> {
                Toast.makeText(this, "لا يوجد اتصال بالإنترنت", Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(this, "حدث خطأ: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // دالة validateFields مع المعاملات
    fun validateFields(email: String, password: String, confirmPassword: String): Boolean {
        var valid = true

        if (email.isEmpty()) {
            emailFieldLayout.error = "البريد الإلكتروني مطلوب"
            valid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailFieldLayout.error = "صيغة البريد الإلكتروني غير صحيحة"
            valid = false
        }

        if (password.isEmpty()) {
            passwordFieldLayout.error = "كلمة المرور مطلوبة"
            valid = false
        } else if (password.length < 6) {
            passwordFieldLayout.error = "كلمة المرور قصيرة جدًا"
            valid = false
        }

        if (confirmPassword.isEmpty()) {
            confirmPasswordFieldLayout.error = "تأكيد كلمة المرور مطلوب"
            valid = false
        } else if (password != confirmPassword) {
            confirmPasswordFieldLayout.error = "كلمات المرور غير متطابقة"
            valid = false
        }

        return valid
    }

    private fun saveEmailToPrefs(email: String) {
        getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            .edit().putString("user_email", email).apply()
    }

    private fun showLoading() {
        createAccountBtn.isClickable = false
        loadingOverlay.apply {
            visibility = View.VISIBLE
            isClickable = true
        }
        animateRocket2In()
    }

    private fun hideLoading() {
        createAccountBtn.isClickable = true
        loadingOverlay.visibility = View.GONE
        rocket2.visibility = View.GONE
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
        val moveIn = ObjectAnimator.ofFloat(rocket2, "translationX", -rocket2.width.toFloat() - 100f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(rocket2, "alpha", 0f, 1f)
        AnimatorSet().apply {
            playTogether(moveIn, fadeIn)
            duration = 500
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun clearErrorOnTyping(layout: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layout.error = null
            }
            override fun afterTextChanged(s: Editable?) {}
        }
    }
}
