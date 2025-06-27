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
    import android.view.inputmethod.InputMethodManager
    import android.view.animation.DecelerateInterpolator
    import android.view.animation.LinearInterpolator
    import android.widget.*
    import androidx.appcompat.app.AppCompatActivity
    import androidx.constraintlayout.widget.ConstraintLayout
    import androidx.core.content.res.ResourcesCompat
    import com.airbnb.lottie.LottieAnimationView
    import com.google.android.material.textfield.TextInputEditText
    import com.google.android.material.textfield.TextInputLayout
    import com.google.firebase.FirebaseNetworkException
    import com.google.firebase.auth.*
    import com.google.firebase.firestore.FirebaseFirestore

    class SigninActivity : AppCompatActivity() {

        // UI Elements
        private lateinit var rocket: LottieAnimationView
        private lateinit var rocket2: LottieAnimationView
        private lateinit var loadingOverlaylayout: FrameLayout
        private lateinit var mainLayout: ConstraintLayout
        private lateinit var signinBtn: ConstraintLayout
        private lateinit var createAccountText: TextView

        private lateinit var emailFieldLayout: TextInputLayout
        private lateinit var passwordFieldLayout: TextInputLayout
        private lateinit var emailField: TextInputEditText
        private lateinit var passwordField: TextInputEditText
        private lateinit var errorFont: Typeface

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_signin)
            supportActionBar?.hide()
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            // Bind Views
            bindViews()
            errorFont = ResourcesCompat.getFont(this, R.font.tajawal_regular)!!

            animateRocketIn()

            // Error Reset
            emailField.addTextChangedListener(clearErrorOnTyping(emailFieldLayout))
            passwordField.addTextChangedListener(clearErrorOnTyping(passwordFieldLayout))

            signinBtn.setOnClickListener {
                hideKeyboard()
                if (validateFields()) {
                    val email = emailField.text.toString().trim()
                    val password = passwordField.text.toString().trim()
                    signInUser(email, password)
                }
            }

            createAccountText.setOnClickListener {
                startActivity(Intent(this, CreateAccountActivity::class.java))
            }
        }

        private fun bindViews() {
            emailFieldLayout = findViewById(R.id.signin_emailFieldLayout)
            emailField = findViewById(R.id.signin_emailInput)
            passwordFieldLayout = findViewById(R.id.signin_passwordFieldLayout)
            passwordField = findViewById(R.id.signin_passwordInput)
            signinBtn = findViewById(R.id.signin_signinBtn)
            createAccountText = findViewById(R.id.signin_createAccountText)
            rocket = findViewById(R.id.signin_rocket)
            rocket2 = findViewById(R.id.signin_loadingRocket)
            loadingOverlaylayout = findViewById(R.id.signin_loadingOverlaylayout)
            mainLayout = findViewById(R.id.signin_main)
        }

        private fun signInUser(email: String, password: String) {
            showLoading()

            val db = FirebaseFirestore.getInstance()
            db.collection("users").whereEqualTo("email", email).get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        emailFieldLayout.error = "البريد الإلكتروني غير مسجل"
                        emailField.requestFocus()
                        hideLoading()
                    } else {
                        FirebaseAuth.getInstance()
                            .signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    fetchUserData()
                                } else {
                                    handleLoginErrors(task.exception)
                                }
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "حدث خطأ في الاتصال أو قاعدة البيانات.", Toast.LENGTH_SHORT).show()
                    hideLoading()
                }
        }

        private fun fetchUserData() {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
            val db = FirebaseFirestore.getInstance()

            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE).edit()
                    prefs.putString("user_name", document.getString("name") ?: "")
                    prefs.putInt("character_id", document.getLong("character_id")?.toInt() ?: 1)
                    prefs.putString("user_email", document.getString("email") ?: "")
                    prefs.putString("user_type", document.getString("user_type") ?: "above6")
                    prefs.apply()

                    goToHome()
                }
                .addOnFailureListener {
                    goToHome()
                }
        }

        private fun handleLoginErrors(exception: Exception?) {
            emailFieldLayout.error = null
            passwordFieldLayout.error = null

            when (exception) {
                is FirebaseAuthInvalidCredentialsException -> {
                    passwordFieldLayout.error = "كلمة المرور غير صحيحة"
                    passwordField.requestFocus()
                }
                is FirebaseNetworkException -> {
                    Toast.makeText(this, "تعذر الاتصال بالإنترنت. حاول مرة أخرى.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "حدث خطأ: ${exception?.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            }
            hideLoading()
        }

        private fun validateFields(): Boolean {
            var isValid = true
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString()

            if (email.isEmpty()) {
                emailFieldLayout.error = "الرجاء إدخال البريد الإلكتروني"
                emailField.typeface = errorFont
                emailField.requestFocus()
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailFieldLayout.error = "صيغة البريد الإلكتروني غير صحيحة"
                emailField.typeface = errorFont
                emailField.requestFocus()
                isValid = false
            }

            if (password.isEmpty()) {
                passwordFieldLayout.error = "الرجاء إدخال كلمة المرور"
                passwordField.typeface = errorFont
                passwordField.requestFocus()
                isValid = false
            }

            return isValid
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

        private fun showLoading() {
            signinBtn.isClickable = false
            loadingOverlaylayout.visibility = View.VISIBLE
            loadingOverlaylayout.isClickable = true
            animateRocket2In()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val blurEffect = RenderEffect.createBlurEffect(20f, 20f, Shader.TileMode.CLAMP)
                mainLayout.setRenderEffect(blurEffect)
            }
        }

        private fun hideLoading() {
            signinBtn.isClickable = true
            loadingOverlaylayout.visibility = View.GONE
            rocket2.visibility = View.GONE

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                loadingOverlaylayout.setRenderEffect(null)
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

        private fun goToHome() {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        override fun onPause() {
            super.onPause()
            GameMusicService.pauseMusic()
        }

        override fun onResume() {
            super.onResume()
            GameMusicService.resumeMusic()
        }
    }
