package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapp.R

class NgoAppointmentFragment : Fragment() {

    private lateinit var ngoId: String
    private lateinit var submitButton: Button
    private lateinit var ngoNameEditText: EditText
    private lateinit var appointmentPurposeEditText: EditText
    private lateinit var contactNumberEditText: EditText
    private lateinit var emailAddressEditText: EditText
    private lateinit var additionalInstructionsEditText: EditText

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_book_ngovisit, container, false)

        // Initialize views
        ngoNameEditText = view.findViewById(R.id.ngoName)
        appointmentPurposeEditText = view.findViewById(R.id.appointmentPurpose)
        contactNumberEditText = view.findViewById(R.id.contactNumber)
        emailAddressEditText = view.findViewById(R.id.emailAddress)
        additionalInstructionsEditText = view.findViewById(R.id.additionalInstructions)
        submitButton = view.findViewById(R.id.submitButton)

        // Get the NGO ID passed from the previous fragment
        arguments?.let {
            ngoId = it.getString("ngoId") ?: ""
        }

        // Handle the submit button click
        submitButton.setOnClickListener {
            submitAppointment()
        }

        return view
    }

    private fun submitAppointment() {
        val userId = auth.currentUser?.uid // Get the current logged-in user's ID
        val ngoName = ngoNameEditText.text.toString().trim()
        val appointmentPurpose = appointmentPurposeEditText.text.toString().trim()
        val contactNumber = contactNumberEditText.text.toString().trim()
        val emailAddress = emailAddressEditText.text.toString().trim()
        val additionalInstructions = additionalInstructionsEditText.text.toString().trim()

        if (ngoName.isEmpty() || appointmentPurpose.isEmpty() || contactNumber.isEmpty() ||
            emailAddress.isEmpty() || additionalInstructions.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map to save the appointment data
        val appointmentData = hashMapOf(
            "userId" to userId,
            "ngoId" to ngoId,
            "ngoName" to ngoName,
            "appointmentPurpose" to appointmentPurpose,
            "contactNumber" to contactNumber,
            "emailAddress" to emailAddress,
            "additionalInstructions" to additionalInstructions,
            "timestamp" to System.currentTimeMillis()
        )

        // Save the data to Firestore in the appointments collection
        db.collection("appointments")
            .add(appointmentData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Appointment submitted successfully", Toast.LENGTH_SHORT).show()
                // Optionally, navigate back to the previous screen or show a confirmation message
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
