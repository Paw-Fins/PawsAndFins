package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.button.MaterialButton
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

class SignUpScreen : Fragment() {

    private lateinit var nameInput: TextInputEditText
    private lateinit var mobileInput: TextInputEditText
    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var imagePreview: ImageView

    private var selectedImageUri: Uri? = null

    private val IMAGE_PICK_REQUEST = 1
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val cloudinary: Cloudinary by lazy {
        Cloudinary(ObjectUtils.asMap(
            "cloud_name", requireContext().getString(R.string.cloud_name),
            "api_key", requireContext().getString(R.string.api_key),
            "api_secret", requireContext().getString(R.string.api_secret)
        ))
    }

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
        imagePreview = view.findViewById(R.id.image_preview)

        val uploadImageButton: MaterialButton = view.findViewById(R.id.upload_image_button)
        uploadImageButton.setOnClickListener {
            openImageChooser()
        }

        val signUpButton: MaterialButton? = view.findViewById(R.id.sign_up_button)
        signUpButton?.setOnClickListener {
            collectUserData()
        }

        val signInTextView: TextView = view.findViewById(R.id.sign_in)
        signInTextView.setOnClickListener {
            val signInFragment = LoginScreen()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, signInFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }



    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_REQUEST) {
            selectedImageUri = data?.data
            imagePreview.setImageURI(selectedImageUri)
        } else {
            Toast.makeText(activity, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
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

        if (password != confirmPassword) {
            Toast.makeText(activity, "Passwords do not match", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    uploadImageToCloudinary(userId)
                } else {
                    Toast.makeText(activity, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun uploadImageToCloudinary(userId: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                selectedImageUri?.let { uri ->
                    val file = getFileFromUri(uri)
                    file?.let {
                        val uploadResult = withContext(Dispatchers.IO) {
                            cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                        }

                        uploadResult?.let {
                            val imageUrl = it["url"].toString()
                            saveImageUrlToFirestore(userId, imageUrl)
                        } ?: run {
                            showToast("Image upload failed")
                        }
                    } ?: run {
                        showToast("File not found")
                    }
                }
            } catch (e: Exception) {
                Log.e("Cloudinary", "Error uploading image: ${e.message}")
                showToast("Error uploading image")
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = activity?.contentResolver?.query(uri, projection, null, null, null)

        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                val filePath = it.getString(columnIndex)
                return File(filePath)
            }
        }

        return null
    }


    private fun saveImageUrlToFirestore(userId: String, imageUrl: String) {
        val userData = hashMapOf(
            "name" to nameInput.text.toString(),
            "mobile" to mobileInput.text.toString(),
            "email" to emailInput.text.toString(),
            "imageUrl" to imageUrl
        )

        firestore.collection("users")
            .document(userId)
            .set(userData)
            .addOnSuccessListener {
                Toast.makeText(activity, "Sign up successful", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }
            .addOnFailureListener { e ->
                Toast.makeText(activity, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        val loginFragment = HomeScreenFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, loginFragment)
            .addToBackStack(null)
            .commit()
    }

}
