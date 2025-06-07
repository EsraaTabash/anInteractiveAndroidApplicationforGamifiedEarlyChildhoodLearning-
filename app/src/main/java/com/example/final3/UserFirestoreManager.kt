package com.example.final3

import com.google.firebase.firestore.FirebaseFirestore

object UserFirestoreManager {

    fun saveUser(
        uid: String,
        email: String,
        name: String,
        character: Int,
        onSuccess: () -> Unit = {},
        onFailure: (Exception) -> Unit = {}
    ) {
        val db = FirebaseFirestore.getInstance()

        val user = hashMapOf(
            "uid" to uid,
            "email" to email,
            "name" to name,
            "character" to character
        )

        db.collection("users")
            .document(uid)
            .set(user)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { exception -> onFailure(exception) }
    }
}
