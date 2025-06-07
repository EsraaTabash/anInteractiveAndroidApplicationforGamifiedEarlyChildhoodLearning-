package com.example.final3

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var editImageIcon: ImageButton
    private lateinit var nameTextView: TextView
    private lateinit var editNameIcon: ImageButton
    private lateinit var nameEditText: EditText
    private lateinit var saveBtn: LinearLayout

    private var selectedImageResId: Int = R.drawable.ch1 // صورة افتراضية

    private val characterImages = intArrayOf(
        R.drawable.ch1, R.drawable.ch2, R.drawable.ch3,
        R.drawable.ch4, R.drawable.ch5, R.drawable.ch6
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.hide()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        profileImage = findViewById(R.id.edit_profile_image)
        editImageIcon = findViewById(R.id.edit_icon)
        nameTextView = findViewById(R.id.edit_name_text)
        editNameIcon = findViewById(R.id.edit_name_icon)
        nameEditText = findViewById(R.id.edit_name_input)
        saveBtn = findViewById(R.id.save_button)

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        val userName = prefs.getString("user_name", "")
        val userCharacter = prefs.getInt("user_character", R.drawable.ch1)

        // عرض الصورة والاسم من SharedPreferences
        profileImage.setImageResource(userCharacter)
        selectedImageResId = userCharacter

        nameTextView.text = userName ?: "اسم الطفل"
        nameEditText.visibility = View.GONE
        nameTextView.visibility = View.VISIBLE
        editNameIcon.visibility = View.VISIBLE

        // عند الضغط على أيقونة تعديل الصورة
        editImageIcon.setOnClickListener {
            showCharacterSelectionBottomSheet()
        }

        // عند الضغط على أيقونة تعديل الاسم
        editNameIcon.setOnClickListener {
            nameTextView.visibility = View.GONE
            editNameIcon.visibility = View.GONE
            nameEditText.visibility = View.VISIBLE
            nameEditText.setText("")
            nameEditText.hint = "الرجاء كتابة اسمك هنا"
            nameEditText.requestFocus()
        }

        // حفظ التعديلات
        saveBtn.setOnClickListener {
            val newName = if (nameEditText.visibility == View.VISIBLE) {
                nameEditText.text.toString().trim()
            } else {
                nameTextView.text.toString()
            }

            if (newName.isBlank()) {
                Toast.makeText(this, "يرجى إدخال الاسم", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // حفظ في SharedPreferences
            val editor = prefs.edit()
            editor.putString("user_name", newName)
            editor.putInt("user_character", selectedImageResId)
            editor.apply()

            // حفظ في Firebase Firestore
            val db = FirebaseFirestore.getInstance()
            val userId = "user_id_example"  // TODO: استبدل بمعرف المستخدم الحقيقي
            val userData = hashMapOf(
                "name" to newName,
                "character" to selectedImageResId
            )

            db.collection("users").document(userId)
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "تم حفظ التغييرات بنجاح", Toast.LENGTH_SHORT).show()

                    // الانتقال إلى شاشة البروفايل بعد الحفظ
                    val intent = Intent(this, ProfileActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // عرض BottomSheet لاختيار شخصية جديدة
    private fun showCharacterSelectionBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_character_grid, null)
        val gridView = view.findViewById<GridView>(R.id.character_grid)

        val adapter = CharactersAdapter(this, characterImages, selectedImageResId) { selectedResId ->
            selectedImageResId = selectedResId
            profileImage.setImageResource(selectedImageResId)
            dialog.dismiss()
        }

        gridView.adapter = adapter
        dialog.setContentView(view)
        dialog.show()
    }
}
