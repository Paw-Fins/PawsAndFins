package com.example.myapp

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.registration.OrganizationDetailFragment

class OrganizationRegistrationFragment : Fragment() {

    private lateinit var orgName: EditText
    private lateinit var contactPerson: EditText
    private lateinit var email: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var address: EditText
    private lateinit var regNumber: EditText
    private lateinit var website: EditText
    private lateinit var description: EditText
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_provider_organisation_register, container, false)

        // Initialize UI elements
        orgName = view.findViewById(R.id.orgName)
        contactPerson = view.findViewById(R.id.contactPerson)
        email = view.findViewById(R.id.email)
        phoneNumber = view.findViewById(R.id.phoneNumber)
        address = view.findViewById(R.id.address)
        regNumber = view.findViewById(R.id.regNumber)
        website = view.findViewById(R.id.website)
        description = view.findViewById(R.id.description)
        registerButton = view.findViewById(R.id.registerButton)

        registerButton.setOnClickListener {
            if (validateInput()) {
                val bundle = Bundle().apply {
                    putString("orgName", orgName.text.toString().trim())
                    putString("contactPerson", contactPerson.text.toString().trim())
                    putString("email", email.text.toString().trim())
                    putString("phoneNumber", phoneNumber.text.toString().trim())
                    putString("address", address.text.toString().trim())
                    putString("website", website.text.toString().trim())
                    putString("description", description.text.toString().trim())
                }

                val detailFragment = OrganizationDetailFragment()
                detailFragment.arguments = bundle

                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        return view
    }

    private fun validateInput(): Boolean {
        if (TextUtils.isEmpty(orgName.text.toString().trim())) {
            orgName.error = "Organization Name is required"
            return false
        }
        if (TextUtils.isEmpty(contactPerson.text.toString().trim())) {
            contactPerson.error = "Contact Person is required"
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email.text.toString().trim()).matches()) {
            email.error = "Enter a valid email"
            return false
        }
        if (!Patterns.PHONE.matcher(phoneNumber.text.toString().trim()).matches()) {
            phoneNumber.error = "Enter a valid phone number"
            return false
        }
        if (TextUtils.isEmpty(address.text.toString().trim())) {
            address.error = "Address is required"
            return false
        }
        if (!TextUtils.isEmpty(website.text.toString().trim()) &&
            !Patterns.WEB_URL.matcher(website.text.toString().trim()).matches()) {
            website.error = "Enter a valid website URL"
            return false
        }
        if (TextUtils.isEmpty(description.text.toString().trim())) {
            description.error = "Description is required"
            return false
        }

        return true
    }
}
