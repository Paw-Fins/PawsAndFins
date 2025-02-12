package com.example.myapp

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class DonationActivity : AppCompatActivity(), PaymentResultListener {

    private lateinit var editAmount: EditText
    private lateinit var btnDonate: Button
    private lateinit var rvDonations: RecyclerView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val donationsList = mutableListOf<Donation>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fragment_donation)

        editAmount = findViewById(R.id.etDonationAmount)
        btnDonate = findViewById(R.id.btnDonate)
        rvDonations = findViewById(R.id.rvDonations)

        // RecyclerView setup
        rvDonations.layoutManager = LinearLayoutManager(this)
        rvDonations.adapter = DonationAdapter(donationsList)

        // Preload Razorpay
        Checkout.preload(applicationContext)

        btnDonate.setOnClickListener {
            val amountText = editAmount.text.toString().trim()
            if (amountText.isNotEmpty()) {
                val amount = amountText.toInt() * 100 // Convert to paise
                startPayment(amount)
            } else {
                Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show()
            }
        }

        fetchDonations()
    }

    private fun startPayment(amount: Int) {
        val checkout = Checkout()
        checkout.setKeyID(getString(R.string.rzr_pay)) // Fetch Razorpay Key from strings.xml

        try {
            val options = JSONObject().apply {
                put("name", "Paws and Fins")
                put("description", "Donation Payment")
                put("currency", "INR")
                put("amount", amount)
                put("prefill", JSONObject().apply {
                    put("email", auth.currentUser?.email ?: "example@email.com")
                    put("contact", auth.currentUser?.phoneNumber ?: "9999999999")
                })
            }

            checkout.open(this, options)
        } catch (e: Exception) {
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        askAnonymousDonation(paymentId)
    }

    override fun onPaymentError(code: Int, response: String?) {
        Toast.makeText(this, "Donation failed. Please try again.", Toast.LENGTH_LONG).show()
    }

    private fun askAnonymousDonation(paymentId: String?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_anonymous_donation, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("Anonymous Donation")
            .setView(dialogView)
            .setCancelable(false)
            .show()

        val btnYes = dialogView.findViewById<Button>(R.id.btnYes)
        val btnNo = dialogView.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            storeDonation(paymentId, anonymous = true)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            storeDonation(paymentId, anonymous = false)
            dialog.dismiss()
        }
    }

    private fun storeDonation(paymentId: String?, anonymous: Boolean) {
        val userId = auth.currentUser?.uid
        val amount = editAmount.text.toString()

        // Get current Date and Time
        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("hh:mm a, dd MMM yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(currentDate)

        if (anonymous || userId == null) {
            saveDonation(paymentId, "Anonymous", amount, formattedDate)
        } else {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val userName = document.getString("name") ?: "Guest"
                    saveDonation(paymentId, userName, amount, formattedDate)
                }
                .addOnFailureListener {
                    saveDonation(paymentId, "Guest", amount, formattedDate)
                }
        }
    }

    private fun saveDonation(paymentId: String?, userName: String, amount: String, dateTime: String) {
        val userId = auth.currentUser?.uid
        val donationData = hashMapOf(
            "userId" to userId,
            "paymentId" to paymentId,
            "name" to userName,
            "amount" to amount,
            "dateTime" to dateTime
        )

        firestore.collection("donations").add(donationData)
            .addOnSuccessListener {
                Toast.makeText(this, "Donation Successful! Thank you!", Toast.LENGTH_SHORT).show()
                fetchDonations() // Refresh the donations list
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error saving donation details.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchDonations() {
        firestore.collection("donations").get()
            .addOnSuccessListener { result ->
                val newList = mutableListOf<Donation>()
                for (document in result) {
                    val name = document.getString("name") ?: "Unknown"
                    val amount = document.getString("amount") ?: "0"
                    val dateTime = document.getString("dateTime") ?: "N/A"
                    newList.add(Donation(name, amount, dateTime))
                }
                donationsList.clear()
                donationsList.addAll(newList)
                rvDonations.adapter = DonationAdapter(donationsList) // Fix: Ensure adapter updates
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to load donations.", Toast.LENGTH_SHORT).show()
            }
    }
}

data class Donation(val name: String, val amount: String, val dateTime: String)

class DonationAdapter(private val donations: List<Donation>) :
    RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donations[position]
        holder.tvName.text = donation.name
        holder.tvAmount.text = "₹${donation.amount}"
        holder.tvDateTime.text = donation.dateTime
    }

    override fun getItemCount(): Int = donations.size

    class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvDateTime)
    }

}
