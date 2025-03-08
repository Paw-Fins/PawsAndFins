import androidx.fragment.app.Fragment
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.example.myapp.R
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

    // Register activity result launcher for image picker
    private lateinit var getImageResult: ActivityResultLauncher<Intent>

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
        context?.let { ctx ->
            ArrayAdapter.createFromResource(
                ctx,
                R.array.filter_options, // Load values from strings.xml
                android.R.layout.simple_spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter
            }
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

        // Register for image result
        getImageResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                data?.data?.let { uri ->
                    imageUri = uri
                    context?.let { ctx -> Glide.with(ctx).load(uri).into(productImagePreview) }
                }
            }
        }

        uploadImageButton.setOnClickListener { openImageChooser() }
        submitProductButton.setOnClickListener { submitProduct() }

        return view
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        getImageResult.launch(intent)
    }

    private fun submitProduct() {
        val name = productName.text.toString().trim()
        val price = productPrice.text.toString().trim()
        val description = productDescription.text.toString().trim()

        if (name.isEmpty() || price.isEmpty() || description.isEmpty() || imageUri == null || selectedCategory.isNullOrEmpty()) {
            context?.let { ctx -> Toast.makeText(ctx, "Please fill all fields and select a category.", Toast.LENGTH_SHORT).show() }
            return
        }

        imageUri?.let {
            uploadImageToCloudinary(it)
        }
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
                context?.let { ctx -> Toast.makeText(ctx, "Product added successfully!", Toast.LENGTH_SHORT).show() }
                clearFields()
            }
            .addOnFailureListener { e ->
                context?.let { ctx -> Toast.makeText(ctx, "Error adding product: ${e.message}", Toast.LENGTH_SHORT).show() }
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
        context?.let { ctx -> Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show() }
    }
}
