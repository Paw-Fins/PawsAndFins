import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.myapp.ImageUploadFragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class NGOSignUpFragment : Fragment() {

    private lateinit var etEstablishmentDate: EditText
    private lateinit var etNGOName: EditText
    private lateinit var etAddress: EditText
    private lateinit var etWebsiteURL: EditText
    private lateinit var btnNGOSignup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.ngo_signup, container, false)

        // Initialize views
        etEstablishmentDate = rootView.findViewById(R.id.etEstablishmentDate)
        etNGOName = rootView.findViewById(R.id.etNGOName)
        etAddress = rootView.findViewById(R.id.etAddress)
        etWebsiteURL = rootView.findViewById(R.id.etWebsiteURL)
        btnNGOSignup = rootView.findViewById(R.id.btnNGOSignup)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up the Sign Up button click listener
        btnNGOSignup.setOnClickListener { signUpNGO() }

        return rootView
    }

    private fun signUpNGO() {
        val establishmentDate = etEstablishmentDate.text.toString()
        val ngoName = etNGOName.text.toString()
        val address = etAddress.text.toString()
        val websiteURL = etWebsiteURL.text.toString()

        // Validate input data
        if (establishmentDate.isEmpty() || ngoName.isEmpty() || address.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user (already authenticated)
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            // Create a map with NGO details
            val ngoData = hashMapOf(
                "establishmentDate" to establishmentDate,
                "ngoName" to ngoName,
                "address" to address,
                "websiteURL" to websiteURL
            )

            // Save the NGO data to Firestore under the "users" collection
            firestore.collection("users").document(user.uid)
                .update(ngoData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "NGO signed up successfully", Toast.LENGTH_SHORT).show()
                    navigateToImage()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error signing up NGO", Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            Toast.makeText(requireContext(), "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToImage() {
        val imageUploadFragment = ImageUploadFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, imageUploadFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
