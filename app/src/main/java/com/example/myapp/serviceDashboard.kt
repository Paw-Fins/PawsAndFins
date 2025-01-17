package com.example.myapp

import AppointmentDetailsFragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ServiceDashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var appointmentsRecyclerView: RecyclerView
    private val appointmentsList = mutableListOf<Appointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_dashboard, container, false)
        (requireActivity() as MainActivity).showBottomNavigation(true)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        appointmentsRecyclerView = view.findViewById(R.id.productContainer)
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch the current user's appointments
        fetchUserAppointments()


        return view
    }

    private fun fetchUserAppointments() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("appointments")
                .whereEqualTo("serviceId", userId) // Match appointments where serviceId == current user's ID
                .get()
                .addOnSuccessListener { documents ->
                    appointmentsList.clear() // Clear the list before populating
                    for (document in documents) {
                        val petName = document.getString("petName") ?: "No pet name"
                        val userName = document.getString("parentName") ?: "No user name"
                        val timeSlot = document.getString("timeSlot") ?: "No time slot"
                        val problemDesc = document.getString("problemDesc") ?: "No problem description"

                        // Create an Appointment object and add it to the list
                        val appointment = Appointment(petName, userName, timeSlot, problemDesc)
                        appointmentsList.add(appointment)
                    }
                    // Update RecyclerView with the adapter
                    appointmentsRecyclerView.adapter = AppointmentAdapter(appointmentsList)
                }
                .addOnFailureListener { exception ->
                    Log.e("ServiceDashboard", "Error fetching appointments: ", exception)
                }
        } else {
            Log.w("ServiceDashboard", "No user is logged in")
        }
    }
}

data class Appointment(
    val petName: String,
    val userName: String,
    val timeSlot: String,
    val problemDesc: String
)

class AppointmentAdapter(private val appointments: List<Appointment>) :
    RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return AppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.bind(appointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val petNameTextView = itemView.findViewById<TextView>(R.id.appointment_petname)
        private val userNameTextView = itemView.findViewById<TextView>(R.id.appointment_username)
        private val timeSlotTextView = itemView.findViewById<TextView>(R.id.appointment_timeSlot)
        private val problemDescTextView =
            itemView.findViewById<TextView>(R.id.appointment_problemdes)
        private val viewMoreButton = itemView.findViewById<Button>(R.id.viewMoreButton)

        fun bind(appointment: Appointment) {
            petNameTextView.text = appointment.petName
            userNameTextView.text = appointment.userName
            timeSlotTextView.text = appointment.timeSlot
            problemDescTextView.text = appointment.problemDesc

            viewMoreButton.setOnClickListener {
                val transaction = itemView.context as AppCompatActivity
                transaction.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AppointmentDetailsFragment())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}