import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class VetContactFragment : BottomSheetDialogFragment() {

    private lateinit var ownerNameTextView: TextView
    private lateinit var addressTextView: TextView
    private lateinit var availabilityTextView: TextView
    private lateinit var callButton: Button
    private lateinit var bookAppointmentButton: Button

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for the bottom sheet
        val view = inflater.inflate(R.layout.fragment_vet_contact, container, false)

        // Initialize the views
        ownerNameTextView = view.findViewById(R.id.serviceOwnerName)
        addressTextView = view.findViewById(R.id.serviceAddress)
        availabilityTextView = view.findViewById(R.id.available)
        callButton = view.findViewById(R.id.callDoctorButton)
        bookAppointmentButton = view.findViewById(R.id.bookAppointmentButton)

        // Get the doctors from Firestore
        fetchDoctorsFromFirestore()

        return view
    }

    private fun fetchDoctorsFromFirestore() {
        // Fetch users where role is 'doctor'
        firestore.collection("users")
            .whereEqualTo("role", "Doctor")
            .get()
            .addOnSuccessListener { querySnapshot ->
                handleDoctorsResponse(querySnapshot)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun handleDoctorsResponse(querySnapshot: QuerySnapshot) {
        if (querySnapshot.isEmpty) {
            Toast.makeText(requireContext(), "No doctors found.", Toast.LENGTH_SHORT).show()
            return
        }

        // Iterate through the results and extract data
        for (document in querySnapshot.documents) {
            val ownerName = document.getString("name") ?: "Unknown"
            val address = document.getString("clinicAddress") ?: "Not Available"
            val availability = document.getString("availabilityTime") ?: "Not Available"
            val isEmergencyAvailable = document.getBoolean("isEmergencyAvailable") ?: false

            // Populate the views with data
            ownerNameTextView.text = ownerName
            addressTextView.text = address
            availabilityTextView.text = availability

            // Show or hide the "Call Doctor" button based on emergency availability
            callButton.visibility = if (isEmergencyAvailable) View.VISIBLE else View.GONE
        }
    }
}
