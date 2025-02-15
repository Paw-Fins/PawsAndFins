package com.example.myapp

import VetContactFragment
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

// Data class to represent a Trainer
data class Trainer(
    val id: String,
    val shopName: String,
    val location: String,
    val ownerName: String,
    val availabilityTime: String,
    val contact: String
)

class TrainerFragment : Fragment() {

    private lateinit var trainerContainer: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_trainer, container, false)

        trainerContainer = view.findViewById(R.id.trainerContainer)

        // Fetch data from Firestore
        fetchTrainerData()

        return view
    }

    private fun fetchTrainerData() {
        firestore.collection("users")
            .whereEqualTo("role", "Trainer") // Filter documents by role "Trainer"
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No trainers found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val id = document.id
                        val shopName = document.getString("name") ?: "Unknown"
                        val location = document.getString("physicalAddress") ?: "Not Available"
                        val ownerName = document.getString("email") ?: "Unknown"
                        val availabilityTime = document.getString("availability") ?: "Not Available"
                        val contact = document.getString("mobile") ?: "Not Available"

                        // Add each trainer data to the view
                        addTrainerView(Trainer(id, shopName, location, ownerName, availabilityTime, contact))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addTrainerView(trainer: Trainer) {
        val trainerView = LayoutInflater.from(context).inflate(R.layout.service_card, trainerContainer, false)

        val shopNameTextView: TextView = trainerView.findViewById(R.id.serviceName)
        val locationTextView: TextView = trainerView.findViewById(R.id.serviceAddress)
        val ownerNameTextView: TextView = trainerView.findViewById(R.id.serviceOwnerName)
        val availabilityTextView: TextView = trainerView.findViewById(R.id.available)
        val contactButton: Button = trainerView.findViewById(R.id.buyNowButton)
        val contactNo = trainer.contact

        shopNameTextView.text = trainer.shopName
        locationTextView.text = trainer.location
        ownerNameTextView.text = trainer.ownerName
        availabilityTextView.text = trainer.availabilityTime

        // Ensure "Call Trainer" option is always visible and functional
        contactButton.text = "Call Trainer"
        contactButton.setOnClickListener {
            if (contactNo.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$contactNo"))
                startActivity(intent)
            } else {
                // Handle the case where there is no phone number available
                Toast.makeText(requireContext(), "No contact number available", Toast.LENGTH_SHORT).show()
            }
        }

        // Set layout parameters to add top margin
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0) // Set top margin to 16dp
        trainerView.layoutParams = params

        // Add the inflated view to the LinearLayout
        trainerContainer.addView(trainerView)

    }
}
