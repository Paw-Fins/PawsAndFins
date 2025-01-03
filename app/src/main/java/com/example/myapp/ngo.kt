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
data class NGO(
    val id: String,
    val name: String,
    val address: String,
    val ownerName: String
)

class NGOFragment : Fragment() {

    private lateinit var ngoContainer: LinearLayout

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

        // Create a list of NGOs
        val ngos = listOf(
            NGO("1", "Helping Hands", "123 NGO Street, City, Country", "John Doe"),
            NGO("2", "Care for All", "456 Charity Ave, City, Country", "Jane Smith"),
            NGO("3", "Hope Foundation", "789 Hope Rd, City, Country", "Alice Johnson"),
            NGO("4", "Community Support", "321 Community St, City, Country", "Bob Brown")
        )

        // Populate the LinearLayout with NGO data
        for (ngo in ngos) {
            addNGOView(ngo)
        }

        return view
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