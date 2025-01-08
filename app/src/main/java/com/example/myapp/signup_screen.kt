package com.example.myapp

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
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton

class SignUpScreen : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var mobileInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var roleSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup_screen, container, false)

        nameInput = view.findViewById(R.id.name_input)
        mobileInput = view.findViewById(R.id.mobile_input)
        emailInput = view.findViewById(R.id.email_input)
        passwordInput = view.findViewById(R.id.password_input)
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input)
        roleSpinner = view.findViewById(R.id.role_spinner)

        populateRoleSpinner()

        passwordInput.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= passwordInput.right - passwordInput.compoundDrawables[2].bounds.width()) {
                    togglePasswordVisibility(passwordInput)
                    return@setOnTouchListener true
                }
            }
            false
        }

//        confirmPasswordInput.setOnTouchListener { v, event ->
//            if (event.action == MotionEvent.ACTION_UP) {
//                if (event.rawX >= confirmPasswordInput.right - confirmPasswordInput.compoundDrawables[2].bounds.width()) {
//                    togglePasswordVisibility(confirmPasswordInput)
//                    return@setOnTouchListener true
//                }
//            }
//            false
//        }

        val signUpButton: MaterialButton? = view.findViewById(R.id.sign_up_button)
        signUpButton?.setOnClickListener {
            collectUserData()
        }

        val signInTextView: TextView = view.findViewById(R.id.sign_in)
        signInTextView.setOnClickListener {
            val signInFragment = LoginScreen()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment != null) {
                transaction.hide(currentFragment)
            }
            transaction.replace(R.id.fragment_container, signInFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }

    private fun populateRoleSpinner() {
        val roles = arrayOf( "User", "Doctor","Trainer", "Groomer", "NGO Manager")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        roleSpinner.adapter = object : ArrayAdapter<String>(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.textSize = 12f
                return view
            }
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.textSize = 12f
                return view
            }
        }
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
            Toast.makeText(
                requireContext(),
                "Password must contain at least one uppercase letter, one special character, one number, and be at least 8 characters long",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (password != confirmPassword) {
            Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }
        val role = roleSpinner.selectedItem.toString()
        val userData = hashMapOf(
            "name" to name,
            "mobile" to mobile,
            "email" to email,
            "password" to password,
            "role" to role
        )
        val imageUploadFragment = ImageUploadFragment()
        val bundle = Bundle()
        bundle.putSerializable("userData", userData)
        imageUploadFragment.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, imageUploadFragment)
            .addToBackStack(null)
            .commit()
    }
}
