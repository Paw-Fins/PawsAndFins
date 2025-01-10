import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.myapp.R

class GroomerContactFragment : Fragment() {

    private lateinit var groomerContainer: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var groomerProfileImage: ImageView
    private lateinit var groomerName: TextView
    private lateinit var groomerSpecialty: TextView
    private lateinit var groomerContactInfo: TextView
    private lateinit var groomerAddress: TextView
    private lateinit var groomerServices: TextView
    private lateinit var bookGroomingButton: Button
    private lateinit var callGroomerButton: Button

    private lateinit var groomerId: String
    private lateinit var groomerContactNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groomer_contact, container, false)

        // Initialize views
        groomerContainer = view.findViewById(R.id.groomerContainer)
        detailsContainer = view.findViewById(R.id.detailsContainer)
        groomerProfileImage = view.findViewById(R.id.groomerProfileImage)
        groomerName = view.findViewById(R.id.groomerName)
        groomerSpecialty = view.findViewById(R.id.groomerSpecialty)
        groomerContactInfo = view.findViewById(R.id.groomerContactInfo)
        groomerAddress = view.findViewById(R.id.groomerAddress)
        groomerServices = view.findViewById(R.id.groomerServices)
        bookGroomingButton = view.findViewById(R.id.bookGroomingButton)
        callGroomerButton = view.findViewById(R.id.callGroomerButton)

        // Get the arguments passed from GroomerFragment
        arguments?.let {
            val groomerNameText = it.getString("groomerName")
            val groomerSpecialtyText = it.getString("groomerSpecialty")
            groomerContactNumber = it.getString("groomerContact") ?: "" // Store the groomer's contact number
            val groomerAddressText = it.getString("groomerAddress")
            val groomerServicesText = it.getString("groomerServices")
            groomerId = it.getString("groomerId") ?: "" // Get the groomerId
            val isEmergencyAvailable = it.getBoolean("isEmergencyAvailable")

            // Populate the views with the passed data
            groomerName.text = "Name: $groomerNameText"
            groomerSpecialty.text = "Specialty: $groomerSpecialtyText"
            groomerContactInfo.text = "Contact: $groomerContactNumber"
            groomerAddress.text = groomerAddressText
            groomerServices.text = groomerServicesText

            detailsContainer.visibility = View.VISIBLE

            bookGroomingButton.setOnClickListener {
                navigateToGroomingAppointmentFragment(groomerId)
            }

            // Set up callGroomerButton to make a phone call when clicked
            callGroomerButton.setOnClickListener {
                makePhoneCall(groomerContactNumber)
            }
        }

        return view
    }

    private fun navigateToGroomingAppointmentFragment(groomerId: String) {
        val fragment = GroomerAppointmentFragment()

        val bundle = Bundle()
        bundle.putString("groomerId", groomerId) // Pass the groomerId to the next fragment
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace the current fragment with GroomingAppointmentFragment
            .addToBackStack(null)
            .commit()
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } else {
            // Handle the case where there is no phone number available
        }
    }
}
