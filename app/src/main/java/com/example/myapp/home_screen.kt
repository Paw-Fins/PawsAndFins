package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import android.util.Log

class HomeScreenFragment : Fragment() {

    // Firebase Firestore reference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Get the ImageView to set the profile image
        val logoImageView: ImageView = view.findViewById(R.id.logoImage)

        // Fetch the image URL based on the currently logged-in user's UID
        fetchImageUrl { imageUrl ->
            if (imageUrl != null) {
                // Load the image from the URL using Glide
                Glide.with(requireContext())
                    .load(imageUrl)
                    .into(logoImageView)
            } else {
                // Set the default image if no URL is available
                logoImageView.setImageResource(R.drawable.profile_circle)
            }
        }

        // Setup the filter spinner
        val filterSpinner: Spinner = view.findViewById(R.id.filterSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        // Show bottom navigation
        (requireActivity() as MainActivity).showBottomNavigation(true)

        // Layout for displaying products
        val productContainer: LinearLayout = view.findViewById(R.id.productContainer)
        val inflater = LayoutInflater.from(requireContext())
        val n = 5

        // Generate product rows dynamically
        for (i in 0 until n step 2) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val params = layoutParams as LinearLayout.LayoutParams
                params.setMargins(0, 0, 0, 13) // 13px margin at the bottom for row spacing
                layoutParams = params
            }

            val productCard1 = inflater.inflate(R.layout.product_card, rowLayout, false)
            val params1 = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params1.setMargins(0, 0, 16, 0)
            productCard1.layoutParams = params1
            rowLayout.addView(productCard1)

            if (i + 1 < n) {
                val productCard2 = inflater.inflate(R.layout.product_card, rowLayout, false)
                val params2 = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                productCard2.layoutParams = params2
                rowLayout.addView(productCard2)
            }

            productContainer.addView(rowLayout)
        }

        return view
    }

    // Function to fetch the image URL from Firestore
    private fun fetchImageUrl(callback: (String?) -> Unit) {
        val user = auth.currentUser
        if (user != null) {
            val userUid = user.uid // Get the logged-in user's UID
            Log.d("HomeScreenFragment", "User ID: $userUid")

            val userRef = firestore.collection("users").document(userUid)

            userRef.get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val imageUrl = document.getString("imageUrl") // Assuming the image URL is stored in the "imageUrl" field
                    Log.d("HomeScreenFragment", "Fetched Image URL: $imageUrl")
                    callback(imageUrl) // Pass the image URL to the callback
                } else {
                    Log.w("HomeScreenFragment", "No document found for user: $userUid")
                    callback(null) // No document found
                }
            }.addOnFailureListener { exception ->
                Log.e("HomeScreenFragment", "Error fetching image URL: ${exception.message}")
                callback(null) // Handle Firestore error
            }
        } else {
            Log.w("HomeScreenFragment", "No authenticated user found.")
            callback(null) // No logged-in user
        }
    }
}
