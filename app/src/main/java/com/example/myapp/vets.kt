package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

// Data class to represent an NGO
data class vet(
    val id: String,
    val name: String,
    val address: String,
    val ownerName: String
)

class VetFragment : Fragment() {

    private lateinit var ngoContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_vets, container, false)

        ngoContainer = view.findViewById(R.id.vetContainer)

        // Create a list of NGOs
        val vets = listOf(
            vet("1", "Dr. Smith", "123 Vet Lane, City, Country", "John Doe"),
            vet("2", "Dr. Johnson", "456 Animal Ave, City, Country", "Jane Smith"),
            vet("3", "Dr. Brown", "789 Pet Rd, City, Country", "Alice Johnson"),
            vet("4", "Dr. Taylor", "321 Care St, City, Country", "Bob Brown")
        )

        // Populate the LinearLayout with NGO data
        for (vet in vets) {
            addVetsView(vet)
        }

        return view
    }

    private fun addVetsView(vet: vet) {
        val ngoView = LayoutInflater.from(context).inflate(R.layout.service_card, ngoContainer, false)

        val serviceNameTextView: TextView = ngoView.findViewById(R.id.serviceName)
        val serviceAddressTextView: TextView = ngoView.findViewById(R.id.serviceAddress)
        val serviceOwnerNameTextView: TextView = ngoView.findViewById(R.id.serviceOwnerName)
        val contactButton: Button = ngoView.findViewById(R.id.buyNowButton)

        serviceNameTextView.text = vet.name
        serviceAddressTextView.text = vet.address
        serviceOwnerNameTextView.text = vet.ownerName

        contactButton.setOnClickListener {
            // Handle contact action here
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