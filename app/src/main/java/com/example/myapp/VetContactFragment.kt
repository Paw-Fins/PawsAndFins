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

class VetContactFragment : Fragment() {

    private lateinit var vetContainer: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var vetProfileImage: ImageView
    private lateinit var vetName: TextView
    private lateinit var vetSpecialty: TextView
    private lateinit var vetContactInfo: TextView
    private lateinit var vetAddress: TextView
    private lateinit var vetServices: TextView
    private lateinit var bookAppointmentButton: Button
    private lateinit var callVetButton: Button

    private lateinit var vetId: String
    private lateinit var vetContactNumber: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vet_contact, container, false)

        // Initialize views
        vetContainer = view.findViewById(R.id.vetContainer)
        detailsContainer = view.findViewById(R.id.detailsContainer)
        vetProfileImage = view.findViewById(R.id.vetProfileImage)
        vetName = view.findViewById(R.id.vetName)
        vetSpecialty = view.findViewById(R.id.vetSpecialty)
        vetContactInfo = view.findViewById(R.id.vetContactInfo)
        vetAddress = view.findViewById(R.id.vetAddress)
        vetServices = view.findViewById(R.id.vetServices)
        bookAppointmentButton = view.findViewById(R.id.bookAppointmentButton)
        callVetButton = view.findViewById(R.id.callVetButton)

        // Get the arguments passed from VetFragment
        arguments?.let {
            val doctorName = it.getString("doctorName")
            val doctorSpecialty = it.getString("doctorSpecialty")
            vetContactNumber = it.getString("doctorContact") ?: "" // Store the vet's contact number
            val doctorAddress = it.getString("doctorAddress")
            val doctorServices = it.getString("doctorServices")
            vetId = it.getString("vetId") ?: "" // Get the vetId
            val isEmergencyAvailable = it.getBoolean("isEmergencyAvailable")

            // Populate the views with the passed data
            vetName.text = "Name: $doctorName"
            vetSpecialty.text = "Specialty: $doctorSpecialty"
            vetContactInfo.text = "Contact: $vetContactNumber"
            vetAddress.text = doctorAddress
            vetServices.text = doctorServices

            callVetButton.visibility = if (isEmergencyAvailable) View.VISIBLE else View.GONE
            detailsContainer.visibility = View.VISIBLE

            bookAppointmentButton.setOnClickListener {
                navigateToVetAppointmentFragment(vetId)
            }

            // Set up callVetButton to make a phone call when clicked
            callVetButton.setOnClickListener {
                makePhoneCall(vetContactNumber)
            }
        }

        return view
    }

    private fun navigateToVetAppointmentFragment(vetId: String) {
        val fragment = VetAppointmentFragment()

        val bundle = Bundle()
        bundle.putString("vetId", vetId) // Pass the vetId to the next fragment
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment) // Replace the current fragment with VetAppointmentFragment
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
