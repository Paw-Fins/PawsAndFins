package com.example.registration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapp.R

class OrganizationDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_organization_detail, container, false)

        // Retrieve data from bundle
        val orgName = arguments?.getString("orgName")
        val contactPerson = arguments?.getString("contactPerson")
        val email = arguments?.getString("email")
        val phoneNumber = arguments?.getString("phoneNumber")
        val address = arguments?.getString("address")
        val website = arguments?.getString("website") ?: "Not Provided"
        val description = arguments?.getString("description")
        val orgId = arguments?.getString("orgId") ?: "N/A" // Handle null case

        // Set values in TextViews
        view.findViewById<TextView>(R.id.displayOrgName).text = orgName
        view.findViewById<TextView>(R.id.displayContactPerson).text = contactPerson
        view.findViewById<TextView>(R.id.displayEmail).text = email
        view.findViewById<TextView>(R.id.displayPhoneNumber).text = phoneNumber
        view.findViewById<TextView>(R.id.displayAddress).text = address
        view.findViewById<TextView>(R.id.displayWebsite).text = website
        view.findViewById<TextView>(R.id.displayDescription).text = description
        view.findViewById<TextView>(R.id.tvorgId).text = orgId // Display org ID

        // Back button
        val backButton = view.findViewById<Button>(R.id.backButton)
        backButton.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return view
    }
}
