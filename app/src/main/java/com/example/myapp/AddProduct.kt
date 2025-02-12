//package com.example.myapp
//
//import android.app.Activity
//import android.content.Intent
//import android.net.Uri
//import android.os.Bundle
//import android.provider.MediaStore
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageView
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import com.bumptech.glide.Glide
//import com.cloudinary.Cloudinary
//import com.cloudinary.utils.ObjectUtils
//import com.google.firebase.firestore.FirebaseFirestore
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.withContext
//import kotlinx.coroutines.CoroutineScope
//import java.io.File
//import java.util.*
//
//class AddProductFragment : Fragment() {
//
//    private lateinit var productImagePreview: ImageView
//    private lateinit var productName: EditText
//    private lateinit var productPrice: EditText
//    private lateinit var productDescription: EditText
//    private lateinit var uploadImageButton: View
//    private lateinit var submitProductButton: View
//
//    private var imageUri: Uri? = null
//    private val PICK_IMAGE_REQUEST = 71
//
//    // Firebase instance
//    private val db = FirebaseFirestore.getInstance()
//
//    // Cloudinary configuration
//    private val cloudName: String by lazy { getString(R.string.cloud_name) }
//    private val apiKey: String by lazy { getString(R.string.api_key) }
//    private val apiSecret: String by lazy { getString(R.string.api_secret) }
//
//    // Initialize Cloudinary
//    private val cloudinary: Cloudinary by lazy {
//        Cloudinary(
//            ObjectUtils.asMap(
//                "cloud_name", cloudName,
//                "api_key", apiKey,
//                "api_secret", apiSecret
//            )
//        )
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Inflate the fragment layout
//        val view = inflater.inflate(R.layout.fragment_add_product, container, false)
//
//        // Initialize views
//        productImagePreview = view.findViewById(R.id.productImagePreview)
//        productName = view.findViewById(R.id.productName)
//        productPrice = view.findViewById(R.id.productPrice)
//        productDescription = view.findViewById(R.id.productDescription)
//        uploadImageButton = view.findViewById(R.id.uploadImageButton)
//        submitProductButton = view.findViewById(R.id.submitProductButton)
//
//        // Image upload button click listener
//        uploadImageButton.setOnClickListener {
//            openImageChooser()
//        }
//
//        // Submit product button click listener
//        submitProductButton.setOnClickListener {
//            submitProduct()
//        }
//
//        return view
//    }
//
//    // Opens the image chooser to select an image
//    private fun openImageChooser() {
//        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
//        startActivityForResult(intent, PICK_IMAGE_REQUEST)
//    }
//
//    // Handle the image selection result
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
//            imageUri = data.data
//            // Preview the selected image using Glide
//            Glide.with(requireContext())
//                .load(imageUri)
//                .into(productImagePreview)
//        }
//    }
//
//    // Submit the product details and upload the image
//    private fun submitProduct() {
//        val name = productName.text.toString().trim()
//        val price = productPrice.text.toString().trim()
//        val description = productDescription.text.toString().trim()
//
//        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUri == null) {
//            Toast.makeText(requireContext(), "Please fill all fields and upload an image.", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        // Upload the image to Cloudinary
//        uploadImageToCloudinary(imageUri!!)
//    }
//
//    // Upload the image to Cloudinary and get the URL
//    private fun uploadImageToCloudinary(imageUri: Uri) {
//        CoroutineScope(Dispatchers.Main).launch {
//            try {
//                val file = getFileFromUri(imageUri)
//                file?.let {
//                    // Upload image to Cloudinary in background
//                    val uploadResult = withContext(Dispatchers.IO) {
//                        cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
//                    }
//
//                    // Handle upload result
//                    uploadResult?.let {
//                        val imageUrl = it["url"].toString()
//                        uploadProductToFirestore(imageUrl)
//                    } ?: run {
//                        showToast("Image upload failed")
//                    }
//                } ?: run {
//                    showToast("File not found")
//                }
//            } catch (e: Exception) {
//                Log.d("Error image","error: ${e.message}")
//                showToast("Error uploading image: ${e.message}")
//            }
//        }
//    }
//
//    // Convert Uri to File
//    private fun getFileFromUri(uri: Uri): File? {
//        val projection = arrayOf(MediaStore.Images.Media.DATA)
//        val cursor = activity?.contentResolver?.query(uri, projection, null, null, null)
//
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//                val filePath = it.getString(columnIndex)
//                return File(filePath)
//            }
//        }
//
//        return null
//    }
//
//    // Upload product details to Firestore
//    private fun uploadProductToFirestore(imageUrl: String) {
//        val name = productName.text.toString().trim()
//        val price = productPrice.text.toString().trim()
//        val description = productDescription.text.toString().trim()
//
//        val product = hashMapOf(
//            "name" to name,
//            "price" to price,
//            "description" to description,
//            "imageUrl" to imageUrl
//        )
//
//        // Add product to the "products" collection in Firestore
//        db.collection("products")
//            .add(product)
//            .addOnSuccessListener {
//                Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
//                clearFields()
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(requireContext(), "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }
//
//    // Clear input fields after submission
//    private fun clearFields() {
//        productName.text.clear()
//        productPrice.text.clear()
//        productDescription.text.clear()
//        productImagePreview.setImageResource(R.drawable.dummy_product) // Set to a placeholder image
//    }
//
//    // Show Toast message
//    private fun showToast(message: String) {
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
//    }
//}


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
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import java.io.File

class AddProductFragment : Fragment() {

    private lateinit var productImagePreview: ImageView
    private lateinit var productName: EditText
    private lateinit var productPrice: EditText
    private lateinit var productDescription: EditText
    private lateinit var uploadImageButton: View
    private lateinit var submitProductButton: View
    private lateinit var categorySpinner: Spinner

    private var imageUri: Uri? = null
    private var selectedCategory: String? = null
    private val PICK_IMAGE_REQUEST = 71

    // Firebase instance
    private val db = FirebaseFirestore.getInstance()

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
        val view = inflater.inflate(R.layout.fragment_add_product, container, false)

        // Initialize views
        productImagePreview = view.findViewById(R.id.productImagePreview)
        productName = view.findViewById(R.id.productName)
        productPrice = view.findViewById(R.id.productPrice)
        productDescription = view.findViewById(R.id.productDescription)
        uploadImageButton = view.findViewById(R.id.uploadImageButton)
        submitProductButton = view.findViewById(R.id.submitProductButton)
        categorySpinner = view.findViewById(R.id.filterSpinner)

        // Handle spinner item selection
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options, // Load values from strings.xml
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            categorySpinner.adapter = adapter
        }
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = parent?.getItemAtPosition(position).toString()
                Log.d("Spinner", "Selected category: $selectedCategory") // Debug log
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedCategory = null
            }
        }

        uploadImageButton.setOnClickListener { openImageChooser() }
        submitProductButton.setOnClickListener { submitProduct() }

        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            Glide.with(requireContext()).load(imageUri).into(productImagePreview)
        }
    }

    private fun submitProduct() {
        val name = productName.text.toString().trim()
        val price = productPrice.text.toString().trim()
        val description = productDescription.text.toString().trim()

        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUri == null || selectedCategory.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields and select a category.", Toast.LENGTH_SHORT).show()
            return
        }

        uploadImageToCloudinary(imageUri!!)
    }

    private fun uploadImageToCloudinary(imageUri: Uri) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val file = getFileFromUri(imageUri)
                file?.let {
                    val uploadResult = withContext(Dispatchers.IO) {
                        cloudinary.uploader().upload(file, ObjectUtils.emptyMap())
                    }

                    uploadResult?.let {
                        val imageUrl = it["url"].toString()
                        uploadProductToFirestore(imageUrl)
                    } ?: showToast("Image upload failed")
                } ?: showToast("File not found")
            } catch (e: Exception) {
                Log.d("Error image", "error: ${e.message}")
                showToast("Error uploading image: ${e.message}")
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

    private fun uploadProductToFirestore(imageUrl: String) {
        val name = productName.text.toString().trim()
        val price = productPrice.text.toString().trim()
        val description = productDescription.text.toString().trim()

        val product = hashMapOf(
            "name" to name,
            "price" to price,
            "description" to description,
            "imageUrl" to imageUrl,
            "category" to selectedCategory
        )

        db.collection("products")
            .add(product)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product added successfully!", Toast.LENGTH_SHORT).show()
                clearFields()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearFields() {
        productName.text.clear()
        productPrice.text.clear()
        productDescription.text.clear()
        productImagePreview.setImageResource(R.drawable.dummy_product)
        categorySpinner.setSelection(0)
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
