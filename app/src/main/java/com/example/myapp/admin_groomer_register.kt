package com.example.myapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class AdminGroomerRegister : Fragment() {

    data class Groomer(
        val name: String,
        val address: String,
        val services: List<String> // List of services
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_groomer_main, container, false)
        val groomerContainer = view.findViewById<LinearLayout>(R.id.groomer_list_container)

        // Load static groomers into the LinearLayout
        loadStaticGroomers(groomerContainer)

        return view
    }

    private fun loadStaticGroomers(container: LinearLayout) {
        val groomers = listOf(
            Groomer("Happy Tails Grooming", "101 Pet Street, City", listOf("Bathing", "Haircut", "Nail Trimming")),
            Groomer("Pawfect Look", "222 Fur Lane, Town", listOf("Full Grooming", "Ear Cleaning", "Teeth Brushing")),
            Groomer("Furry Friends Spa", "333 Grooming Ave, Village", listOf("Hydrotherapy", "De-shedding Treatment", "Paw Massage")),
            Groomer("Elite Pet Grooming", "444 Luxury Blvd, Metropolis", listOf("Hand Scissoring", "Coat Styling", "Show Grooming")),
            Groomer("Paws & Claws", "555 Animal Drive, Countryside", listOf("Flea & Tick Treatment", "De-matting", "Aromatherapy Bath"))
        )

        container.removeAllViews()
        val inflater = LayoutInflater.from(requireContext())

        for (groomer in groomers) {
            val itemView = inflater.inflate(R.layout.admin_organization_ragister, container, false)

            val nameTextView = itemView.findViewById<TextView>(R.id.owner_name)
            val addressTextView = itemView.findViewById<TextView>(R.id.address)
            val servicesContainer = itemView.findViewById<LinearLayout>(R.id.services_container)

            nameTextView.text = groomer.name
            addressTextView.text = groomer.address

            // Clear and add services dynamically
            servicesContainer.removeAllViews()
            for (service in groomer.services) {
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
