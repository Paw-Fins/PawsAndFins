package com.example.myapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class AdminVetRegister : Fragment() {

    data class Vet(
        val name: String,
        val address: String,
        val specialization: String,
        val services: List<String> // List of services
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_vet_main, container, false)
        val vetContainer = view.findViewById<LinearLayout>(R.id.vet_list_container)

        // Load static vets into the LinearLayout
        loadStaticVets(vetContainer)

        return view
    }

    private fun loadStaticVets(container: LinearLayout) {
        val vets = listOf(
            Vet("Dr. John Doe", "123 Pet Street, City", "Small Animals",
                listOf("Vaccinations", "Microchipping", "Dental Cleaning")),
            Vet("Dr. Jane Smith", "456 Vet Road, Town", "Equine",
                listOf("Hoof Care", "Deworming", "Emergency Surgery")),
            Vet("Dr. Emily Johnson", "789 Animal Ave, Village", "Exotic Pets",
                listOf("Bird Care", "Reptile Check-ups", "Rodent Nutrition Advice")),
            Vet("Dr. Robert Brown", "321 Wildlife Lane, Country", "Wildlife",
                listOf("Rehabilitation", "Disease Control", "Tracking & Monitoring")),
            Vet("Dr. Sarah White", "654 Farm Drive, Countryside", "Livestock",
                listOf("Artificial Insemination", "Herd Health Management", "Mastitis Treatment")),
            Vet("Dr. Michael Green", "987 Urban Blvd, Metropolis", "Surgery Specialist",
                listOf("Orthopedic Surgery", "Soft Tissue Surgery", "Tumor Removal"))
        )

        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for (vet in vets) {
            val itemView = inflater.inflate(R.layout.admin_organization_ragister, container, false)

            val nameTextView = itemView.findViewById<TextView>(R.id.owner_name)
            val addressTextView = itemView.findViewById<TextView>(R.id.address)
            val servicesContainer = itemView.findViewById<LinearLayout>(R.id.services_container)

            nameTextView.text = vet.name
            addressTextView.text = vet.address

            // Clear and add services dynamically
            servicesContainer.removeAllViews()
            for (service in vet.services) {
                val serviceTextView = TextView(requireContext()).apply {
                    text = service
                    textSize = 14f
                    setTextColor(Color.parseColor("#666666"))
                }

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
