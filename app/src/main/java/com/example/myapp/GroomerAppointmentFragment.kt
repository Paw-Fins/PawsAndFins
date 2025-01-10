import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapp.R

class GroomerAppointmentFragment : Fragment(R.layout.user_booking_groomer) {

    private lateinit var petName: EditText
    private lateinit var petBreed: EditText
    private lateinit var groomingService: EditText
    private lateinit var appointmentDate: EditText
    private lateinit var timeSlot: EditText
    private lateinit var customerPhoneNumber: EditText
    private lateinit var parentName: EditText
    private lateinit var additionalInstructions: EditText
    private lateinit var submitButton: Button

    private lateinit var groomerId: String

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the EditTexts and Button
        petName = view.findViewById(R.id.petName)
        petBreed = view.findViewById(R.id.petBreed)
        groomingService = view.findViewById(R.id.groomingService)
        appointmentDate = view.findViewById(R.id.appointmentDate)
        timeSlot = view.findViewById(R.id.timeSlot)
        customerPhoneNumber = view.findViewById(R.id.customerPhoneNumber)
        parentName = view.findViewById(R.id.txtParentName)
        additionalInstructions = view.findViewById(R.id.additionalInstructions)
        submitButton = view.findViewById(R.id.submitButton)

        // Get the groomerId passed from GroomerContactFragment
        arguments?.let {
            groomerId = it.getString("groomerId") ?: "" // Retrieve the groomerId
        }

        submitButton.setOnClickListener {
            submitAppointment()
        }
    }

    private fun submitAppointment() {
        // Get input data from the form
        val petNameText = petName.text.toString()
        val petBreedText = petBreed.text.toString()
        val groomingServiceText = groomingService.text.toString()
        val appointmentDateText = appointmentDate.text.toString()
        val timeSlotText = timeSlot.text.toString()
        val customerPhoneNumberText = customerPhoneNumber.text.toString()
        val parentNameText = parentName.text.toString()
        val additionalInstructionsText = additionalInstructions.text.toString()

        // Validate that no fields are empty
        if (petNameText.isEmpty() || petBreedText.isEmpty() || groomingServiceText.isEmpty() ||
            appointmentDateText.isEmpty() || timeSlotText.isEmpty() || customerPhoneNumberText.isEmpty() ||
            parentNameText.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user ID (service ID is now the groomerId)
        val userId = auth.currentUser?.uid

        if (userId != null && groomerId.isNotEmpty()) {
            val appointmentData = hashMapOf(
                "serviceId" to groomerId,  // Store the groomerId as serviceId
                "petName" to petNameText,
                "petBreed" to petBreedText,
                "groomingService" to groomingServiceText,
                "appointmentDate" to appointmentDateText,
                "timeSlot" to timeSlotText,
                "customerPhoneNumber" to customerPhoneNumberText,
                "parentName" to parentNameText,
                "additionalInstructions" to additionalInstructionsText
            )

            // Store the appointment data in Firestore
            firestore.collection("appointments")
                .add(appointmentData)
                .addOnSuccessListener {
                    Toast.makeText(context, "Appointment booked successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(context, "Error booking appointment: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "User ID or Groomer ID is missing", Toast.LENGTH_SHORT).show()
        }
    }
}
