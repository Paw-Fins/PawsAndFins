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

class TrainerSignUpFragment : Fragment() {

    private lateinit var etExperience: EditText
    private lateinit var etAvailability: EditText
    private lateinit var etPhysicalAddress: EditText
    private lateinit var btnTrainerSignup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.trainer_signup, container, false)

        // Initialize views
        etExperience = rootView.findViewById(R.id.etExperience)
        etAvailability = rootView.findViewById(R.id.etAvailability)
        etPhysicalAddress = rootView.findViewById(R.id.etPhysicalAddress)
        btnTrainerSignup = rootView.findViewById(R.id.btnTrainerSignup)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up the Sign Up button click listener
        btnTrainerSignup.setOnClickListener { signUpTrainer() }

        return rootView
    }

    private fun signUpTrainer() {
        val experience = etExperience.text.toString()
        val availability = etAvailability.text.toString()
        val physicalAddress = etPhysicalAddress.text.toString()

        // Validate input data
        if (experience.isEmpty() || availability.isEmpty() || physicalAddress.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user (already authenticated)
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            // Create a map with trainer details
            val trainerData = hashMapOf(
                "experience" to experience,
                "availability" to availability,
                "physicalAddress" to physicalAddress
            )

            // Save the trainer data to Firestore under the "users" collection
            firestore.collection("users").document(user.uid)
                .update(trainerData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Trainer signed up successfully", Toast.LENGTH_SHORT).show()
                    navigateToImage()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error signing up trainer", Toast.LENGTH_SHORT).show()
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
