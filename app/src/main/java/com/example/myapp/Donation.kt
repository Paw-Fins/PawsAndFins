package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DonationFragment : Fragment() {

    private lateinit var recyclerViewDonations: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_donation, container, false)

        recyclerViewDonations = view.findViewById(R.id.recyclerViewDonations)
        recyclerViewDonations.layoutManager = LinearLayoutManager(requireContext())

        // Sample donation data
        val donationList = listOf(
            Donation("$10", "One-time Donation",),
            Donation("$25", "Monthly Support", ),
            Donation("$50", "Annual Support")
        )

        recyclerViewDonations.adapter = DonationAdapter(donationList)

        return view
    }
}

// Data model for donation items
data class Donation(
    val amount: String,
    val amountType: String,
//    val paymentMethods: List<Int> // Resource IDs for icons
)

// Adapter for RecyclerView
class DonationAdapter(private val donationList: List<Donation>) :
    RecyclerView.Adapter<DonationAdapter.DonationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_donation, parent, false)
        return DonationViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonationViewHolder, position: Int) {
        val donation = donationList[position]
        holder.tvAmount.text = donation.amount
        holder.tvAmountType.text = donation.amountType

        // Add payment method icons dynamically (max 3)
        holder.paymentMethodsContainer.removeAllViews()
//        donation.paymentMethods.take(3).forEach { iconRes ->
//            val imageView = android.widget.ImageView(holder.itemView.context)
//            imageView.setImageResource(iconRes)
//            val params = android.widget.LinearLayout.LayoutParams(80, 80)
//            params.marginEnd = 8
//            imageView.layoutParams = params
//            holder.paymentMethodsContainer.addView(imageView)
//        }
    }

    override fun getItemCount() = donationList.size

    class DonationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvAmount: android.widget.TextView = itemView.findViewById(R.id.tvAmount)
        val tvAmountType: android.widget.TextView = itemView.findViewById(R.id.tvAmountType)
        val paymentMethodsContainer: android.widget.LinearLayout = itemView.findViewById(R.id.paymentMethodsContainer)
    }
}
