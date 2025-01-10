package com.example.myapp

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelperLogin(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    fun loginUser(
        email: String,
        password: String,
        onSuccess: (FirebaseUser) -> Unit,  // Pass FirebaseUser to callback
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    if (user != null) {
                        onSuccess(user)  // Pass the FirebaseUser object on success
                    } else {
                        onFailure("User not found")
                    }
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    onFailure(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.localizedMessage ?: "Network error")
            }
    }


    private fun checkIfUserIsBanned(
        userId: String,
        onSuccess: (Boolean) -> Unit,
        onFailure: (String) -> Unit
    ) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val isBanned = document.getBoolean("isBanned") ?: false
                    onSuccess(isBanned)
                } else {
                    onFailure("User document not found in Firestore.")
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.localizedMessage ?: "Error fetching user data.")
            }
    }
}
