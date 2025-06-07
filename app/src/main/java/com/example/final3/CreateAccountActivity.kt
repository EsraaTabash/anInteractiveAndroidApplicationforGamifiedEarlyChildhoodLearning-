package com.example.final3

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Shader
import android.graphics.RenderEffect
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.airbnb.lottie.LottieAnimationView
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class CreateAccountActivity : AppCompatActivity() {

    private lateinit var rocket: LottieAnimationView
    private lateinit var rocket2: LottieAnimationView
    private lateinit var loadingOverlaylayout: FrameLayout

    private lateinit var createAccountBtn: ConstraintLayout
    private lateinit var orDividerGroup: ConstraintLayout

    //    private lateinit var socialContainer: LinearLayout
    private lateinit var emailFieldLayout: TextInputLayout
    private lateinit var passwordFieldLayout: TextInputLayout
    private lateinit var confirmPasswordFieldLayout: TextInputLayout
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var confirmPasswordField: TextInputEditText
    private lateinit var mainLayout: ConstraintLayout
    private lateinit var googleIcon: FrameLayout

    private val RC_SIGN_IN = 9001
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    @SuppressLint("ServiceCast")
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // ربط عناصر الواجهة
        emailFieldLayout = findViewById(R.id.create_emailFieldLayout)
        emailField = findViewById(R.id.create_emailInput)
        passwordFieldLayout = findViewById(R.id.create_passwordFieldLayout)
        passwordField = findViewById(R.id.create_passwordInput)
        confirmPasswordFieldLayout = findViewById(R.id.create_confirmPasswordFieldLayout)
        confirmPasswordField = findViewById(R.id.create_confirmPasswordInput)
        createAccountBtn = findViewById(R.id.createAccountBtn)
        orDividerGroup = findViewById(R.id.orDividerGroup)
        //socialContainer = findViewById(R.id.socialContainer)
        loadingOverlaylayout = findViewById(R.id.loadingOverlay)
        rocket = findViewById(R.id.rocket)
        rocket2 = findViewById(R.id.loadingRocket)
        mainLayout = findViewById(R.id.main)
        //googleIcon = findViewById(R.id.google)

        // تهيئة FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // إزالة رسالة الخطأ عند الكتابة
        emailField.addTextChangedListener(clearErrorOnTyping(emailFieldLayout))
        passwordField.addTextChangedListener(clearErrorOnTyping(passwordFieldLayout))
        confirmPasswordField.addTextChangedListener(clearErrorOnTyping(confirmPasswordFieldLayout))

        animateRocketIn()

        createAccountBtn.setOnClickListener {
            hideKeyboard()
            if (validateFields(
                    emailFieldLayout,
                    emailField,
                    passwordFieldLayout,
                    passwordField,
                    confirmPasswordFieldLayout,
                    confirmPasswordField,
                    ResourcesCompat.getFont(this, R.font.tajawal_regular)
                )
            ) {
                val email = emailField.text.toString().trim()
                val password = passwordField.text.toString().trim()

                setContentBlur()
                createAccountBtn.isClickable = false
                loadingOverlaylayout.visibility = View.VISIBLE
                loadingOverlaylayout.isClickable = true

                animateRocket2In()

                // إنشاء حساب Firebase
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            Toast.makeText(
                                this,
                                "تم إنشاء الحساب: ${user?.email}",
                                Toast.LENGTH_SHORT
                            ).show()

                           //  حفظ البريد في SharedPreferences
                            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                            prefs.edit().putString("user_email", email).apply()

                            // الانتقال إلى الشاشة التالية
                            val intent = Intent(this, WriteNameActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            emailFieldLayout.error = null
                            passwordFieldLayout.error = null
                            confirmPasswordFieldLayout.error = null

                            val exception = task.exception
                            when (exception) {
                                is com.google.firebase.auth.FirebaseAuthWeakPasswordException -> {
                                    passwordFieldLayout.error =
                                        "كلمة المرور ضعيفة جدًا. حاول استخدام 6 أحرف على الأقل"
                                    passwordField.requestFocus()
                                }

                                is com.google.firebase.auth.FirebaseAuthUserCollisionException -> {
                                    emailFieldLayout.error = "البريد الإلكتروني موجود بالفعل"
                                    emailField.requestFocus()
                                }

                                is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException -> {
                                    emailFieldLayout.error = "صيغة البريد الإلكتروني غير صحيحة"
                                    emailField.requestFocus()
                                }

                                is com.google.firebase.FirebaseNetworkException -> {
                                    Toast.makeText(
                                        this,
                                        "تعذر الاتصال بالإنترنت. حاول مرة أخرى.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                else -> {
                                    Toast.makeText(
                                        this,
                                        "فشل في إنشاء الحساب: ${exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                            resetUIAfterLoading()
                        }
                    }
            }
        }

        // رابط لتسجيل الدخول إذا كان لدى المستخدم حساب
        findViewById<TextView>(R.id.textView2).setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }

        // تهيئة Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // تأكد من تعيين هذا في res/values/strings.xml
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // زر تسجيل الدخول بجوجل
//        googleIcon.setOnClickListener {
//            signInWithGoogle()
//        }
    }

    private fun hideKeyboard() {
        val imm =
            getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        val view = currentFocus ?: View(this)
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(
                    this,
                    "فشل تسجيل الدخول بواسطة جوجل: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val account = GoogleSignIn.getLastSignedInAccount(this)
                    val email = account?.email
                    val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    prefs.edit().putString("user_email", email).apply()

                    Toast.makeText(this, "تم تسجيل الدخول: ${user?.email}", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(this, WriteNameActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "فشل تسجيل الدخول بواسطة جوجل", Toast.LENGTH_SHORT).show()
                }
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
        createAccountBtn.isClickable = true
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

    private fun clearErrorOnTyping(layout: TextInputLayout): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(
                s: CharSequence?, start: Int, before: Int, count:
                Int
            ) {
                layout.error = null
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun validateFields(
        emailLayout: TextInputLayout,
        email: TextInputEditText,
        passwordLayout: TextInputLayout,
        password: TextInputEditText,
        confirmPasswordLayout: TextInputLayout,
        confirmPassword: TextInputEditText,
        font: android.graphics.Typeface?
    ): Boolean {
        var isValid = true
        val emailText = email.text.toString().trim()
        val passwordText = password.text.toString()
        val confirmPasswordText = confirmPassword.text.toString()

        if (emailText.isEmpty()) {
            emailLayout.error = "البريد الإلكتروني مطلوب"
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
            emailLayout.error = "صيغة البريد الإلكتروني غير صحيحة"
            isValid = false
        }

        if (passwordText.isEmpty()) {
            passwordLayout.error = "كلمة المرور مطلوبة"
            isValid = false
        } else if (passwordText.length < 6) {
            passwordLayout.error = "كلمة المرور يجب أن تكون 6 أحرف على الأقل"
            isValid = false
        }

        if (confirmPasswordText.isEmpty()) {
            confirmPasswordLayout.error = "تأكيد كلمة المرور مطلوب"
            isValid = false
        } else if (passwordText != confirmPasswordText) {
            confirmPasswordLayout.error = "كلمات المرور غير متطابقة"
            isValid = false
        }

        return isValid
    }
}