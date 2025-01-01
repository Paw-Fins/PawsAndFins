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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

class EditProductFragment : Fragment() {

    private lateinit var productId: String
    private lateinit var productName: String
    private lateinit var productPrice: String
    private lateinit var productDescription: String
    private lateinit var productImageUrl: String

    private lateinit var editName: EditText
    private lateinit var editPrice: EditText
    private lateinit var editDescription: EditText
    private lateinit var productImagePreview: ImageView
    private lateinit var uploadImageButton: View
    private lateinit var saveButton: MaterialButton

    private var imageUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    // Firebase instance
    private val firestore = FirebaseFirestore.getInstance()

    // Cloudinary configuration
    private val cloudName: String by lazy { getString(R.string.cloud_name) }
    private val apiKey: String by lazy { getString(R.string.api_key) }
    private val apiSecret: String by lazy { getString(R.string.api_secret) }

    // Initialize Cloudinary
    private val cloudinary: Cloudinary by lazy {
        Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_edit_product, container, false)

        // Retrieve the passed arguments
        productId = arguments?.getString("productId") ?: ""
        productName = arguments?.getString("productName") ?: ""
        productPrice = arguments?.getString("productPrice") ?: ""
        productDescription = arguments?.getString("productDescription") ?: ""
        productImageUrl = arguments?.getString("productImageUrl") ?: ""

        // Bind the views
        editName = view.findViewById(R.id.editProductName)
        editPrice = view.findViewById(R.id.editProductPrice)
        editDescription = view.findViewById(R.id.editProductDescription)
        productImagePreview = view.findViewById(R.id.productImagePreview)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        saveButton = view.findViewById(R.id.saveProductButton)

        // Set existing data in EditTexts
        editName.setText(productName)
        editPrice.setText(productPrice)
        editDescription.setText(productDescription)

        // Set the existing image preview
        Glide.with(requireContext())
            .load(productImageUrl)
            .into(productImagePreview)

        // Upload new image on button click
        uploadImageButton.setOnClickListener {
            openImageChooser()
        }

        // Save changes to product details
        saveButton.setOnClickListener {
            saveProductChanges()
        }

        return view
    }

    // Opens the image chooser to select an image
    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    // Handle the image selection result
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            // Preview the selected image using Glide
            Glide.with(requireContext())
                .load(imageUri)
                .into(productImagePreview)
        }
    }

    // Save product changes (including image) to Firestore
    private fun saveProductChanges() {
        val updatedName = editName.text.toString().trim()
        val updatedPrice = editPrice.text.toString().trim()
        val updatedDescription = editDescription.text.toString().trim()

        if (updatedName.isEmpty() || updatedPrice.isEmpty() || updatedDescription.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // If image is updated, upload to Cloudinary
        if (imageUri != null) {
            uploadImageToCloudinary(imageUri!!)
        } else {
            // If no new image, update product details without image
            updateProductInFirestore(productImageUrl)
        }
    }

    // Upload the image to Cloudinary and get the URL
    private fun uploadImageToCloudinary(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val file = getFileFromUri(imageUri)
                file?.let {
                    // Upload image to Cloudinary in background
                    val uploadResult = withContext(Dispatchers.IO) {
                        cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                    }

                    // Handle upload result
                    uploadResult?.let {
                        val imageUrl = it["url"].toString()
                        updateProductInFirestore(imageUrl)
                    } ?: run {
                        showToast("Image upload failed")
                    }
                } ?: run {
                    showToast("File not found")
                }
            } catch (e: Exception) {
                Log.d("Error image", "error: ${e.message}")
                showToast("Error uploading image: ${e.message}")
            }
        }
    }

    // Convert Uri to File
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

    // Update product details in Firestore
    private fun updateProductInFirestore(imageUrl: String) {
        val updatedName = editName.text.toString().trim()
        val updatedPrice = editPrice.text.toString().trim()
        val updatedDescription = editDescription.text.toString().trim()

        val updatedProduct = hashMapOf(
            "name" to updatedName,
            "price" to updatedPrice,
            "description" to updatedDescription,
            "imageUrl" to imageUrl
        )

        firestore.collection("products").document(productId)
            .update(updatedProduct as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product updated successfully", Toast.LENGTH_SHORT).show()
                requireActivity().supportFragmentManager.popBackStack() // Navigate back to Admin screen
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Show Toast message
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
