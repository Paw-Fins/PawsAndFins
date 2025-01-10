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

class VetSignUpFragment : Fragment() {

    private lateinit var etExperience: EditText
    private lateinit var etAvailabilityTime: EditText
    private lateinit var etClinicAddress: EditText
    private lateinit var etClinicName: EditText
    private lateinit var etSpeciality: EditText
    private lateinit var etServices: EditText
    private lateinit var radioGroupEmergency: RadioGroup
    private lateinit var btnVetSignup: Button

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.vet_signup, container, false)

        // Initialize views
        etExperience = rootView.findViewById(R.id.etExperience)
        etAvailabilityTime = rootView.findViewById(R.id.etAvailabilityTime)
        etClinicAddress = rootView.findViewById(R.id.etClinicAddress)
        etClinicName = rootView.findViewById(R.id.etClinicName)
        etSpeciality = rootView.findViewById(R.id.etSpeciality)
        etServices = rootView.findViewById(R.id.etServices)
        radioGroupEmergency = rootView.findViewById(R.id.radioGroupEmergency)
        btnVetSignup = rootView.findViewById(R.id.btnVetSignup)

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Set up the Sign Up button click listener
        btnVetSignup.setOnClickListener { signUpVet() }

        return rootView
    }

    private fun signUpVet() {
        val experience = etExperience.text.toString()
        val availabilityTime = etAvailabilityTime.text.toString()
        val clinicAddress = etClinicAddress.text.toString()
        val clinicName = etClinicName.text.toString()
        val speciality = etSpeciality.text.toString()
        val services = etServices.text.toString()
        val isEmergencyAvailable = when (radioGroupEmergency.checkedRadioButtonId) {
            R.id.rbAvailableYes -> true
            R.id.rbAvailableNo -> false
            else -> false
        }

        // Validate input data
        if (experience.isEmpty() || availabilityTime.isEmpty() || clinicAddress.isEmpty() ||
            clinicName.isEmpty() || speciality.isEmpty() || services.isEmpty()
        ) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Get current user (already authenticated)
        val currentUser = auth.currentUser
        currentUser?.let { user ->

            // Create a map with vet details
            val vetData = hashMapOf(
                "experience" to experience,
                "availabilityTime" to availabilityTime,
                "clinicAddress" to clinicAddress,
                "clinicName" to clinicName,
                "speciality" to speciality,
                "services" to services,
                "isEmergencyAvailable" to isEmergencyAvailable
            )

            // Save the vet data to Firestore under the "users" collection
            firestore.collection("users").document(user.uid)
                .update(vetData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Vet signed up successfully", Toast.LENGTH_SHORT).show()
                    navigateToImage()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Error signing up vet", Toast.LENGTH_SHORT).show()
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
