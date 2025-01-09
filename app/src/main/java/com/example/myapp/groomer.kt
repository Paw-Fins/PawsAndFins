package com.example.myapp

import VetContactFragment
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

// Data class to represent a Groomer
data class Groomer(
    val id: String,
    val shopName: String,
    val location: String,
    val ownerName: String,
    val availabilityTime: String
)

class GroomerFragment : Fragment() {

    private lateinit var groomerContainer: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_groomer, container, false)

        groomerContainer = view.findViewById(R.id.groomerContainer)

        // Fetch data from Firestore
        fetchGroomersData()

        return view
    }

    private fun fetchGroomersData() {
        firestore.collection("users")
            .whereEqualTo("role", "Groomer") // Filter documents by role "Groomer"
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No groomers found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val id = document.id
                        val shopName = document.getString("shopName") ?: "Unknown"
                        val location = document.getString("location") ?: "Not Available"
                        val ownerName = document.getString("name") ?: "Unknown"
                        val availabilityTime = document.getString("availabilityTime") ?: "Not Available"

                        // Add each groomer data to the view
                        addGroomerView(Groomer(id, shopName, location, ownerName, availabilityTime))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addGroomerView(groomer: Groomer) {
        val groomerView = LayoutInflater.from(context).inflate(R.layout.service_card, groomerContainer, false)

        val shopNameTextView: TextView = groomerView.findViewById(R.id.serviceName)
        val locationTextView: TextView = groomerView.findViewById(R.id.serviceAddress)
        val ownerNameTextView: TextView = groomerView.findViewById(R.id.serviceOwnerName)
        val availabilityTextView: TextView = groomerView.findViewById(R.id.available)
        val contactButton: Button = groomerView.findViewById(R.id.buyNowButton)

        shopNameTextView.text = groomer.shopName
        locationTextView.text = groomer.location
        ownerNameTextView.text = groomer.ownerName
        availabilityTextView.text = groomer.availabilityTime

        // Ensure "Call" option is always visible and functional
        contactButton.text = "Call Groomer"
        contactButton.setOnClickListener {
            // Show the detailed contact page in a BottomSheet
            val contactFragment = VetContactFragment()
            contactFragment.show(childFragmentManager, contactFragment.tag)
        }

        // Set layout parameters to add top margin
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0) // Set top margin to 16dp
        groomerView.layoutParams = params

        // Add the inflated view to the LinearLayout
        groomerContainer.addView(groomerView)
    }
}
