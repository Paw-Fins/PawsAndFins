package com.example.myapp

import SignUpScreen
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.firebase.messaging.FirebaseMessaging

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
        firebaseHelperLogin = FirebaseHelperLogin(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login_screen, container, false)

        emailInput = view.findViewById(R.id.pasword_email_input)
        passwordInput = view.findViewById(R.id.password_input)
        passwordInput.setOnTouchListener { v ,event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= passwordInput.right - passwordInput.compoundDrawables[2].bounds.width()) {
                    togglePasswordVisibility(passwordInput)
                    return@setOnTouchListener true
                }
            }
            false
        }

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

    private fun togglePasswordVisibility(editText: TextInputEditText) {
        if (editText.inputType == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.open_eye, 0)
        } else {
            editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.cross_open_eye, 0)
        }
        editText.setSelection(editText.text?.length ?: 0)
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
            // Log in using Firebase authentication
            firebaseHelperLogin.loginUser(email, password, { firebaseUser ->
                if (firebaseUser != null) {
                    // Fetch the FCM token and store it in Firestore
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        storeFcmToken(firebaseUser.uid, token)
                    }

                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
                    val userId = firebaseUser.uid

                    // Fetch the user's role from Firestore
                    FirebaseFirestore.getInstance().collection("users")
                        .document(userId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val userRole = document.getString("role") // Assuming "role" field exists in the user document
                                when (userRole) {
                                    "User" -> navigateToHome()  // Navigate to Home screen if role is "User"
                                    "Doctor" -> navigateToDoctorDashboard()  // Navigate to Doctor Dashboard
                                    "Groomer" -> navigateToGroomerDashboard()  // Navigate to Groomer Dashboard
                                    "Trainer" -> navigateToTrainerDashboard()  // Navigate to Trainer Dashboard
                                    "Ngo" -> navigateToNgoDashboard()  // Navigate to Ngo Dashboard
                                    else -> {
                                        Toast.makeText(requireContext(), "Role not recognized.", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                // Document not found
                                Toast.makeText(requireContext(), "User document not found!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle error while fetching the user role
                            Toast.makeText(requireContext(), "Error fetching user role: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }, { errorMessage ->
                Toast.makeText(requireContext(), "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
            })
        }
    }

    private fun storeFcmToken(userId: String, token: String) {
        FirebaseFirestore.getInstance().collection("users")
            .document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                Log.d("LoginScreen", "FCM token stored successfully.")
            }
            .addOnFailureListener { e ->
                Log.e("LoginScreen", "Error storing FCM token: ${e.message}")
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

    private fun navigateToDoctorDashboard() {
        val doctorDashboardFragment = ServiceDashboard()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, doctorDashboardFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToGroomerDashboard() {
        val groomerDashboardFragment = GroomerDashboard()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, groomerDashboardFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToTrainerDashboard() {
        val trainerDashboardFragment = TrainerDashboard()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, trainerDashboardFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun navigateToNgoDashboard() {
        val ngoDashboardFragment = NGODashboard()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, ngoDashboardFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
