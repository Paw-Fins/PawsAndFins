package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginScreen : Fragment() {

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var firebaseHelperLogin: FirebaseHelperLogin

    private val adminCredentials = object {
        val adminEmail = "test@gmail.com"
        val adminPassword = "test@123"
    }

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

        // Adding a callback to handle back press actions, including gestures
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()  // Exit the app when back or gesture is used
            }
        })

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

        // Check for admin login
        if (email == adminCredentials.adminEmail && password == adminCredentials.adminPassword) {
            Toast.makeText(
                requireContext(),
                "Admin Login Successful!\nEmail: $email",
                Toast.LENGTH_SHORT
            ).show()
            navigateToAdminScreen()
        } else {
            // Use FirebaseHelperLogin for regular user login
            firebaseHelperLogin.loginUser(email, password, {
                Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }, { errorMessage ->
                // Show error message if login fails
                Toast.makeText(requireContext(), "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun navigateToAdminScreen() {
        try {
            val adminMainFragment = AdminScreenFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                transaction.hide(currentFragment)
            }
            transaction.replace(R.id.fragment_container, adminMainFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        } catch (e: Exception) {
            Log.e("LoginScreen", "Error during fragment transaction", e)
        }
    }

    private fun navigateToHome() {
        val homeScreenFragment = HomeScreenFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, homeScreenFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}