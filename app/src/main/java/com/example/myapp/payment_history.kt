package com.example.myapp

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class PaymentItem(
    val userName: String,
    val paymentAmount: String,
    val paymentStatus: Boolean, // true = done, false = not done
    val paymentMethod: String,
    val userEmail: String,
    val userPhone: String
)

class PaymentAdapter(private val payments: List<PaymentItem>) :
    RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {

    inner class PaymentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userName: TextView = itemView.findViewById(R.id.tvUserName)
        val paymentAmount: TextView = itemView.findViewById(R.id.tvPaymentAmount)
        val paymentStatus: TextView = itemView.findViewById(R.id.tvPaymentStatus)
        val paymentMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        val userEmail: TextView = itemView.findViewById(R.id.tvUserEmail)
        val userPhone: TextView = itemView.findViewById(R.id.tvUserPhone)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.payment_item, parent, false)
        return PaymentViewHolder(view)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val payment = payments[position]
        holder.userName.text = payment.userName
        holder.paymentAmount.text = "Amount: ${payment.paymentAmount}"
        holder.paymentStatus.text = if (payment.paymentStatus) "Payment Status: Done" else "Payment Status: Not Done"
        holder.paymentStatus.setTextColor(if (payment.paymentStatus) Color.GREEN else Color.RED)
        holder.paymentMethod.text = "Method: ${payment.paymentMethod}"
        holder.userEmail.text = "Email: ${payment.userEmail}"
        holder.userPhone.text = "Phone: ${payment.userPhone}"
    }

    override fun getItemCount(): Int = payments.size
}

class PaymentHistoryFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_history, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.payment_container)

        // Static data for demonstration
        val paymentList = listOf(
            PaymentItem("John Doe", "$100", true, "Credit Card", "john.doe@example.com", "1234567890"),
            PaymentItem("Jane Smith", "$200", false, "PayPal", "jane.smith@example.com", "9876543210"),
            PaymentItem("Alice Brown", "$150", true, "Debit Card", "alice.brown@example.com", "4567891230")
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PaymentAdapter(paymentList)

        return view
    }
}
