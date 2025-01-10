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

class GroomerAppointmentService : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    private lateinit var txtPetName: TextView
    private lateinit var txtPetBreed: TextView
    private lateinit var txtGroomingService: TextView
    private lateinit var txtAppointmentDate: TextView
    private lateinit var txtTimeSlot: TextView
    private lateinit var txtCustomerPhoneNumber: TextView
    private lateinit var txtParentName: TextView
    private lateinit var txtAdditionalInstructions: TextView
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_groomer_appointment, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        txtPetName = view.findViewById(R.id.txtPetName)
        txtPetBreed = view.findViewById(R.id.txtPetBreed)
        txtGroomingService = view.findViewById(R.id.txtGroomingService)
        txtAppointmentDate = view.findViewById(R.id.txtAppointmentDate)
        txtTimeSlot = view.findViewById(R.id.txtTimeSlot)
        txtCustomerPhoneNumber = view.findViewById(R.id.txtCustomerPhoneNumber)
        txtParentName = view.findViewById(R.id.txtParentName)
        txtAdditionalInstructions = view.findViewById(R.id.txtAdditionalInstructions)
        acceptButton = view.findViewById(R.id.acceptButton)
        declineButton = view.findViewById(R.id.declineButton)

        val userId = auth.currentUser?.uid

        if (userId != null) {
            fetchGroomingAppointmentData(userId)
        }

        acceptButton.setOnClickListener {
            updateGroomingAppointmentStatus("Accepted")
        }

        declineButton.setOnClickListener {
            updateGroomingAppointmentStatus("Declined")
        }

        return view
    }

    private fun fetchGroomingAppointmentData(serviceId: String) {
        val appointmentsRef = db.collection("groomingAppointments")

        appointmentsRef.whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document: QueryDocumentSnapshot in documents) {
                        val petName = document.getString("petName") ?: "Unknown"
                        val petBreed = document.getString("petBreed") ?: "Unknown"
                        val groomingService = document.getString("groomingService") ?: "Unknown"
                        val appointmentDate = document.getString("appointmentDate") ?: "Unknown"
                        val timeSlot = document.getString("timeSlot") ?: "Unknown"
                        val customerPhoneNumber = document.getString("customerPhoneNumber") ?: "Unknown"
                        val parentName = document.getString("parentName") ?: "Unknown"
                        val additionalInstructions = document.getString("additionalInstructions") ?: "None"

                        txtPetName.text = petName
                        txtPetBreed.text = petBreed
                        txtGroomingService.text = groomingService
                        txtAppointmentDate.text = appointmentDate
                        txtTimeSlot.text = timeSlot
                        txtCustomerPhoneNumber.text = customerPhoneNumber
                        txtParentName.text = parentName
                        txtAdditionalInstructions.text = additionalInstructions
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun updateGroomingAppointmentStatus(status: String) {
        val appointmentRef = db.collection("groomingAppointments")

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
                    Toast.makeText(requireContext(), "No grooming appointments found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve grooming appointments", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }
}
