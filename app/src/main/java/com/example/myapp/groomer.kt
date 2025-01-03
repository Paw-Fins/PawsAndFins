package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class GroomerFragment : Fragment() {

    private lateinit var groomerContainer: LinearLayout

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

        // Create a list of groomers
        val groomers = listOf(
            Groomer("1", "Grooming Galore", "123 Groomer St, City, Country", "Alice"),
            Groomer("2", "Paws & Claws", "456 Pet Grooming Ave, City, Country", "Bob"),
            Groomer("3", "Furry Friends Grooming", "789 Animal Care Rd, City, Country", "Charlie"),
            Groomer("4", "The Dog Spa", "321 Canine Blvd, City, Country", "Diana")
        )

        // Populate the LinearLayout with groomer data
        for (groomer in groomers) {
            addGroomerView(groomer)
        }

        return view
    }

    private fun addGroomerView(groomer: Groomer) {
        val groomerView = LayoutInflater.from(context).inflate(R.layout.service_card, groomerContainer, false)

        val serviceNameTextView: TextView = groomerView.findViewById(R.id.serviceName)
        val serviceAddressTextView: TextView = groomerView.findViewById(R.id.serviceAddress)
        val serviceOwnerNameTextView: TextView = groomerView.findViewById(R.id.serviceOwnerName)
        val contactButton: Button = groomerView.findViewById(R.id.buyNowButton)

        serviceNameTextView.text = groomer.name
        serviceAddressTextView.text = groomer.address
        serviceOwnerNameTextView.text = groomer.ownerName

        contactButton.setOnClickListener {
            // Handle contact action here
        }

        // Set layout parameters to add top margin
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(0, 16, 0, 0) // Set top margin to 16dp
        groomerView.layoutParams = params

        groomerContainer.addView(groomerView)
    }
}
data class Groomer(
    val id: String,
    val name: String,
    val address: String,
    val ownerName: String
)