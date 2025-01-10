import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapp.R

class VetAppointmentFragment : Fragment(R.layout.fragment_user_booking_vet) {

    private lateinit var petName: EditText
    private lateinit var petAge: EditText
    private lateinit var petBreed: EditText
    private lateinit var petGender: EditText
    private lateinit var appointmentDate: EditText
    private lateinit var customerPhoneNumber: EditText
    private lateinit var parentName: EditText
    private lateinit var timeSlot: EditText
    private lateinit var problemDescription: EditText
    private lateinit var submitButton: Button

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the EditTexts and Button
        petName = view.findViewById(R.id.petName)
        petAge = view.findViewById(R.id.petAge)
        petBreed = view.findViewById(R.id.petBreed)
        petGender = view.findViewById(R.id.petGender)
        appointmentDate = view.findViewById(R.id.appointmentDate)
        customerPhoneNumber = view.findViewById(R.id.customerPhoneNumber)
        parentName = view.findViewById(R.id.parentName)
        timeSlot = view.findViewById(R.id.timeSlot)
        problemDescription = view.findViewById(R.id.problemDescription)
        submitButton = view.findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            submitAppointment()
        }
    }

    private fun submitAppointment() {
        // Get input data from the form
        val petNameText = petName.text.toString()
        val petAgeText = petAge.text.toString()
        val petBreedText = petBreed.text.toString()
        val petGenderText = petGender.text.toString()
        val appointmentDateText = appointmentDate.text.toString()
        val customerPhoneNumberText = customerPhoneNumber.text.toString()
        val parentNameText = parentName.text.toString()
        val timeSlotText = timeSlot.text.toString()
        val problemDescriptionText = problemDescription.text.toString()

        // Validate that no fields are empty
        if (petNameText.isEmpty() || petAgeText.isEmpty() || petBreedText.isEmpty() ||
            petGenderText.isEmpty() || appointmentDateText.isEmpty() || customerPhoneNumberText.isEmpty() ||
            parentNameText.isEmpty() || timeSlotText.isEmpty() || problemDescriptionText.isEmpty()) {
            Toast.makeText(context, "All fields are required", Toast.LENGTH_SHORT).show()
            return
        }

        // Get the current user ID and vet ID (for example, pass the vet ID from another fragment)
        val userId = auth.currentUser?.uid
        val vetId = arguments?.getString("vetId") // Assuming you pass the vet ID when navigating to this fragment

        if (userId != null && vetId != null) {
            val appointmentData = hashMapOf(
                "userId" to userId,
                "serviceId" to vetId,
                "petName" to petNameText,
                "petAge" to petAgeText,
                "petBreed" to petBreedText,
                "petGender" to petGenderText,
                "appointmentDate" to appointmentDateText,
                "customerPhoneNumber" to customerPhoneNumberText,
                "parentName" to parentNameText,
                "timeSlot" to timeSlotText,
                "problemDesc" to problemDescriptionText
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
            Toast.makeText(context, "User or vet ID is missing", Toast.LENGTH_SHORT).show()
        }
    }
}
