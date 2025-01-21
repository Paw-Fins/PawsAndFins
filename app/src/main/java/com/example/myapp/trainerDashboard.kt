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

class TrainerDashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var trainerAppointmentsRecyclerView: RecyclerView
    private val trainerAppointmentsList = mutableListOf<TrainerAppointment>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_trainer_dashboard_main, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        (requireActivity() as MainActivity).showBottomNavigation(true)

        trainerAppointmentsRecyclerView = view.findViewById(R.id.pendingAppointmentsRecyclerView)
        trainerAppointmentsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchTrainerAppointments()

        return view
    }

    private fun fetchTrainerAppointments() {
        trainerAppointmentsList.clear()

        val dummyAppointments = listOf(
            TrainerAppointment("Bella", "John Doe", "10:00 AM - 11:00 AM", "Obedience Training"),
            TrainerAppointment("Max", "Jane Smith", "12:00 PM - 1:00 PM", "Agility Training"),
            TrainerAppointment("Charlie", "Robert Brown", "2:00 PM - 3:00 PM", "Behavioral Training"),
            TrainerAppointment("Lucy", "Emily White", "3:30 PM - 4:30 PM", "Puppy Training")
        )

        trainerAppointmentsList.addAll(dummyAppointments)

        trainerAppointmentsRecyclerView.adapter = TrainerAppointmentAdapter(trainerAppointmentsList)
        trainerAppointmentsRecyclerView.adapter?.notifyDataSetChanged()

        Log.d("TrainerDashboard", "Dummy appointments list size: ${trainerAppointmentsList.size}")
    }
}

data class TrainerAppointment(
    val petName: String,
    val ownerName: String,
    val timeSlot: String,
    val trainingService: String
)

class TrainerAppointmentAdapter(private val appointments: List<TrainerAppointment>) :
    RecyclerView.Adapter<TrainerAppointmentAdapter.TrainerAppointmentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrainerAppointmentViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.notification_card, parent, false)
        return TrainerAppointmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrainerAppointmentViewHolder, position: Int) {
        val trainerAppointment = appointments[position]
        holder.bind(trainerAppointment)
    }

    override fun getItemCount(): Int = appointments.size

    inner class TrainerAppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val petNameTextView = itemView.findViewById<TextView>(R.id.appointment_petname)
        private val ownerNameTextView = itemView.findViewById<TextView>(R.id.appointment_username)
        private val timeSlotTextView = itemView.findViewById<TextView>(R.id.appointment_timeSlot)
        private val trainingServiceTextView = itemView.findViewById<TextView>(R.id.appointment_problemdes)
        private val viewMoreButton = itemView.findViewById<Button>(R.id.viewMoreButton)

        fun bind(trainerAppointment: TrainerAppointment) {
            petNameTextView.text = trainerAppointment.petName
            ownerNameTextView.text = trainerAppointment.ownerName
            timeSlotTextView.text = trainerAppointment.timeSlot
            trainingServiceTextView.text = trainerAppointment.trainingService

            viewMoreButton.setOnClickListener {
                val transaction = itemView.context as AppCompatActivity
                transaction.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, GroomerAppointmentService())
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
