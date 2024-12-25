package com.example.myapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class LoginScreen : Fragment() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var firebaseHelperLogin: FirebaseHelperLogin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseHelperLogin = FirebaseHelperLogin(requireContext())  // Initialize FirebaseHelperLogin
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_screen, container, false)
        emailInput = view.findViewById(R.id.pasword_email_input)
        passwordInput = view.findViewById(R.id.password_input)

        (requireActivity() as MainActivity).showBottomNavigation(false)

        val signUpTextView: TextView = view.findViewById(R.id.sign_up)
        val forgotTextView: TextView = view.findViewById(R.id.forgot_password)

        forgotTextView.setOnClickListener {
            val forgotPasswordScreen = ForgotPasswordScreen()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, forgotPasswordScreen)
                .addToBackStack(null)
                .commit()
        }

        signUpTextView.setOnClickListener {
            val signUpFragment = SignUpScreen()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, signUpFragment)
                .addToBackStack(null)
                .commit()
        }

        val signInButton: TextView = view.findViewById(R.id.submit_button)
        signInButton.setOnClickListener {

            collectUser()
        }

        return view
    }

    private fun collectUser() {
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter both email and password",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Use FirebaseHelperLogin to validate the user credentials
        firebaseHelperLogin.loginUser(email, password, {
            Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }, { errorMessage ->
            // Show error message if login fails
            Toast.makeText(requireContext(), "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
        })
    }

    private fun navigateToHome() {
        val loginFragment = HomeScreenFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .addToBackStack(null)
            .commit()
    }
}
