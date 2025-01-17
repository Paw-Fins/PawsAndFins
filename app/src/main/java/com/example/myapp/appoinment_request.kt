package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class AppoinmentRequestFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var appointmentsRecyclerView: RecyclerView
    private val appointmentsList = mutableListOf<AppointmentRequest>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_appoinment_request, container, false)

        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        appointmentsRecyclerView = view.findViewById(R.id.all_appoinment_request)
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch all appointment requests
        fetchAllAppointments()

        return view
    }

    private fun fetchAllAppointments() {
        firestore.collection("appointments")
            .get()
            .addOnSuccessListener { documents ->
                appointmentsList.clear() // Clear the list before populating
                for (document in documents) {
                    val petName = document.getString("petName") ?: "No pet name"
                    val userName = document.getString("parentName") ?: "No user name"
                    val timeSlot = document.getString("timeSlot") ?: "No time slot"
                    val problemDesc = document.getString("problemDesc") ?: "No problem description"

                    // Create an AppointmentRequest object and add it to the list
                    val appointment = AppointmentRequest(petName, userName, timeSlot, problemDesc)
                    appointmentsList.add(appointment)
                }
                // Update RecyclerView with the adapter
                appointmentsRecyclerView.adapter = AppointmentRequestAdapter(appointmentsList)
            }
            .addOnFailureListener { exception ->
                Log.e("AppoinmentRequest", "Error fetching appointments: ", exception)
            }
    }
}

// Data class to represent an appointment request
data class AppointmentRequest(
    val petName: String,
    val userName: String,
    val timeSlot: String,
    val problemDesc: String
)

// RecyclerView Adapter for appointment requests
class AppointmentRequestAdapter(private val appointments: List<AppointmentRequest>) :
    RecyclerView.Adapter<AppointmentRequestAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.notification_card, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = appointments.size

    // ViewHolder for each appointment request item
    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val petNameTextView: TextView = itemView.findViewById(R.id.appointment_petname)
        private val userNameTextView: TextView = itemView.findViewById(R.id.appointment_username)
        private val timeSlotTextView: TextView = itemView.findViewById(R.id.appointment_timeSlot)
        private val problemDescTextView: TextView = itemView.findViewById(R.id.appointment_problemdes)
        private val viewMoreButton: Button = itemView.findViewById(R.id.viewMoreButton)

        fun bind(appointment: AppointmentRequest) {
            petNameTextView.text = appointment.petName
            userNameTextView.text = appointment.userName
            timeSlotTextView.text = appointment.timeSlot
            problemDescTextView.text = appointment.problemDesc

            viewMoreButton.setOnClickListener {
                // Logic for viewing more details about the appointment can be added here
            }
        }
    }
}
