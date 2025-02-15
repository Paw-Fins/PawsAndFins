package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductDetailFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentProduct: Product

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_product_detail, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Retrieve product information from the arguments
        val bundle = arguments
        if (bundle != null) {
            val productName = bundle.getString("product_name") ?: "Unknown Product"
            val productPrice = bundle.getString("product_price")?.toIntOrNull() ?: 0
            val productDesc = bundle.getString("product_desc") ?: "No Description"
            val productImageUrl = bundle.getString("product_image_url") ?: ""
            val productCategory = bundle.getString("product_category") ?: "Uncategorized"

            currentProduct = Product(
                name = productName,
                price = productPrice,
                description = productDesc,
                imageUrl = productImageUrl,
                category = productCategory
            )

            // Set product details to the views (make sure these IDs exist in your layout)
            view.findViewById<TextView>(R.id.productTitle).text = productName
            view.findViewById<TextView>(R.id.productPrice).text = "₹$productPrice"
            view.findViewById<TextView>(R.id.productDescription).text = productDesc

            // Load product image using Glide
            Glide.with(requireContext())
                .load(productImageUrl)
                .into(view.findViewById(R.id.productImage))

            // Handle "Buy Now" button click
            val buyNowButton = view.findViewById<Button>(R.id.buyNowButton)
            buyNowButton.setOnClickListener {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val userId = currentUser.uid
                    firestore.collection("orders")
                        .whereEqualTo("userId", userId)
                        .whereEqualTo("name", productName)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (querySnapshot.isEmpty) {
                                // Product is not in the cart, so add it
                                val product = Product(
                                    name = productName,
                                    price = productPrice,
                                    description = productDesc,
                                    imageUrl = productImageUrl,
                                    quantity = 1 // Default quantity is 1
                                )
                                saveProductToFirestore(product)
                            } else {
                                // Product is already in the cart
                                Toast.makeText(requireContext(), "Product already in cart", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    // User is not authenticated
                    Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Return the view
        return view
    }

    private fun saveProductToFirestore(product: Product) {
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val orderData = hashMapOf(
                "name" to product.name,
                "price" to product.price,
                "description" to product.description,
                "imageUrl" to product.imageUrl,
                "quantity" to product.quantity,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("orders")
                .add(orderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Product added to orders!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Data class to represent a product
    data class Product(
        val name: String = "",
        val price: Int = 0,
        val description: String = "",
        val imageUrl: String = "",
        val quantity: Int = 1,
        val category: String = ""
    )
}
