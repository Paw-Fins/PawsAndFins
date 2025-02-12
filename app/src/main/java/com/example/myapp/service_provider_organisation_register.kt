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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class OrganizationRegistrationFragment : Fragment() {

    private lateinit var orgName: EditText
    private lateinit var contactPerson: EditText
    private lateinit var email: EditText
    private lateinit var phoneNumber: EditText
    private lateinit var address: EditText
    private lateinit var regNumber: EditText
    private lateinit var website: EditText
    private lateinit var description: EditText
    private lateinit var orgId: TextView
    private lateinit var registerButton: Button
    private lateinit var progressBar: ProgressBar

    private val db = FirebaseFirestore.getInstance()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid
    private var organizationId: String? = null // Store existing organization ID

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
        orgId = view.findViewById(R.id.tvorgIdreg)
        description = view.findViewById(R.id.description)
        registerButton = view.findViewById(R.id.registerButton)
        progressBar = view.findViewById(R.id.organizationProgressBar)

        progressBar.visibility = View.VISIBLE
        checkExistingOrganization() // Fetch existing data

        registerButton.setOnClickListener {
            if (validateInput()) {
                saveOrganizationData()
            }
        }

        return view
    }

    private fun checkExistingOrganization() {
        userId?.let {
            db.collection("organizations").whereEqualTo("userId", it).get()
                .addOnSuccessListener { documents ->
                    if (!documents.isEmpty) {
                        val doc = documents.documents.first()
                        organizationId = doc.id

                        // Autofill the fields
                        orgName.setText(doc.getString("orgName") ?: "")
                        contactPerson.setText(doc.getString("contactPerson") ?: "")
                        email.setText(doc.getString("email") ?: "")
                        phoneNumber.setText(doc.getString("phoneNumber") ?: "")
                        address.setText(doc.getString("address") ?: "")
                        regNumber.setText(doc.getString("regNumber") ?: "")
                        description.setText(doc.getString("description") ?: "")

                        // Handle optional website field
                        val websiteText = doc.getString("website")
                        website.setText(websiteText ?: "")

                        // Display the Organization ID
                        orgId.text = doc.getString("organizationId") ?: "N/A"

                        // Change button text to "Update"
                        registerButton.text = "Update Organization"
                    } else {
                        registerButton.text = "Register Organization"
                    }
                    progressBar.visibility = View.GONE
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
        }
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

    private fun saveOrganizationData() {
        val orgData = hashMapOf(
            "orgName" to orgName.text.toString().trim(),
            "contactPerson" to contactPerson.text.toString().trim(),
            "email" to email.text.toString().trim(),
            "phoneNumber" to phoneNumber.text.toString().trim(),
            "address" to address.text.toString().trim(),
            "regNumber" to regNumber.text.toString().trim(),
            "description" to description.text.toString().trim(),
            "userId" to userId
        )

        // Only add website if it's not empty
        val websiteText = website.text.toString().trim()
        if (websiteText.isNotEmpty()) {
            orgData["website"] = websiteText
        }

        progressBar.visibility = View.VISIBLE

        if (organizationId == null) {
            val uniqueId = generateUniqueorgId()
            orgData["organizationId"] = uniqueId

            db.collection("organizations").document(uniqueId).set(orgData)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Organization Registered", Toast.LENGTH_LONG).show()
                    orgId.text = uniqueId // Show newly generated ID
                    redirectToDetailsFragment(uniqueId)
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Registration Failed", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Update existing organization
            db.collection("organizations").document(organizationId!!)
                .update(orgData as Map<String, Any>)
                .addOnSuccessListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Organization Updated", Toast.LENGTH_LONG).show()
                    redirectToDetailsFragment(organizationId!!)
                }
                .addOnFailureListener {
                    progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Update Failed", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun redirectToDetailsFragment(orgId: String) {
        val bundle = Bundle().apply {
            putString("orgName", orgName.text.toString().trim())
            putString("contactPerson", contactPerson.text.toString().trim())
            putString("email", email.text.toString().trim())
            putString("phoneNumber", phoneNumber.text.toString().trim())
            putString("address", address.text.toString().trim())
            putString("description", description.text.toString().trim())
            putString("orgId", orgId)
            val websiteText = website.text.toString().trim()
            if (websiteText.isNotEmpty()) {
                putString("website", websiteText)
            }
        }

        val detailFragment = OrganizationDetailFragment()
        detailFragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, detailFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun generateUniqueorgId(): String {
        val timestamp = System.currentTimeMillis().toString()
        val randomPart = (1000..9999).random().toString()
        return "ORG-$timestamp-$randomPart"
    }
}
