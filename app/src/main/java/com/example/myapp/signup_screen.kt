import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.HomeScreenFragment
import com.example.myapp.ImageUploadFragment
import com.example.myapp.LoginScreen
import com.example.myapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignUpScreen : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var mobileInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private  lateinit var logIn : TextView
    private lateinit var roleSpinner: Spinner
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_screen, container, false)

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        nameInput = view.findViewById(R.id.name_input)
        logIn = view.findViewById(R.id.sign_in)
        mobileInput = view.findViewById(R.id.mobile_input)
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input)
        roleSpinner = view.findViewById(R.id.role_spinner)

        logIn.setOnClickListener {
            val signUpFragment = LoginScreen()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, signUpFragment)
                .addToBackStack(null)
                .commit()
        }

        populateRoleSpinner()


        passwordInput.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= passwordInput.right - passwordInput.compoundDrawables[2].bounds.width()) {
                    togglePasswordVisibility(passwordInput)
                    return@setOnTouchListener true
                }
            }
            false
        }

        val signUpButton: MaterialButton? = view.findViewById(R.id.sign_up_button)
        signUpButton?.setOnClickListener {
            collectUserData()
        }

        return view
    }

    private fun populateRoleSpinner() {
        val roles = arrayOf("User", "Doctor", "Trainer", "Groomer", "NGO Manager")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        roleSpinner.adapter = adapter
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

    private fun collectUserData() {
        val name = nameInput.text.toString().trim()
        val mobile = mobileInput.text.toString().trim()
        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString().trim()
        val confirmPassword = confirmPasswordInput.text.toString().trim()

        if (name.isEmpty() || mobile.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(activity, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val passwordRegex = "^(?=.*[A-Z])(?=.*[!@#\$%^&*()_+=|<>?{}\\[\\]~-])(?=.*\\d).{8,}$".toRegex()
        if (!password.matches(passwordRegex)) {
            Toast.makeText(requireContext(), "Password must contain at least one uppercase letter, one special character, one number, and be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        if (password != confirmPassword) {
            Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        // Register user with Firebase Authentication
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val role = roleSpinner.selectedItem.toString()

                    // Get FCM Token
                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                        if (tokenTask.isSuccessful) {
                            val fcmToken = tokenTask.result

                            // Save additional user data in Firestore along with FCM token
                            val userData = hashMapOf(
                                "name" to name,
                                "mobile" to mobile,
                                "email" to email,
                                "role" to role,
                                "fcmToken" to fcmToken // Add FCM token here
                            )

                            firestore.collection("users").document(user!!.uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                                    redirectToRolePage(role)
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            // Handle error in getting FCM token
                            Toast.makeText(requireContext(), "Failed to get FCM token", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToRolePage(role: String) {
        val nextFragment = when (role) {
            "Doctor" -> VetSignUpFragment()
            "Trainer" -> TrainerSignUpFragment()
            "NGO Manager" -> NGOSignUpFragment()
            "Groomer" -> GroomerSignupFragment()
            else -> ImageUploadFragment()
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, nextFragment)
            .addToBackStack(null)
            .commit()
    }
}