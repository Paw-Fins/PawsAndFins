package com.example.myapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordScreen : Fragment() {

    private lateinit var emailInput: TextInputEditText
    private val authBackend = AuthBackend() // Instance of AuthBackend

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Correcting Firebase initialization by using requireContext()
        FirebaseApp.initializeApp(requireContext()) // Pass the correct context here
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        emailInput = view.findViewById(R.id.pasword_email_input)

        val signUpTextView: TextView = view.findViewById(R.id.sign_up)
        signUpTextView.setOnClickListener {
            val signUpFragment = SignUpScreen()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                transaction.hide(currentFragment)
            }
            transaction.replace(R.id.fragment_container, signUpFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

//        val signInTextView: TextView = view.findViewById(R.id.login_link)
//        signInTextView.setOnClickListener {
//            val signInFragment = LoginScreen()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
//            if (currentFragment != null) {
//                transaction.hide(currentFragment)
//            }
//            transaction.replace(R.id.fragment_container, signInFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }


        val submitButton: TextView = view.findViewById(R.id.submit_button)
        submitButton.setOnClickListener {
            collectUser()
        }

        return view
    }

    private fun collectUser() {
        val email = emailInput.text.toString().trim()
        Log.d("ForgotPassword", "Entered email: $email") // Check if the email is correct

        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your email", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate the email format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the email is registered using Firebase Authentication
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val result = task.result
                    val signInMethods = result?.signInMethods ?: emptyList()

                    if (signInMethods.isNotEmpty()) {
                        // If registered, proceed with password reset
                        authBackend.sendPasswordResetEmail(email) { success, message ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Email is not registered", Toast.LENGTH_SHORT).show()
                        authBackend.sendPasswordResetEmail(email) { success, message ->
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }

                    }
                } else {
                    // Handle error in fetching sign-in methods
                    Toast.makeText(requireContext(), "Failed to check email registration", Toast.LENGTH_SHORT).show()
                }
            }
    }



}