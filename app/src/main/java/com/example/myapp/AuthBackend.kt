
package com.example.myapp

import android.util.Log
import com.google.firebase.auth.FirebaseAuth

class AuthBackend {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // Function to send password reset email
    fun sendPasswordResetEmail(email: String, onComplete: (Boolean, String) -> Unit) {
        if (email.isEmpty()) {
            onComplete(false, "Please enter a valid email")
            return
        }

        // Send password reset email
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete(true, "Password reset email sent successfully")
                } else {
                    onComplete(
                        false,
                        "Failed to send password reset email: ${task.exception?.message}"
                    )
                }
            }
    }
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // Function to check if the email is registered
    fun isEmailRegistered(email: String, onComplete: (Boolean) -> Unit) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    // If the result is not null and contains methods, email exists
                    val emailExists = result?.signInMethods?.isNotEmpty() == true
                    if (emailExists) {
                        onComplete(true) // Email exists
                    } else {
                        onComplete(false)
                    }
                } else {
                    onComplete(false)
                }
            }
    }

}