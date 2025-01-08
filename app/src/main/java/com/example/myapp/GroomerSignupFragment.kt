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

class GroomerSignupFragment : Fragment() {

    private lateinit var etLocation: EditText
    private lateinit var etAvailabilityTime: EditText
    private lateinit var etShopName: EditText
    private lateinit var btnGroomerSignup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.groomer_signup, container, false)

        // Initialize views
        etLocation = rootView.findViewById(R.id.etLocation)
        etAvailabilityTime = rootView.findViewById(R.id.etAvailabilityTime)
        etShopName = rootView.findViewById(R.id.etShopName)
        btnGroomerSignup = rootView.findViewById(R.id.btnGroomerSignup)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up the Sign Up button click listener
        btnGroomerSignup.setOnClickListener { signUpGroomer() }

        return rootView
    }

    private fun signUpGroomer() {
        val location = etLocation.text.toString()
        val availabilityTime = etAvailabilityTime.text.toString()
        val shopName = etShopName.text.toString()

        // Validate input data
        if (location.isEmpty() || availabilityTime.isEmpty() || shopName.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user (already authenticated)
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            // Create a map with groomer details
            val groomerData = hashMapOf(
                "location" to location,
                "availabilityTime" to availabilityTime,
                "shopName" to shopName
            )

            // Save the groomer data to Firestore under the "users" collection
            firestore.collection("users").document(user.uid)
                .update(groomerData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Groomer signed up successfully", Toast.LENGTH_SHORT).show()
                    navigateToImage()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error signing up groomer", Toast.LENGTH_SHORT).show()
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