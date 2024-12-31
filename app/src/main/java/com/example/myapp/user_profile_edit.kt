package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask

class EditUserDetail : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var phoneNumEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_profile_edit, container, false)

        // Initialize views
        nameEditText = rootView.findViewById(R.id.edit_user_name)
        emailEditText = rootView.findViewById(R.id.edit_user_email)
        phoneNumEditText = rootView.findViewById(R.id.edit_user_phoneNum)
        saveButton = rootView.findViewById(R.id.save_button)
        uploadImageButton = rootView.findViewById(R.id.upload_image_button)
        imagePreview = rootView.findViewById(R.id.image_preview)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference

        // Fetch current user data
        fetchCurrentUserData()

        // Set up the save button click listener
        saveButton.setOnClickListener {
            saveUserData()
        }

        // Set up the upload image button click listener
        uploadImageButton.setOnClickListener {
            openImagePicker()
        }

        return rootView
    }

    private fun fetchCurrentUserData() {
        val currentUser  = auth.currentUser

        if (currentUser  != null) {
            val userRef = firestore.collection("users").document(currentUser .uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: ""
                        val email = currentUser .email ?: ""
                        val phoneNum = document.getString("phoneNum") ?: ""

                        // Set the data to input fields
                        nameEditText.setText(name)
                        emailEditText.setText(email)
                        phoneNumEditText.setText(phoneNum)

                        // Load the image if it exists
                        val imageUrl = document.getString("imageUrl")
                        if (imageUrl != null) {
                            Glide.with(this)
                                .load(imageUrl)
                                .into(imagePreview)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No User Logged In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserData() {
        val currentUser  = auth.currentUser

        if (currentUser  != null) {
            val userRef = firestore.collection("users").document(currentUser .uid)

            val updatedName = nameEditText.text.toString()
            val updatedEmail = emailEditText.text.toString()
            val updatedPhoneNum = phoneNumEditText.text.toString()
            userRef.update("name", updatedName, "email", updatedEmail, "phoneNum", updatedPhoneNum)
                .addOnSuccessListener {
                    // If an image was selected, upload it
                    if (imageUri != null) {
                        uploadImageToFirebase(imageUri!!)
                    } else {
                        Toast.makeText(requireContext(), "User  data updated successfully", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error updating user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No User Logged In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val currentUser  = auth.currentUser

        if (currentUser  != null) {
            // Create a reference to the location where the image will be stored
            val imageRef = storageRef.child("user_images/${currentUser .uid}/profile_image.jpg")

            // Upload the image
            val uploadTask: UploadTask = imageRef.putFile(imageUri)

            uploadTask.addOnSuccessListener {
                // Get the download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    // Update the Firestore document with the image URL
                    val userRef = firestore.collection("users").document(currentUser .uid)
                    userRef.update("imageUrl", uri.toString())
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "User  data updated successfully", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(requireContext(), "Error updating image URL", Toast.LENGTH_SHORT).show()
                        }
                }
            }.addOnFailureListener {
                Toast.makeText(requireContext(), "Error uploading image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No User Logged In", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            imagePreview.setImageURI(imageUri)
        }
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1000
    }
}