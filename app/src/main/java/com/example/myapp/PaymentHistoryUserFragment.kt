package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class PaymentHistoryUserFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var payuserContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_payment_history_user, container, false)
        payuserContainer = rootView.findViewById(R.id.payuserContainer)

        firestore = FirebaseFirestore.getInstance()

        fetchPaymentHistory()

        return rootView
    }

    private fun fetchPaymentHistory() {
        firestore.collection("payment_history")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val name = document.getString("name") ?: "Unknown User"
                    val amount = document.getLong("amount")?.toInt() ?: 0
                    val contact = document.getString("contact") ?: "Unknown Phone"
                    val email = document.getString("email") ?: "Unknown Email"
                    val paymentId = document.getString("paymentId") ?: "N/A"
                    val status = document.getString("status") ?: "Unknown Status"
                    val timestamp = document.getLong("timestamp") ?: 0L

                    val paymentCard = createPaymentCard(
                        name,
                        amount,
                        contact,
                        email,
                        paymentId,
                        status,
                        timestamp
                    )
                    payuserContainer.addView(paymentCard)
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to fetch data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun createPaymentCard(
        name: String,
        amount: Int,
        contact: String,
        email: String,
        paymentId: String,
        status: String,
        timestamp: Long
    ): View {
        val cardView = LayoutInflater.from(requireContext())
            .inflate(R.layout.payment_item, payuserContainer, false) as CardView

        val tvUserName = cardView.findViewById<TextView>(R.id.tvUserName)
        val tvPaymentAmount = cardView.findViewById<TextView>(R.id.tvPaymentAmount)
        val tvPaymentMethod = cardView.findViewById<TextView>(R.id.tvPaymentMethod)
        val tvUserEmail = cardView.findViewById<TextView>(R.id.tvUserEmail)
        val tvUserPhone = cardView.findViewById<TextView>(R.id.tvUserPhone)
        val tvPaymentStatus = cardView.findViewById<TextView>(R.id.tvPaymentStatus)

        tvUserName.text = name
        tvPaymentAmount.text = "Amount: ₹$amount"
        tvPaymentMethod.text = "Payment ID: $paymentId"
        tvUserEmail.text = "Email: $email"
        tvUserPhone.text = "Phone: $contact"
        tvPaymentStatus.text = "Payment Status: $status"

        // Update status text color dynamically
        tvPaymentStatus.setTextColor(
            if (status.equals("success", ignoreCase = true))
                requireContext().getColor(android.R.color.holo_green_dark)
            else
                requireContext().getColor(android.R.color.holo_red_dark)
        )

        return cardView
    }
}