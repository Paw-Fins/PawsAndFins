package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore

class PaymentHistoryAdminFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var payuserContainer: LinearLayout
    private lateinit var searchBar: SearchView
    private lateinit var noMatchesFound: TextView

    private val paymentList = mutableListOf<Map<String, Any>>() // Store all payment data

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_payment_history_user, container, false)
        payuserContainer = rootView.findViewById(R.id.payuserContainer)
        searchBar = rootView.findViewById(R.id.search_bar)
        noMatchesFound = rootView.findViewById(R.id.tvNoMatchesFound)

        firestore = FirebaseFirestore.getInstance()

        fetchPaymentHistory()
        setupSearch()

        return rootView
    }

    private fun fetchPaymentHistory() {
        firestore.collection("payment_history")
            .get()
            .addOnSuccessListener { querySnapshot ->
                paymentList.clear() // Clear the list before adding new data
                payuserContainer.removeAllViews() // Clear existing views
                for (document in querySnapshot.documents) {
                    val data = document.data
                    if (data != null) {
                        paymentList.add(data)
                        val paymentCard = createPaymentCard(data)
                        payuserContainer.addView(paymentCard)
                    }
                }
                toggleNoMatchesFound(paymentList.isEmpty())
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to fetch data: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun setupSearch() {
        searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterPaymentList(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterPaymentList(newText)
                return true
            }
        })

        searchBar.setOnCloseListener {
            // Reset to show all data when search is cleared
            displayAllPayments()
            false
        }
    }

    private fun filterPaymentList(query: String?) {
        val filteredList = paymentList.filter { payment ->
            payment.any { (_, value) ->
                value.toString().contains(query ?: "", ignoreCase = true)
            }
        }

        payuserContainer.removeAllViews()
        if (filteredList.isNotEmpty()) {
            for (data in filteredList) {
                val paymentCard = createPaymentCard(data)
                payuserContainer.addView(paymentCard)
            }
        }
        toggleNoMatchesFound(filteredList.isEmpty())
    }

    private fun displayAllPayments() {
        payuserContainer.removeAllViews()
        for (data in paymentList) {
            val paymentCard = createPaymentCard(data)
            payuserContainer.addView(paymentCard)
        }
        toggleNoMatchesFound(paymentList.isEmpty())
    }

    private fun toggleNoMatchesFound(show: Boolean) {
        noMatchesFound.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun createPaymentCard(data: Map<String, Any>): View {
        val cardView = LayoutInflater.from(requireContext())
            .inflate(R.layout.payment_item, payuserContainer, false) as CardView

        val tvUserName = cardView.findViewById<TextView>(R.id.tvUserName)
        val tvPaymentAmount = cardView.findViewById<TextView>(R.id.tvPaymentAmount)
        val tvPaymentMethod = cardView.findViewById<TextView>(R.id.tvPaymentMethod)
        val tvUserEmail = cardView.findViewById<TextView>(R.id.tvUserEmail)
        val tvUserPhone = cardView.findViewById<TextView>(R.id.tvUserPhone)
        val tvPaymentStatus = cardView.findViewById<TextView>(R.id.tvPaymentStatus)

        val name = data["name"] as? String ?: "Unknown User"
        val amount = (data["amount"] as? Long)?.toInt() ?: 0
        val paymentId = data["paymentId"] as? String ?: "N/A"
        val email = data["email"] as? String ?: "Unknown Email"
        val contact = data["contact"] as? String ?: "Unknown Phone"
        val status = data["status"] as? String ?: "Unknown Status"

        tvUserName.text = name
        tvPaymentAmount.text = "Amount: ₹$amount"
        tvPaymentMethod.text = "Payment ID: $paymentId"
        tvUserEmail.text = "Email: $email"
        tvUserPhone.text = "Phone: $contact"
        tvPaymentStatus.text = "Payment Status: $status"

        context?.let {
            tvPaymentStatus.setTextColor(
                if (status.equals("success", ignoreCase = true))
                    it.getColor(android.R.color.holo_green_dark)
                else
                    it.getColor(android.R.color.holo_red_dark)
            )
        }

        return cardView
    }
}
