package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageUploadFragment : Fragment() {

    private lateinit var imageView: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var uploadButton: Button
    private lateinit var skipButton: Button
    private lateinit var userData: HashMap<String, String>
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_REQUEST = 1

    private val cloudinary: Cloudinary by lazy {
        Cloudinary(ObjectUtils.asMap(
            "cloud_name", getString(R.string.cloud_name),
            "api_key", getString(R.string.api_key),
            "api_secret", getString(R.string.api_secret)
        ))
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_upload, container, false)

        imageView = view.findViewById(R.id.image_preview)
        selectImageButton = view.findViewById(R.id.select_image_button)
        uploadButton = view.findViewById(R.id.upload_image_button)
        skipButton = view.findViewById(R.id.skip_upload)

        selectImageButton.setOnClickListener { openImageChooser() }
        uploadButton.setOnClickListener { onSubmitImage() }
        skipButton.setOnClickListener { registerUserAndNavigate(fallbackImageUrl = true) }

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
            imageView.setImageURI(selectedImageUri)
        }
    }

    private fun onSubmitImage() {
        selectedImageUri?.let {
            uploadImageToCloudinary(it)
        } ?: run {
            Toast.makeText(activity, "Please select an image first", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToCloudinary(uri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val file = File(uri.path)
                val result = cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                val imageUrl = result["url"].toString()
                registerUserAndNavigate(fallbackImageUrl = false, imageUrl = imageUrl)
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(activity, "Image upload failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun registerUserAndNavigate(fallbackImageUrl: Boolean, imageUrl: String = "") {
        val userId = firebaseAuth.currentUser?.uid

        // If no image URL was uploaded, use a fallback image URL
        val imageToUpload = if (fallbackImageUrl) {
            "http://res.cloudinary.com/dmg3a821h/image/upload/v1736325853/hhtag3kjkpvag7qwqszo.jpg" // Replace with a default image URL
        } else {
            imageUrl
        }

        // Add the image URL to the user data
        val userDataWithImage = hashMapOf<String, Any>(
            "imageUrl" to imageToUpload
        )

        // Retrieve the user's role from Firestore
        userId?.let { uid ->
            firestore.collection("users").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val role = document.getString("role")

                        // Add the image URL to Firestore
                        firestore.collection("users").document(uid)
                            .update(userDataWithImage)
                            .addOnSuccessListener {
                                // If the data was successfully saved, navigate to the appropriate screen
                                val targetFragment = if (role.equals("User", ignoreCase = true)) {
                                    HomeScreenFragment()
                                } else {
                                    ServiceDashboard()
                                }
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container, targetFragment)
                                    .commit()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(activity, "Failed to update user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        Toast.makeText(activity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(activity, "Error fetching user role: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
