package com.example.pawsandfins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject

class ReviewsFragment : Fragment() {

    private lateinit var recyclerReviews: RecyclerView
    private lateinit var btnHome: Button
    private val db = FirebaseFirestore.getInstance()
    private val reviewList = mutableListOf<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_reviews, container, false)

        recyclerReviews = view.findViewById(R.id.recyclerReviews)
        btnHome = view.findViewById(R.id.btnHome)
        recyclerReviews.layoutManager = LinearLayoutManager(requireContext())

        btnHome.setOnClickListener {
            Toast.makeText(requireContext(), "Home Clicked", Toast.LENGTH_SHORT).show()
        }

        fetchReviews()

        return view
    }

    private fun fetchReviews() {
        db.collection("reviews").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val review = document.toObject<Review>()
                    fetchNames(review)
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load reviews", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchNames(review: Review) {
        db.collection("users").document(review.userId).get()
            .addOnSuccessListener { userDoc ->
                review.reviewerName = userDoc.getString("name") ?: "Unknown"
                db.collection("users").document(review.vetId).get()
                    .addOnSuccessListener { vetDoc ->
                        review.vetName = "${vetDoc.getString("name") ?: "Unknown"} (${vetDoc.getString("role") ?: "Unknown"})"
                        reviewList.add(review)
                        recyclerReviews.adapter = ReviewAdapter(reviewList)
                    }
            }
    }

    data class Review(
        val reviewDate: String = "",
        val reviewText: String = "",
        val userId: String = "",
        val vetId: String = "",
        var reviewerName: String = "",
        var vetName: String = ""
    )

    class ReviewAdapter(private val reviewList: List<Review>) :
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

        class ReviewViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvFrom: TextView = view.findViewById(R.id.tvFrom)
            val tvTo: TextView = view.findViewById(R.id.tvTo)
            val tvReview: TextView = view.findViewById(R.id.tvReview)
            val tvDate: TextView = view.findViewById(R.id.tvDate)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_review, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val review = reviewList[position]
            holder.tvFrom.text = "From: ${review.reviewerName}"
            holder.tvTo.text = "To: ${review.vetName}"
            holder.tvReview.text = review.reviewText
            holder.tvDate.text = "Date: ${review.reviewDate}"
        }

        override fun getItemCount(): Int = reviewList.size
    }
}
