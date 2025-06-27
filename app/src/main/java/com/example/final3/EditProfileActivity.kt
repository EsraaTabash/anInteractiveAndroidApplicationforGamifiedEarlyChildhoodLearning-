    package com.example.final3

    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.view.View
    import android.view.inputmethod.InputMethodManager
    import android.widget.*
    import androidx.appcompat.app.AppCompatActivity
    import com.google.android.material.bottomsheet.BottomSheetDialog
    import com.google.firebase.auth.FirebaseAuth
    import com.google.firebase.firestore.FirebaseFirestore
    import com.google.firebase.firestore.SetOptions

    class EditProfileActivity : AppCompatActivity() {

        private lateinit var profileImage: ImageView
        private lateinit var editImageIcon: ImageButton
        private lateinit var nameTextView: TextView
        private lateinit var editNameIcon: ImageButton
        private lateinit var nameEditText: EditText
        private lateinit var saveBtn: LinearLayout
        private lateinit var backBtn: LinearLayout
        private var selectedImageId: Int = 1

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_edit_profile)
            supportActionBar?.hide()
            requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

            bindViews()
            loadUserData()
            setupListeners()
        }

        private fun bindViews() {
            profileImage = findViewById(R.id.edit_profile_image)
            editImageIcon = findViewById(R.id.edit_icon)
            nameTextView = findViewById(R.id.edit_name_text)
            editNameIcon = findViewById(R.id.edit_name_icon)
            nameEditText = findViewById(R.id.edit_name_input)
            saveBtn = findViewById(R.id.save_button)
            backBtn = findViewById(R.id.backBtn)
        }

        private fun loadUserData() {
            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            val name = prefs.getString("user_name", "") ?: ""
            val characterId = prefs.getInt("character_id", 1)
            selectedImageId = characterId

            profileImage.setImageResource(CharacterUtils.getDrawableByCharacterId(characterId))
            nameTextView.text = if (name.isNotBlank()) name else CharacterUtils.getNameByCharacterId(characterId)

            nameEditText.visibility = View.GONE
            nameTextView.visibility = View.VISIBLE
            editNameIcon.visibility = View.VISIBLE
        }

        private fun setupListeners() {
            editImageIcon.setOnClickListener { showCharacterSelectionBottomSheet() }

            editNameIcon.setOnClickListener {
                nameTextView.visibility = View.GONE
                editNameIcon.visibility = View.GONE
                nameEditText.visibility = View.VISIBLE
                nameEditText.setText("")
                nameEditText.hint = "الرجاء كتابة اسمك هنا"
                nameEditText.requestFocus()

                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(nameEditText, InputMethodManager.SHOW_IMPLICIT)
            }

            backBtn.setOnClickListener { finish() }

            saveBtn.setOnClickListener { saveProfileChanges() }
        }

        private fun saveProfileChanges() {
            val isEditing = nameEditText.visibility == View.VISIBLE
            val newName = if (isEditing) nameEditText.text.toString().trim() else nameTextView.text.toString().trim()

            if (newName.isBlank()) {
                if (isEditing) {
                    nameEditText.error = "يرجى إدخال الاسم"
                    nameEditText.requestFocus()
                } else {
                    Toast.makeText(this, "يرجى إدخال الاسم", Toast.LENGTH_SHORT).show()
                }
                return
            }

            val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("user_name", newName)
                .putInt("character_id", selectedImageId)
                .apply()

            val userType = prefs.getString("user_type", "under6") ?: "under6"

            if (userType == "under6") {
                navigateToProfile()
            } else {
                if (!isNetworkAvailable(this)) {
                    Toast.makeText(this, "لا يوجد اتصال بالإنترنت", Toast.LENGTH_LONG).show()
                    return
                }

                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val userData = mapOf("name" to newName, "character_id" to selectedImageId)

                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    db.collection("users").document(userId)
                        .update(userData)
                        .addOnSuccessListener { navigateToProfile() }
                        .addOnFailureListener {
                            db.collection("users").document(userId)
                                .set(userData, SetOptions.merge())
                                .addOnSuccessListener { navigateToProfile() }
                                .addOnFailureListener {
                                    Toast.makeText(this, "تعذر الحفظ! حاول لاحقًا.", Toast.LENGTH_LONG).show()
                                }
                        }
                } else {
                    Toast.makeText(this, "لم يتم العثور على الحساب", Toast.LENGTH_SHORT).show()
                }
            }
        }

        private fun navigateToProfile() {
            Toast.makeText(this, "تم حفظ التغييرات بنجاح", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, ProfileActivity::class.java))
            finish()
        }

        private fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            return connectivityManager.activeNetworkInfo?.isConnected == true
        }

        private fun showCharacterSelectionBottomSheet() {
            val dialog = BottomSheetDialog(this)
            val view = layoutInflater.inflate(R.layout.bottom_sheet_character_grid, null)
            val gridView = view.findViewById<GridView>(R.id.character_grid)

            val adapter = CharactersAdapter(this, selectedImageId) { selectedId ->
                selectedImageId = selectedId
                profileImage.setImageResource(CharacterUtils.getDrawableByCharacterId(selectedImageId))
                dialog.dismiss()
            }

            gridView.adapter = adapter
            dialog.setContentView(view)
            dialog.show()
        }
    }
