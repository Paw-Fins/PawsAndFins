package com.example.myapp

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelperLogin(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Function to log in the user
    fun loginUser(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        // Check if the user is banned
                        checkIfUserIsBanned(userId, { isBanned ->
                            if (isBanned) {
                                // User is banned, sign out and notify
                                auth.signOut()
                                onFailure("Your account is banned. Please contact support.")
                            } else {
                                // User is not banned, proceed with login
                                onSuccess()
                            }
                        }, { errorMessage ->
                            // Failed to check the banned status
                            auth.signOut()
                            onFailure(errorMessage)
                        })
                    } else {
                        onFailure("User ID is null. Unable to verify banned status.")
                    }
                } else {
                    // Login failed, handle the error
                    val errorMessage = task.exception?.localizedMessage ?: "Unknown error"
                    onFailure(errorMessage)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any failures (e.g., network failure)
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
