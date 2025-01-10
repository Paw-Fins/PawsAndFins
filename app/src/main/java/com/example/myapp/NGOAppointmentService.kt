import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class NGOAppointmentService : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var txtNGOName: TextView
    private lateinit var txtAppointmentPurpose: TextView
    private lateinit var txtContactNumber: TextView
    private lateinit var txtEmailAddress: TextView
    private lateinit var txtAdditionalInstructions: TextView
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_ngo, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtNGOName = view.findViewById(R.id.txtNGOName)
        txtAppointmentPurpose = view.findViewById(R.id.appointmentPurpose)
        txtContactNumber = view.findViewById(R.id.txtContactNumber)
        txtEmailAddress = view.findViewById(R.id.txtEmailAddress)
        txtAdditionalInstructions = view.findViewById(R.id.txtAdditionalInstructions)
        acceptButton = view.findViewById(R.id.acceptButton)
        declineButton = view.findViewById(R.id.declineButton)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            fetchNGOAppointmentData(userId)
        }

        acceptButton.setOnClickListener {
            updateNGOAppointmentStatus("Accepted")
        }

        declineButton.setOnClickListener {
            updateNGOAppointmentStatus("Declined")
        }

        return view
    }

    private fun fetchNGOAppointmentData(serviceId: String) {
        val appointmentsRef = db.collection("ngoAppointments")

        appointmentsRef.whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document: QueryDocumentSnapshot in documents) {
                        val ngoName = document.getString("ngoName") ?: "Unknown"
                        val appointmentPurpose = document.getString("appointmentPurpose") ?: "Unknown"
                        val contactNumber = document.getString("contactNumber") ?: "Unknown"
                        val emailAddress = document.getString("emailAddress") ?: "Unknown"
                        val additionalInstructions = document.getString("additionalInstructions") ?: "None"

                        txtNGOName.text = ngoName
                        txtAppointmentPurpose.text = appointmentPurpose
                        txtContactNumber.text = contactNumber
                        txtEmailAddress.text = emailAddress
                        txtAdditionalInstructions.text = additionalInstructions
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun updateNGOAppointmentStatus(status: String) {
        val appointmentRef = db.collection("ngoAppointments")

        appointmentRef.whereEqualTo("serviceId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        val appointmentId = document.id

                        appointmentRef.document(appointmentId)
                            .update("status", status)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(),"Appointment $status",
                                    Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { exception ->
                                Toast.makeText(requireContext(),"Failed to update status",
                                    Toast.LENGTH_SHORT).show()
                                exception.printStackTrace()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "No NGO appointments found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve NGO appointments", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }
}
