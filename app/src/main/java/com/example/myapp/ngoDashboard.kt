package com.example.myapp

import NGOAppointmentService
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

class NGODashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var ngoAppointmentsRecyclerView: RecyclerView
    private val ngoAppointmentsList = mutableListOf<NGOAppointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngo_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        (requireActivity() as MainActivity).showBottomNavigation(true)

        ngoAppointmentsRecyclerView = view.findViewById(R.id.pendingAppointmentsRecyclerView)
        ngoAppointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchNGOAppointments()

        return view
    }

    private fun fetchNGOAppointments() {
        ngoAppointmentsList.clear()

        val dummyAppointments = listOf(
            NGOAppointment("Bella", "John Doe", "10:00 AM - 11:00 AM", "Obedience Training"),
            NGOAppointment("Max", "Jane Smith", "12:00 PM - 1:00 PM", "Agility Training"),
            NGOAppointment("Charlie", "Robert Brown", "2:00 PM - 3:00 PM", "Behavioral Training"),
            NGOAppointment("Lucy", "Emily White", "3:30 PM - 4:30 PM", "Puppy Training")
        )

        ngoAppointmentsList.addAll(dummyAppointments)

        ngoAppointmentsRecyclerView.adapter = NGOAppointmentAdapter(ngoAppointmentsList)
        ngoAppointmentsRecyclerView.adapter?.notifyDataSetChanged()

        Log.d("NGODashboard", "Dummy appointments list size: ${ngoAppointmentsList.size}")
    }
}

data class NGOAppointment(
    val petName: String,
    val ownerName: String,
    val timeSlot: String,
    val trainingService: String
)

class NGOAppointmentAdapter(private val appointments: List<NGOAppointment>) :
    RecyclerView.Adapter<NGOAppointmentAdapter.NGOAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NGOAppointmentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return NGOAppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: NGOAppointmentViewHolder, position: Int) {
        val ngoAppointment = appointments[position]
        holder.bind(ngoAppointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class NGOAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val petNameTextView = itemView.findViewById<TextView>(R.id.appointment_petname)
        private val ownerNameTextView = itemView.findViewById<TextView>(R.id.appointment_username)
        private val timeSlotTextView = itemView.findViewById<TextView>(R.id.appointment_timeSlot)
        private val trainingServiceTextView = itemView.findViewById<TextView>(R.id.appointment_problemdes)
        private val viewMoreButton = itemView.findViewById<Button>(R.id.viewMoreButton)

        fun bind(ngoAppointment: NGOAppointment) {
            petNameTextView.text = ngoAppointment.petName
            ownerNameTextView.text = ngoAppointment.ownerName
            timeSlotTextView.text = ngoAppointment.timeSlot
            trainingServiceTextView.text = ngoAppointment.trainingService

            viewMoreButton.setOnClickListener {
                val transaction = itemView.context as AppCompatActivity
                transaction.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, NGOAppointmentService())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
