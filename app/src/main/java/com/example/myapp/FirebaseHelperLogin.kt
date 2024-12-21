package com.example.myapp

import android.content.Context
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth

class FirebaseHelperLogin(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Function to log in the user
    fun loginUser(email: String,
                  password: String,
                  onSuccess: () -> Unit,
                  onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Login successful
                    onSuccess()
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
}
