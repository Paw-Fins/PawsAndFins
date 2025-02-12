package com.example.myapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class AdminNgoRegister : Fragment() {

    data class Organization(val name: String, val address: String, val services: Array<String>)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_ngo_main, container, false)
        val ngoContainer = view.findViewById<LinearLayout>(R.id.adminNgoRegisterContainer)

        // Load NGOs into the LinearLayout
        loadStaticNGOs(ngoContainer)

        return view
    }

    private fun loadStaticNGOs(container: LinearLayout) {
        val ngos = listOf(
            Organization("Helping Paws", "123 Main Street, City, Country", arrayOf("Adoption", "Rescue", "Fundraising")),
            Organization("Animal Rescue Foundation", "456 Park Avenue, City, Country", arrayOf("Rescue", "Medical Assistance", "Foster Care")),
            Organization("Save Strays", "789 Oak Road, City, Country", arrayOf("Rescue", "Adoption", "Awareness")),
            Organization("Wildlife Protectors", "102 Greenway Lane, City, Country", arrayOf("Habitat Protection", "Animal Rescue", "Legal Advocacy")),
            Organization("Compassionate Hearts", "55 Sunset Boulevard, City, Country", arrayOf("Food Drives", "Health Camps", "Education Programs")),
            Organization("Nature's Guardians", "808 Forest Hills, City, Country", arrayOf("Wildlife Conservation", "Community Engagement", "Eco-Tourism"))
        )

        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for (ngo in ngos) {
            val itemView = inflater.inflate(R.layout.admin_organization_ragister, container, false)

            val nameTextView = itemView.findViewById<TextView>(R.id.owner_name)
            val addressTextView = itemView.findViewById<TextView>(R.id.address)
            val servicesContainer = itemView.findViewById<LinearLayout>(R.id.services_container)

            nameTextView.text = ngo.name
            addressTextView.text = ngo.address

            // Clear and add services dynamically
            servicesContainer.removeAllViews()
            for (service in ngo.services) {
                val serviceTextView = TextView(requireContext())
                serviceTextView.text = service
                serviceTextView.textSize = 14f
                serviceTextView.setTextColor(Color.parseColor("#666666"))

                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(0, 4, 0, 4)
                serviceTextView.layoutParams = params

                servicesContainer.addView(serviceTextView)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = (16 * resources.displayMetrics.density).toInt()
            layoutParams.setMargins(0, margin, 0, margin)
            itemView.layoutParams = layoutParams

            container.addView(itemView)
        }
    }
}
