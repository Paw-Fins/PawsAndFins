package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton

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

        // Fetch and display products dynamically
        fetchProducts(productContainer)

        return view
    }

    // Fetch product data from Firestore and display them
    private fun fetchProducts(productContainer: LinearLayout) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productContainer.removeAllViews() // Clear existing views
                val inflater = LayoutInflater.from(requireContext())
                var rowLayout: LinearLayout? = null
                var productCount = 0

                for ((index, document) in documents.withIndex()) {
                    val productData = document.data
                    val productCard = inflater.inflate(R.layout.product_card, productContainer, false)

                    // Set product details
                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productData["name"].toString()
                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "$${productData["price"].toString()}"
                    val descriptionTextView: TextView = productCard.findViewById(R.id.dynamicDes)
                    descriptionTextView.text = productData["description"].toString()

                    // Load product image using Glide
                    Glide.with(requireContext())
                        .load(productData["imageUrl"].toString())
                        .into(productCard.findViewById(R.id.imageView))

                    // Add the product card to the row
                    if (productCount % 2 == 0) {
                        rowLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 13)
                            }
                        }
                        productContainer.addView(rowLayout)
                    }

                    rowLayout?.addView(productCard, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    productCount++
                }
            }
            .addOnFailureListener { e ->
                Log.e("HomeScreenFragment", "Error fetching products", e)
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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
                    val imageUrl = document.getString("imageUrl")
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

    // Data class to represent a product
    data class Product(
        val name: String = "",
        val price: Int = 0,
        val description: String = "",
        val imageUrl: String = ""
    )
}
