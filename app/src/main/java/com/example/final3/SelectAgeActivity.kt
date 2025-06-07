package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.shawnlin.numberpicker.NumberPicker

class SelectAgeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_age)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val numberPicker = findViewById<NumberPicker>(R.id.agePicker)
       // val nameFieldLayout = findViewById<TextInputLayout>(R.id.select_nameFieldLayout)
       // val nameField = findViewById<TextInputEditText>(R.id.select_nameInput)
        val btnStart = findViewById<ImageView>(R.id.btnStart)

        val typeface = ResourcesCompat.getFont(this, R.font.zain_regular)
        val errorFont = ResourcesCompat.getFont(this, R.font.tajawal_regular)

        val ageRanges = arrayOf("3 - 4 سنـوات", "5 - 7 سنـوات", "8 - 10 سنـوات")
        numberPicker.displayedValues = ageRanges
        numberPicker.minValue = 0
        numberPicker.maxValue = ageRanges.size - 1
        numberPicker.wrapSelectorWheel = false
        numberPicker.value = 0
        numberPicker.setSelectedTypeface(typeface)
        numberPicker.setTypeface(typeface)

        //nameField.typeface = typeface
        //nameField.addTextChangedListener(clearErrorOnTyping(nameFieldLayout))

        //nameField.imeOptions = EditorInfo.IME_ACTION_DONE
//        nameField.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                handleValidation(nameFieldLayout, nameField, errorFont)
//                true
//            } else false
//        }

        btnStart.setOnClickListener {
           // handleValidation(nameFieldLayout, nameField, errorFont)
            val selectedAgeRange = ageRanges[numberPicker.value]
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("user_age_range", selectedAgeRange).apply()
            val intent = Intent(this, WriteNameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleValidation(
        layout: TextInputLayout,
        field: TextInputEditText,
        errorFont: Typeface?
    ) {
        val name = field.text.toString().trim()
        if (name.isEmpty()) {
            layout.error = "الرجاء إدخال الاسم"
            field.typeface = errorFont
            field.requestFocus()
        } else {
            layout.error = null
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
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
}
