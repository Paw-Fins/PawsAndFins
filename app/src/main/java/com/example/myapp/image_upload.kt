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
    private lateinit var uploadButton: Button
    private lateinit var skipButton: Button
    private lateinit var userData: HashMap<String, String>
    private var selectedImageUri: Uri? = null
    private val IMAGE_PICK_REQUEST = 1

    private val cloudinary: Cloudinary by lazy {
        Cloudinary(ObjectUtils.asMap(
            "cloud_name", requireContext().getString(R.string.cloud_name),
            "api_key", requireContext().getString(R.string.api_key),
            "api_secret", requireContext().getString(R.string.api_secret)
        ))
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val firebaseAuth = FirebaseAuth.getInstance()

    // Fallback image URL if the user skips uploading
    private val fallbackImageUrl = "https://example.com/fallback-image.jpg" // Update with your default image URL

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_image_upload, container, false)

        imageView = view.findViewById(R.id.image_preview)
        uploadButton = view.findViewById(R.id.upload_image_button)
        skipButton = view.findViewById(R.id.skip_upload)

        userData = arguments?.getSerializable("userData") as HashMap<String, String>
        Log.d("ImageUploadFragment", "Email: ${userData["email"]}, Password: ${userData["password"]}, Name: ${userData["name"]}, Mobile: ${userData["mobile"]}")
        uploadButton.setOnClickListener {
            openImageChooser()
        }

        skipButton.setOnClickListener {
            val email = userData["email"] ?: return@setOnClickListener
            val password = userData["password"] ?: return@setOnClickListener
            signUpUser(email, password)
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
            imageView.setImageURI(selectedImageUri)
            val email = userData["email"] ?: ""
            val password = userData["password"] ?: ""
            signUpUserAndUploadImage(email, password)
        } else {
            Toast.makeText(activity, "Image selection failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Sign up user
    private fun signUpUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    saveUserDataToFirestore(fallbackImageUrl, isNewUser = true)
                } else {
                    Toast.makeText(activity, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    // Sign up and upload image if available
    private fun signUpUserAndUploadImage(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // User is signed up, proceed with image upload
                    val userId = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener

                    selectedImageUri?.let { uri ->
                        val file = getFileFromUri(uri)
                        file?.let {
                            CoroutineScope(Dispatchers.Main).launch {
                                try {
                                    val uploadResult = withContext(Dispatchers.IO) {
                                        cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                                    }

                                    uploadResult?.let {
                                        val imageUrl = it["url"].toString()
                                        saveUserDataToFirestore(imageUrl, isNewUser = true) // Save all user data (email, password, image)
                                    } ?: run {
                                        showToast("Image upload failed")
                                    }
                                } catch (e: Exception) {
                                    showToast("Error uploading image: ${e.message}")
                                }
                            }
                        } ?: run {
                            showToast("File not found")
                        }
                    }
                } else {
                    Toast.makeText(activity, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val fileDescriptor = activity?.contentResolver?.openFileDescriptor(uri, "r")
            fileDescriptor?.let {
                val inputStream = activity?.contentResolver?.openInputStream(uri)
                val file = File(requireContext().cacheDir, "uploaded_image.jpg")
                inputStream?.copyTo(file.outputStream())
                file
            }
        } catch (e: Exception) {
            null
        }
    }

    // Save user data to Firestore (with or without image URL)
    private fun saveUserDataToFirestore(imageUrl: String, isNewUser: Boolean) {
        val userId = firebaseAuth.currentUser?.uid ?: return
        val userMap: MutableMap<String, Any> = mutableMapOf()
        userMap["imageUrl"] = imageUrl
        userMap["email"] = userData["email"] ?: ""
        userMap["password"] = userData["password"] ?: ""
        userMap["role"] = userData["role"] ?: "User"
        userMap["name"] = userData["name"] ?: ""
        userMap["mobile"] = userData["mobile"] ?: ""
        if (isNewUser) {
            firestore.collection("users")
                .document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    showToast("User data saved successfully")
                    navigateToHome()
                }
                .addOnFailureListener { e ->
                    showToast("Failed to save user data: ${e.message}")
                }
        } else {
            showToast("User signed up successfully")
            navigateToHome()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun navigateToHome() {
        val userMap: MutableMap<String, Any> = mutableMapOf()
        if(userMap["role"] != "User" || userMap["role"] != "user" ){
            val servicesDashborad = ServiceDashboard()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container,servicesDashborad)
                .addToBackStack(null)
                .commit()
        }
        else{
        val homeFragment = HomeScreenFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .addToBackStack(null)
            .commit()
        }

    }
}

