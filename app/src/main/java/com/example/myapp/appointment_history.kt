import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class AppointmentHistoryFragment : Fragment() {

    private lateinit var db: FirebaseFirestore
    private lateinit var appointmentContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appointment_main, container, false)

        db = FirebaseFirestore.getInstance()
        appointmentContainer = view.findViewById(R.id.appointmentContainer)

        fetchAppointments()

        return view
    }

    private fun fetchAppointments() {
        db.collection("appointments")
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    for (document in documents) {
                        createAppointmentCard(document)
                    }
                } else {
                    Toast.makeText(requireContext(), "No appointments found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch appointments", Toast.LENGTH_SHORT).show()
                exception.printStackTrace()
            }
    }
    private fun createAppointmentCard(document: QueryDocumentSnapshot) {
        // Inflate a single appointment card layout
        val appointmentCard = LayoutInflater.from(requireContext())
            .inflate(R.layout.appointmenthistory_main, null) as LinearLayout

        // Extract data from Firestore document
        val userName = document.getString("userName") ?: "Unknown"
        val serviceProvider = document.getString("serviceProvider") ?: "Unknown"
        val timeSlot = document.getString("timeSlot") ?: "Unknown"
        val status = document.getString("status") ?: "Unknown"

        // Set data into views
        val txtUserName = appointmentCard.findViewById<TextView>(R.id.txtUserName)
        val txtServiceProvider = appointmentCard.findViewById<TextView>(R.id.txtServiceProvider)
        val txtTimeSlot = appointmentCard.findViewById<TextView>(R.id.txtTimeSlot)
        val statusButton = appointmentCard.findViewById<Button>(R.id.statusButton)

        txtUserName.text = "User Name: $userName"
        txtServiceProvider.text = "Service Provider: $serviceProvider"
        txtTimeSlot.text = "Time Slot: $timeSlot"

        // Set button status color
        statusButton.text = status
        if (status.equals("Accepted", ignoreCase = true)) {
            statusButton.text = "Accepted"
            statusButton.setBackgroundColor(Color.parseColor("#4CAF50")) // Green
        } else {
            statusButton.text = "Rejected"
            statusButton.setBackgroundColor(Color.parseColor("#F44336")) // Red
        }

        // Add the card to the appointment container
        appointmentContainer.addView(appointmentCard)
    }

}