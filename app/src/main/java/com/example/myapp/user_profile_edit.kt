import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.*

class EditUserDetailFragment : Fragment() {

    private lateinit var nameEditText: EditText
    private lateinit var emailTextView: TextView
    private lateinit var phoneNumEditText: EditText
    private lateinit var petNameEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var uploadImageButton: Button
    private lateinit var imagePreview: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var cloudinary: Cloudinary
    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_profile_edit, container, false)

        // Initialize views
        nameEditText = rootView.findViewById(R.id.edit_user_name)
        emailTextView = rootView.findViewById(R.id.user_email)
        phoneNumEditText = rootView.findViewById(R.id.edit_user_phoneNum)
        petNameEditText = rootView.findViewById(R.id.edit_pet_name)
        saveButton = rootView.findViewById(R.id.save_button)
        uploadImageButton = rootView.findViewById(R.id.upload_image_button)
        imagePreview = rootView.findViewById(R.id.image_preview)

        // Initialize Firebase and Cloudinary
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        initCloudinary()

        // Fetch user data and populate fields
        fetchCurrentUserData()

        // Set up listeners
        saveButton.setOnClickListener { saveUserData() }
        uploadImageButton.setOnClickListener { openImagePicker() }

        return rootView
    }

    private fun initCloudinary() {
        cloudinary = Cloudinary(
            mapOf(
                "cloud_name" to getString(R.string.cloud_name),
                "api_key" to getString(R.string.api_key),
                "api_secret" to getString(R.string.api_secret)
            )
        )
    }

    private fun fetchCurrentUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userRef = firestore.collection("users").document(user.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        nameEditText.setText(document.getString("name") ?: "")
                        emailTextView.text = user.email
                        phoneNumEditText.setText(document.getString("mobile") ?: "")
                        petNameEditText.setText(document.getString("petName") ?: "")
                        val imageUrl = document.getString("imageUrl")
                        imageUrl?.let {
                            Glide.with(this).load(it).into(imagePreview)
                        }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error loading user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val userRef = firestore.collection("users").document(user.uid)
            val updatedData = mutableMapOf<String, Any>()

            if (nameEditText.text.isNotEmpty()) updatedData["name"] = nameEditText.text.toString()
            if (phoneNumEditText.text.isNotEmpty()) updatedData["mobile"] = phoneNumEditText.text.toString()
            if (petNameEditText.text.isNotEmpty()) updatedData["petName"] = petNameEditText.text.toString()

            if (updatedData.isEmpty() && selectedImageUri == null) {
                Toast.makeText(requireContext(), "No changes to save", Toast.LENGTH_SHORT).show()
                return
            }

            userRef.update(updatedData)
                .addOnSuccessListener {
                    selectedImageUri?.let {
                        uploadImageToCloudinary(user.uid)
                    } ?: Toast.makeText(requireContext(), "Data updated successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error updating data", Toast.LENGTH_SHORT).show()
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
                            cloudinary.uploader().upload(it, ObjectUtils.emptyMap())
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

    private fun saveImageUrlToFirestore(userId: String, imageUrl: String) {
        firestore.collection("users").document(userId)
            .update("imageUrl", imageUrl)
            .addOnSuccessListener {
                showToast("Image updated successfully")
                Glide.with(this).load(imageUrl).into(imagePreview)
            }
            .addOnFailureListener {
                showToast("Failed to update image URL")
            }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK).apply { type = "image/*" }
        startActivityForResult(intent, IMAGE_PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_CODE && resultCode == Activity.RESULT_OK) {
            selectedImageUri = data?.data
            imagePreview.setImageURI(selectedImageUri)
        }
    }

    private fun getFileFromUri(uri: Uri): String? {
        val filePathColumn = arrayOf(android.provider.MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.use {
            it.moveToFirst()
            val columnIndex = it.getColumnIndex(filePathColumn[0])
            return it.getString(columnIndex)
        }
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val IMAGE_PICK_CODE = 1001
    }
}
