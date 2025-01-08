package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class FragmentUserBookingVet : Fragment() {

    private lateinit var petNameEditText: EditText
    private lateinit var petAgeEditText: EditText
    private lateinit var petBreedEditText: EditText
    private lateinit var petGenderEditText: EditText
    private lateinit var appointmentDateEditText: EditText
    private lateinit var customerPhoneNumberEditText: EditText
    private lateinit var submitButton: Button

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_booking_vet, container, false)

        // Initialize the UI components
        petNameEditText = view.findViewById(R.id.petName)
        petAgeEditText = view.findViewById(R.id.petAge)
        petBreedEditText = view.findViewById(R.id.petBreed)
        petGenderEditText = view.findViewById(R.id.petGender)
        appointmentDateEditText = view.findViewById(R.id.appointmentDate)
        customerPhoneNumberEditText = view.findViewById(R.id.customerPhoneNumber)
        submitButton = view.findViewById(R.id.submitButton)

        // Set up the submit button click listener
        submitButton.setOnClickListener {
            submitAppointment()
        }

        return view
    }

    private fun submitAppointment() {
        // Get the input data from the form
        val petName = petNameEditText.text.toString().trim()
        val petAge = petAgeEditText.text.toString().trim()
        val petBreed = petBreedEditText.text.toString().trim()
        val petGender = petGenderEditText.text.toString().trim()
        val appointmentDate = appointmentDateEditText.text.toString().trim()
        val customerPhoneNumber = customerPhoneNumberEditText.text.toString().trim()

        // Validate the form inputs
        if (petName.isEmpty() || petAge.isEmpty() || petBreed.isEmpty() ||
            petGender.isEmpty() || appointmentDate.isEmpty() || customerPhoneNumber.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        // Create a map of the appointment data
        val appointmentData = hashMapOf(
            "petName" to petName,
            "petAge" to petAge,
            "petBreed" to petBreed,
            "petGender" to petGender,
            "appointmentDate" to appointmentDate,
            "customerPhoneNumber" to customerPhoneNumber
        )

        // Save the appointment data to Firebase Firestore
        firestore.collection("appointments")
            .add(appointmentData)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Appointment submitted successfully!", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error submitting appointment: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        petNameEditText.text.clear()
        petAgeEditText.text.clear()
        petBreedEditText.text.clear()
        petGenderEditText.text.clear()
        appointmentDateEditText.text.clear()
        customerPhoneNumberEditText.text.clear()
    }
}
