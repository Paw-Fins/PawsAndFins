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
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

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

        // Initialize Views
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

        // Get current user service ID from FirebaseAuth
        val userId = auth.currentUser?.uid

        // Fetch appointment details from Firestore
        if (userId != null) {
            fetchAppointmentData(userId)
        }

        acceptButton.setOnClickListener {
            updateAppointmentStatus("Accepted")
        }

        declineButton.setOnClickListener {
            updateAppointmentStatus("Declined")
        }

        return view
    }

    private fun fetchAppointmentData(serviceId: String) {
        // Reference to the appointments collection in Firestore
        val appointmentsRef = db.collection("appointments")

        // Query for appointments with the same service ID as the logged-in user
        appointmentsRef.whereEqualTo("serviceId", serviceId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents != null && !documents.isEmpty) {
                    for (document: QueryDocumentSnapshot in documents) {
                        // Retrieve the appointment data and update the UI
                        val petName = document.getString("petName") ?: "Unknown"
                        val petAge = document.getString("petAge") ?: "Unknown"
                        val petBreed = document.getString("petBreed") ?: "Unknown"
                        val petGender = document.getString("petGender") ?: "Unknown"
                        val appointmentDate = document.getString("appointmentDate") ?: "Unknown"
                        val customerPhoneNumber = document.getString("customerPhoneNumber") ?: "Unknown"
                        val parentName = document.getString("parentName") ?: "Unknown"
                        val timeSlot = document.getString("timeSlot") ?: "Unknown"
                        val problemDescription = document.getString("problemDesc") ?: "Unknown"

                        // Set the data in the respective TextViews
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
        // Reference to the appointments collection in Firestore
        val appointmentRef = db.collection("appointments")

        // Query to find the appointment that needs updating
        appointmentRef.whereEqualTo("serviceId", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val appointmentId = document.id // Get the appointment ID

                    // Update the appointment status
                    appointmentRef.document(appointmentId)
                        .update("status", status)
                        .addOnSuccessListener {
                            Toast.makeText(requireContext(), "Appointment $status", Toast.LENGTH_SHORT).show()
                            // Fetch the userId from the appointment document
                            val userId = document.getString("userId") ?: ""
                            sendStatusUpdateNotification(userId, status)
                        }
                }
            }
    }

    private fun sendStatusUpdateNotification(userId: String, status: String) {
        // Fetch the FCM token for the user
        val usersRef = db.collection("users")
        usersRef.document(userId).get()
            .addOnSuccessListener { document ->
                val token = document.getString("fcmToken")

                // Send a notification via FCM
                if (token != null) {
                    val message = RemoteMessage.Builder("$userId@fcm.googleapis.com")
                        .setMessageId(System.currentTimeMillis().toString())
                        .addData("title", "Appointment Status Updated")
                        .addData("body", "Your appointment has been $status.")
                        .build()

                    FirebaseMessaging.getInstance().send(message)
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }
}
