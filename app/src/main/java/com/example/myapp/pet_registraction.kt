package com.example.myapp

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.concurrent.Executors

class PetRegistrationFragment : Fragment() {

    private lateinit var etPetName: EditText
    private lateinit var etPetType: EditText
    private lateinit var etPetBreed: EditText
    private lateinit var etPetWeight: EditText
    private lateinit var etPetAge: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var btnUpload: Button
    private lateinit var btnSaveProfile: Button
    private lateinit var petidText: TextView
    private lateinit var petImage: ImageView

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var petId: String? = null
    private var imageUri: Uri? = null
    private var uploadedImageUrl: String? = null

    private val cloudinary: Cloudinary by lazy {
        Cloudinary(
            ObjectUtils.asMap(
                "cloud_name", getString(R.string.cloud_name),
                "api_key", getString(R.string.api_key),
                "api_secret", getString(R.string.api_secret)
            )
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_registration, container, false)

        // Initialize UI Components
        etPetName = view.findViewById(R.id.etPetName)
        etPetType = view.findViewById(R.id.etPetType)
        etPetBreed = view.findViewById(R.id.etPetBreed)
        etPetWeight = view.findViewById(R.id.etPetWeight)
        etPetAge = view.findViewById(R.id.etPetAge)
        radioGroupGender = view.findViewById(R.id.radioGroupGender)
        btnUpload = view.findViewById(R.id.uploadButton)
        btnSaveProfile = view.findViewById(R.id.saveProfileButton)
        petImage = view.findViewById(R.id.petImage)
        petidText = view.findViewById(R.id.tvPetId)

        btnUpload.setOnClickListener { openGallery() }

        btnSaveProfile.setOnClickListener {
            if (imageUri != null) {
                // First upload image, then save pet profile
                uploadImageToCloudinary(imageUri!!) {
                    savePetProfile()
                }
            } else {
                // If no new image, directly save profile
                savePetProfile()
            }
        }

        fetchPetData()
        return view
    }

    private fun fetchPetData() {
        userId?.let {
            db.collection("pets").whereEqualTo("userId", it).get()
                .addOnSuccessListener { documents ->
                    for (doc in documents) {
                        petidText.text = doc.getString("petId")
                        etPetName.hint = doc.getString("name") ?: "Enter Pet Name"
                        etPetType.hint = doc.getString("type") ?: "Enter Pet Type"
                        etPetBreed.hint = doc.getString("breed") ?: "Enter Pet Breed"
                        etPetWeight.hint = doc.getDouble("weight")?.toString() ?: "Enter Weight (kg)"
                        etPetAge.hint = doc.getLong("age")?.toString() ?: "Enter Age (years)"

                        val gender = doc.getString("gender")
                        if (gender == "Male") view?.findViewById<RadioButton>(R.id.rbMale)?.isChecked = true
                        else view?.findViewById<RadioButton>(R.id.rbFemale)?.isChecked = true

                        uploadedImageUrl = doc.getString("imageUrl")
                        uploadedImageUrl?.let {
                            Glide.with(this).load(it).into(petImage)
                        }
                    }
                }
        }
    }

    private fun savePetProfile() {
        val petName = etPetName.text.toString().trim().ifEmpty { etPetName.hint.toString() }
        val petType = etPetType.text.toString().trim().ifEmpty { etPetType.hint.toString() }
        val petBreed = etPetBreed.text.toString().trim().ifEmpty { etPetBreed.hint.toString() }

        val petWeightText = etPetWeight.text.toString().trim()
        val petAgeText = etPetAge.text.toString().trim()

        val petWeight = if (petWeightText.isNotEmpty()) petWeightText.toDouble() else etPetWeight.hint.toString().toDoubleOrNull() ?: 0.0
        val petAge = if (petAgeText.isNotEmpty()) petAgeText.toInt() else etPetAge.hint.toString().toIntOrNull() ?: 0

        val selectedGenderId = radioGroupGender.checkedRadioButtonId
        val gender = if (selectedGenderId == R.id.rbMale) "Male" else if (selectedGenderId == R.id.rbFemale) "Female" else null

        val finalGender = gender ?: view?.findViewById<RadioButton>(R.id.rbMale)?.text.toString()
        val petData = hashMapOf(
            "name" to petName,
            "type" to petType,
            "breed" to petBreed,
            "weight" to petWeight,
            "age" to petAge,
            "gender" to finalGender,
            "imageUrl" to uploadedImageUrl,
            "userId" to userId,
            "petId" to petId
        )
        if (petId == null) {
            petId = generateUniquePetId()
            db.collection("pets").document(petId!!)
                .set(petData)
                .addOnSuccessListener { Snackbar.make(requireView(), "Pet saved!", Snackbar.LENGTH_LONG).show() }
        } else {

            db.collection("pets").document(petId!!)
                .update(petData as Map<String, Any>)
                .addOnSuccessListener { Snackbar.make(requireView(), "Pet updated!", Snackbar.LENGTH_LONG).show() }
        }
    }


    private fun generateUniquePetId(): String {
        val timestamp = System.currentTimeMillis().toString()
        val randomPart = (1000..9999).random().toString()
        return "PET-$timestamp-$randomPart"
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).setType("image/*")
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.data
            petImage.setImageURI(imageUri) // Preview selected image
        }
    }

    private fun uploadImageToCloudinary(uri: Uri, onSuccess: () -> Unit) {
        val executor = Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                val file = getFileFromUri(uri)
                val uploadResult = cloudinary.uploader().upload(file.absolutePath, ObjectUtils.emptyMap())
                val url = uploadResult["secure_url"] as String
                requireActivity().runOnUiThread {
                    Glide.with(this).load(url).into(petImage)
                    uploadedImageUrl = url
                    Snackbar.make(requireView(), "Image uploaded successfully!", Snackbar.LENGTH_LONG).show()
                    onSuccess() // Now save pet profile after image upload
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Snackbar.make(requireView(), "Image upload failed: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileFromUri(uri: Uri): File {
        val fileName = getFileName(uri)
        val file = File(requireContext().cacheDir, fileName)
        val inputStream: InputStream? = requireContext().contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()
        return file
    }

    private fun getFileName(uri: Uri): String {
        var name = "temp_pet_image.jpg"
        val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex >= 0) name = it.getString(columnIndex)
            }
        }
        return name
    }
}
