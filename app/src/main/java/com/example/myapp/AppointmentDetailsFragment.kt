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

class AppointmentDetailsFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var txtPetName: TextView
    private lateinit var txtPetAge: TextView
    private lateinit var txtPetBreed: TextView
    private lateinit var txtPetGender: TextView
    private lateinit var txtAppointmentDate: TextView
    private lateinit var txtCustomerPhoneNumber: TextView
    private lateinit var txtParentName: TextView
    private lateinit var txtTimeSlot: TextView
    private lateinit var txtProblemDescription: TextView
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vet_appointment, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtPetName = view.findViewById(R.id.txtpetName)
        txtPetAge = view.findViewById(R.id.txtpetAge)
        txtPetBreed = view.findViewById(R.id.txtpetBreed)
        txtPetGender = view.findViewById(R.id.txtpetGender)
        txtAppointmentDate = view.findViewById(R.id.txtappointmentDate)
        txtCustomerPhoneNumber = view.findViewById(R.id.txtcustomerPhoneNumber)
        txtParentName = view.findViewById(R.id.txtparentName)
        txtTimeSlot = view.findViewById(R.id.txttimeSlot)
        txtProblemDescription = view.findViewById(R.id.txtproblemDescription)
        acceptButton = view.findViewById(R.id.acceptButton)
        declineButton = view.findViewById(R.id.declineButton)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            fetchAppointmentData(userId)
        }

        acceptButton.setOnClickListener {
            updateAppointmentStatus("Accepted")
            declineButton.visibility = View.GONE
            acceptButton.text = "Accepted"
            acceptButton.isEnabled = false
        }

        declineButton.setOnClickListener {
            updateAppointmentStatus("Declined")
            acceptButton.visibility = View.GONE
            declineButton.text = "Declined"
            declineButton.isEnabled = false
        }

        return view
    }

    private fun fetchAppointmentData(serviceId: String) {
        val appointmentsRef = db.collection("appointments")

        appointmentsRef.whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document: QueryDocumentSnapshot in documents) {
                        val petName = document.getString("petName") ?: "Unknown"
                        val petAge = document.getString("petAge") ?: "Unknown"
                        val petBreed = document.getString("petBreed") ?: "Unknown"
                        val petGender = document.getString("petGender") ?: "Unknown"
                        val appointmentDate = document.getString("appointmentDate") ?: "Unknown"
                        val customerPhoneNumber =
                            document.getString("customerPhoneNumber") ?: "Unknown"
                        val parentName = document.getString("parentName") ?: "Unknown"
                        val timeSlot = document.getString("timeSlot") ?: "Unknown"
                        val problemDescription = document.getString("problemDesc") ?: "Unknown"

                        txtPetName.text = petName
                        txtPetAge.text = petAge
                        txtPetBreed.text = petBreed
                        txtPetGender.text = petGender
                        txtAppointmentDate.text = appointmentDate
                        txtCustomerPhoneNumber.text = customerPhoneNumber
                        txtParentName.text = parentName
                        txtTimeSlot.text = timeSlot
                        txtProblemDescription.text = problemDescription
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun updateAppointmentStatus(status: String) {
        val appointmentRef = db.collection("appointments")

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
                    Toast.makeText(requireContext(), "No appointments found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve appointments", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }
}
