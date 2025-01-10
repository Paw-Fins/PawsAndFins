package com.example.myapp

import GroomerAppointmentService
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

class GroomerDashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var groomingAppointmentsRecyclerView: RecyclerView
    private val groomingAppointmentsList = mutableListOf<GroomingAppointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize RecyclerView
        groomingAppointmentsRecyclerView = view.findViewById(R.id.productContainer)
        groomingAppointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch the current groomer's appointments
        fetchGroomerAppointments()

        return view
    }

    private fun fetchGroomerAppointments() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid

            firestore.collection("appointments")
                .whereEqualTo("groomerId", userId) // Match appointments where groomerId == current user's ID
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        val petName = document.getString("petName") ?: "No pet name"
                        val ownerName = document.getString("parentName") ?: "No owner name"
                        val timeSlot = document.getString("timeSlot") ?: "No time slot"
                        val groomingService = document.getString("groomingService") ?: "No grooming service"

                        // Create a GroomingAppointment object and add it to the list
                        val groomingAppointment = GroomingAppointment(petName, ownerName, timeSlot, groomingService)
                        groomingAppointmentsList.add(groomingAppointment)
                    }
                    // Update RecyclerView with the adapter
                    groomingAppointmentsRecyclerView.adapter = GroomingAppointmentAdapter(groomingAppointmentsList)
                }
                .addOnFailureListener { exception ->
                    Log.e("GroomerDashboard", "Error fetching grooming appointments: ", exception)
                }
        } else {
            Log.w("GroomerDashboard", "No user is logged in")
        }
    }
}

data class GroomingAppointment(
    val petName: String,
    val ownerName: String,
    val timeSlot: String,
    val groomingService: String
)

class GroomingAppointmentAdapter(private val appointments: List<GroomingAppointment>) :
    RecyclerView.Adapter<GroomingAppointmentAdapter.GroomingAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroomingAppointmentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return GroomingAppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroomingAppointmentViewHolder, position: Int) {
        val groomingAppointment = appointments[position]
        holder.bind(groomingAppointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class GroomingAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val petNameTextView = itemView.findViewById<TextView>(R.id.appointment_petname)
        private val ownerNameTextView = itemView.findViewById<TextView>(R.id.appointment_username)
        private val timeSlotTextView = itemView.findViewById<TextView>(R.id.appointment_timeSlot)
        private val groomingServiceTextView = itemView.findViewById<TextView>(R.id.appointment_problemdes)
        private val viewMoreButton = itemView.findViewById<Button>(R.id.viewMoreButton)

        fun bind(groomingAppointment: GroomingAppointment) {
            petNameTextView.text = groomingAppointment.petName
            ownerNameTextView.text = groomingAppointment.ownerName
            timeSlotTextView.text = groomingAppointment.timeSlot
            groomingServiceTextView.text = groomingAppointment.groomingService

            // Set onClick listener for "View More" button
            viewMoreButton.setOnClickListener {
                val transaction = itemView.context as AppCompatActivity
                transaction.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GroomerAppointmentService())
                    .addToBackStack(null) // Allow back navigation
                    .commit()
            }
        }
    }
}
