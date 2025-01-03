package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProductFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var filterSpinner: Spinner

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_product, container, false)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Setup the filter spinner
        filterSpinner = view.findViewById(R.id.filterSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options, // Ensure this array is defined in res/values/strings.xml
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        // Layout for displaying products
        val productContainer: LinearLayout = view.findViewById(R.id.productContainerPage)

        // Fetch and display products dynamically
        fetchProducts(productContainer)

        // Set up the filter selection listener
        filterSpinner.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedFilter = filterSpinner.getItemAtPosition(position).toString()
                fetchProducts(productContainer, selectedFilter)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing or handle the case when nothing is selected
            }
        })

        return view
    }

    private fun fetchProducts(productContainer: LinearLayout, filter: String? = null) {
        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productContainer.removeAllViews() // Clear existing views
                val inflater = LayoutInflater.from(requireContext())

                for (document in documents) {
                    val productData = document.data
                    val productName = productData["name"].toString()
                    val productPrice = productData["price"]?.toString() ?: "$0"
                    val productDescription = productData["description"].toString()
                    val productImageUrl = productData["imageUrl"].toString()
                    val productId = document.id // Get the document ID to pass as a product identifier

                    // Apply filter logic here
                    if (filter == null || productName.contains(filter, ignoreCase = true)) {
                        val productCard = inflater.inflate(R.layout.product_card, productContainer, false)

                        productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
                        productCard.findViewById<TextView>(R.id.dynamicPrice).text = "$$productPrice"
                        productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription

                        // Load image using Glide
                        val imageView = productCard.findViewById<ImageView>(R.id.imageView)
                        Glide.with(requireContext())
                            .load(productImageUrl)
                            .into(imageView)

                        // Handle "Buy Now" button click
                        productCard.findViewById<Button>(R.id.buyNowButton).setOnClickListener {
                            val currentUser  = auth.currentUser
                            if (currentUser  != null) {
                                val userId = currentUser .uid
                                firestore.collection("orders")
                                    .whereEqualTo("userId", userId)
                                    .whereEqualTo("name", productName)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        if (querySnapshot.isEmpty) {
                                            val product = Product(
                                                name = productName,
                                                price = productPrice.toInt(),
                                                description = productDescription,
                                                imageUrl = productImageUrl,
                                                quantity = 1 // Default quantity is 1
                                            )
                                            saveProductToFirestore(product)
                                        } else {
                                            Toast.makeText(
                                                requireContext(),
                                                "Product already in cart",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(requireContext(), "User  not authenticated!", Toast.LENGTH_SHORT).show()
                            }
                        }

                        // Handle "View More" button click
                        productCard.findViewById<Button>(R.id.viewMoreButton).setOnClickListener {
                            // Navigate to ProductDetailFragment with product data
                            val bundle = Bundle().apply {
                                putString("product_id", productId) // Pass the product ID
                                putString("product_name", productName)
                                putString("product_price", productPrice)
                                putString("product_desc", productDescription)
                                putString("product_image_url", productImageUrl)
                            }
                            val productDetail = ProductDetailFragment().apply {
                                arguments = bundle
                            }
                            requireActivity().supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, productDetail)
                                .addToBackStack(null) // Add to back stack to allow navigation back
                                .commit()
                        }

                        // Add the product card to the container
                        productContainer.addView(productCard)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("ProductFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching products: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to save product details to Firestore
    private fun saveProductToFirestore(product: Product) {
        val db = FirebaseFirestore.getInstance()
        val currentUser  = auth.currentUser

        if (currentUser  != null) {
            val userId = currentUser .uid
            val orderData = hashMapOf(
                "name" to product.name,
                "price" to product.price,
                "description" to product.description,
                "imageUrl" to product.imageUrl,
                "quantity" to product.quantity,
                "userId" to userId,
                "timestamp" to System.currentTimeMillis() // Optional: To track when the order was placed
            )

            db.collection("orders") // Create or use an "orders" collection
                .add(orderData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Product added to orders!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to add product: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User  not authenticated!", Toast.LENGTH_SHORT).show()
        }
    }

    // Data class to represent a product
    data class Product(
        val name: String = "",
        val price: Int = 0,
        val description: String = "",
        val imageUrl: String = "",
        val quantity: Int = 1
    )
}
