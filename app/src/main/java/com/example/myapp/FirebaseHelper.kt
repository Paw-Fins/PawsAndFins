package com.example.myapp

import android.content.Context
import android.text.TextUtils
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirebaseHelper(private val context: Context) {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Function to validate email
    fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Function to validate phone number
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.length == 10 && phoneNumber.all { it.isDigit() }
    }

    // Function to validate the username (ensures no numbers)
    fun isValidUsername(username: String): Boolean {
        return !username.contains(Regex("[0-9]"))
    }

    // Function to create user with email and password
    fun signUpWithEmailPassword(
        email: String,
        password: String,
        username: String,
        mobile: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = mapOf(
                        "username" to username,
                        "email" to email,
                        "mobile" to mobile
                    )
                    // Save user data in Firestore
                    firestore.collection("users")
                        .document(firebaseAuth.currentUser!!.uid)
                        .set(user)
                        .addOnSuccessListener {
                            onSuccess()  // Call success callback
                        }
                        .addOnFailureListener { exception ->
                            onFailure("Error saving user data: ${exception.message}")
                        }
                } else {
                    onFailure("Error: ${task.exception?.message}")
                }
            }
    }


    // Function to show toast message
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
