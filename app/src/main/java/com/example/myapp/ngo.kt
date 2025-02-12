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

// Data class to represent an NGO
data class NGO(
    val id: String,
    val name: String,
    val address: String,
    val ownerName: String
)

class NGOFragment : Fragment() {

    private lateinit var ngoContainer: LinearLayout
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_ngo, container, false)

        ngoContainer = view.findViewById(R.id.ngoContainer)

        // Fetch data from Firestore
        fetchNGOData()

        return view
    }

    private fun fetchNGOData() {
        firestore.collection("users")
            .whereEqualTo("role", "NGO Manager") // Filter documents by role "NGO"
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(requireContext(), "No NGOs found.", Toast.LENGTH_SHORT).show()
                } else {
                    for (document in result) {
                        val id = document.id
                        val name = document.getString("name") ?: "Unknown"
                        val address = document.getString("address") ?: "Not Available"
                        val ownerName = document.getString("ngoName") ?: "Unknown"

                        // Add each NGO data to the view
                        addNGOView(NGO(id, name, address, ownerName))
                    }
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Error fetching data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addNGOView(ngo: NGO) {
        val ngoView = LayoutInflater.from(context).inflate(R.layout.service_card, ngoContainer, false)

        val serviceNameTextView: TextView = ngoView.findViewById(R.id.serviceName)
        val serviceAddressTextView: TextView = ngoView.findViewById(R.id.serviceAddress)
        val serviceOwnerNameTextView: TextView = ngoView.findViewById(R.id.serviceOwnerName)
        val contactButton: Button = ngoView.findViewById(R.id.buyNowButton)

        serviceNameTextView.text = ngo.name
        serviceAddressTextView.text = ngo.address
        serviceOwnerNameTextView.text = ngo.ownerName

        // Ensure "Contact NGO" option is always visible and functional
        contactButton.text = "Contact NGO"
        contactButton.setOnClickListener {

        }

        // Set layout parameters to add top margin
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0) // Set top margin to 16dp
        ngoView.layoutParams = params

        ngoContainer.addView(ngoView)
    }
}
