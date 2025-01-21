package com.example.myapp

import VetContactFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent a Vet
data class Vet(
    val id: String,
    val name: String,
    val address: String,
    val ownerName: String,
    val available: String,
    val specialty: String,
    val contact: String,
    val services: String,
    val isEmergencyAvailable: Boolean
)

class VetFragment : Fragment() {

    private lateinit var vetContainer: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vets, container, false)

        vetContainer = view.findViewById(R.id.vetContainer)

        // Fetch data from Firestore
        fetchVetsData()

        return view
    }

    private fun fetchVetsData() {
        firestore.collection("users")
            .whereEqualTo("role", "Doctor") // Filter documents by role "Doctor"
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No Vets", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val id = document.id
                        val name = document.getString("clinicName") ?: ""
                        val address = document.getString("clinicAddress") ?: ""
                        val ownerName = document.getString("name") ?: ""
                        val available = document.getString("availabilityTime") ?: ""
                        val specialty = document.getString("specialty") ?: ""
                        val contact = document.getString("mobile") ?: ""
                        val services = document.getString("services") ?: ""
                        val isEmergencyAvailable = document.getBoolean("isEmergencyAvailable") ?: false
                        // Add each vet data to the view
                        addVetView(Vet(id, name, address, ownerName, available, specialty, contact, services, isEmergencyAvailable))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addVetView(vet: Vet) {
        val vetView = LayoutInflater.from(context).inflate(R.layout.service_card, vetContainer, false)

        val serviceNameTextView: TextView = vetView.findViewById(R.id.serviceName)
        val serviceAddressTextView: TextView = vetView.findViewById(R.id.serviceAddress)
        val serviceOwnerNameTextView: TextView = vetView.findViewById(R.id.serviceOwnerName)
        val availableTextView: TextView = vetView.findViewById(R.id.available)
        val contactButton: Button = vetView.findViewById(R.id.buyNowButton)

        serviceNameTextView.text = vet.name
        serviceAddressTextView.text = vet.address
        serviceOwnerNameTextView.text = vet.ownerName
        availableTextView.text = vet.available

        // Handle contact button click
        contactButton.setOnClickListener {
            navigateToContact(vet)
        }

        // Set layout parameters to add top margin
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0) // Set top margin to 16dp
        vetView.layoutParams = params

        // Add the inflated view to the LinearLayout
        vetContainer.addView(vetView)
    }

    private fun navigateToContact(vet: Vet) {
        val homeScreenFragment = VetContactFragment()
        val bundle = Bundle().apply {
            putString("vetId", vet.id)
            putString("doctorName", vet.name)
            putString("doctorSpecialty", vet.specialty)  // Passing specialty
            putString("doctorContact", vet.contact)  // Passing contact info
            putString("doctorAddress", vet.address)
            putString("doctorServices", vet.services)  // Passing services info
            putBoolean("isEmergencyAvailable", vet.isEmergencyAvailable)
        }

        homeScreenFragment.arguments = bundle

        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, homeScreenFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
