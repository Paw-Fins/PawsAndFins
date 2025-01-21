package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VetReviewFragment : Fragment() {

    private lateinit var reviewRecyclerView: RecyclerView
    private val reviews = mutableListOf<Map<String, String>>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var vetId: String

    // This adapter binds the review data to the layout
    class VetReviewAdapter(private val reviewList: List<Map<String, String>>) :
        RecyclerView.Adapter<VetReviewAdapter.ReviewViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.vet_review, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val review = reviewList[position]
            holder.reviewerName.text = review["reviewerName"]
            holder.reviewText.text = review["reviewText"]
            holder.reviewDate.text = review["reviewDate"]
        }

        override fun getItemCount(): Int = reviewList.size

        class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val reviewerName: TextView = itemView.findViewById(R.id.reviewerName)
            val reviewText: TextView = itemView.findViewById(R.id.reviewText)
            val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vet_review, container, false)

        // Initialize Firebase Firestore and Authentication
        firestore = FirebaseFirestore.getInstance()
        val auth = FirebaseAuth.getInstance()

        // Get current user's ID as vetId (from Firebase Authentication)
        vetId = auth.currentUser?.uid ?: ""
        Log.d("VetReviewFragment", "vetId: $vetId")  // Log vetId for debugging

        // Initialize RecyclerView
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.setHasFixedSize(true)

        // Initialize Adapter
        val adapter = VetReviewAdapter(reviews)
        reviewRecyclerView.adapter = adapter

        // Load reviews from Firestore
        loadReviewsFromFirestore(adapter)

        return view
    }

    private fun loadReviewsFromFirestore(adapter: VetReviewAdapter) {
        firestore.collection("reviews")
            .whereEqualTo("vetId", vetId)  // Fetch reviews for the current vetId
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Log.d("VetReviewFragment", "No reviews found.")
                } else {
                    Log.d("VetReviewFragment", "Found ${documents.size()} reviews.")
                }

                // Clear existing reviews and add fetched reviews
                requireActivity().runOnUiThread {
                    reviews.clear()
                    for (document in documents) {
                        val reviewerName = document.getString("reviewerName") ?: ""
                        val reviewText = document.getString("reviewText") ?: ""
                        val reviewDate = document.getString("reviewDate") ?: ""

                        reviews.add(hashMapOf(
                            "reviewerName" to reviewerName,
                            "reviewText" to reviewText,
                            "reviewDate" to reviewDate
                        ))
                    }
                    adapter.notifyDataSetChanged()  // Notify adapter that the data set has changed
                }
            }
            .addOnFailureListener { e ->
                Log.e("VetReviewFragment", "Error fetching reviews", e)
            }
    }
}
