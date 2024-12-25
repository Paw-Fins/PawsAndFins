package com.example.myapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class SignUpScreen : Fragment() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var usernameInput: TextInputEditText
    private lateinit var mobileInput: TextInputEditText

    private lateinit var firebaseHelper: FirebaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseHelper = FirebaseHelper(requireContext())  // Initialize FirebaseHelper
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_screen, container, false)

        // Initialize the input fields
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input)
        usernameInput = view.findViewById(R.id.name_input)
        mobileInput = view.findViewById(R.id.mobile_input)

        // Set up the click listener for the Sign Up button
        val signUpButton: MaterialButton? = view.findViewById(R.id.sign_up_button)
        signUpButton?.setOnClickListener {
            collectUser()
        }

        // Set up the click listener for the Sign In text
        val signInTextView: TextView = view.findViewById(R.id.sign_in)
        signInTextView.setOnClickListener {
            navigateToLogin()
        }

        return view
    }

    private fun collectUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()
        val username = usernameInput.text.toString().trim()
        val mobile = mobileInput.text.toString().trim()

        // Validate inputs using FirebaseHelper
        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || username.isEmpty() || mobile.isEmpty()) {
            showToast("Please fill in all fields")
            return
        }

        if (!firebaseHelper.isValidEmail(email)) {
            showToast("Please enter a valid email address")
            return
        }

        if (password != confirmPassword) {
            showToast("Passwords do not match")
            return
        }

        if (!firebaseHelper.isValidUsername(username)) {
            showToast("Name cannot contain numbers")
            return
        }

        if (!firebaseHelper.isValidPhoneNumber(mobile)) {
            showToast("Please enter a valid phone number")
            return
        }

        // Proceed with sign-up process
        firebaseHelper.signUpWithEmailPassword(email, password, username, mobile, {
            // Handle success
            showToast("User registered successfully")
            // Redirect to Login screen
            navigateToHome()
        }, { errorMessage ->
            // Handle failure (Custom error message)

            // Check if the error is related to an existing email
            if (errorMessage.contains("email address is already in use")) {
                showToast("User already registered. Please login")
                navigateToLogin()
            } else {
                showToast(errorMessage)
            }
        })
    }

    // Function to navigate to the Login screen
    private fun navigateToLogin() {
        val loginFragment = LoginScreen()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun navigateToHome() {
        val loginFragment = HomeScreenFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .addToBackStack(null)
            .commit()
    }


    // Function to show toast message
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
