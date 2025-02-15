package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
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
        val progressBar: ProgressBar? = view?.findViewById(R.id.productProgressBar)

        // Show progress bar if it exists
        progressBar?.visibility = View.VISIBLE

        firestore.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                Log.d("ProductFragment", "Fetched ${documents.size()} products")

                val inflater = LayoutInflater.from(requireContext())

                // Clear previous views to prevent duplicates
                productContainer.removeAllViews()

                if (documents.isEmpty()) {
                    Log.d("ProductFragment", "No products available")
                    Toast.makeText(requireContext(), "No products available", Toast.LENGTH_SHORT).show()
                } else {
                    var rowLayout: LinearLayout? = null
                    var count = 0

                    for (document in documents) {
                        val productData = document.data
                        val productName = productData["name"].toString()
                        val productPrice = productData["price"]?.toString() ?: "$0"
                        val productDescription = productData["description"].toString()
                        val productImageUrl = productData["imageUrl"].toString()
                        val productId = document.id
                        val productCategory = productData["category"]?.toString()?.trim()?.lowercase() ?: ""

                        if (filter.isNullOrEmpty() || filter.equals("All", ignoreCase = true) || filter.trim().equals(productCategory, ignoreCase = true)) {
                            if (count % 2 == 0) {
                                rowLayout = LinearLayout(requireContext()).apply {
                                    orientation = LinearLayout.HORIZONTAL
                                    layoutParams = LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                    ).apply { setMargins(0, 0, 0, 8) }
                                }
                                productContainer.addView(rowLayout)
                            }

                            val productCard = inflater.inflate(R.layout.product_card, rowLayout, false)
                            productCard.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                                setMargins(if (count % 2 == 0) 0 else 8, 0, 0, 0)
                            }

                            productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
                            productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹$productPrice"
                            productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription

                            val imageView = productCard.findViewById<ImageView>(R.id.imageView)
                            Glide.with(requireContext())
                                .load(productImageUrl)
                                .placeholder(R.drawable.dummy_product)
                                .error(R.drawable.dummy_product)
                                .into(imageView)

                            rowLayout?.addView(productCard)
                            count++
                        }
                    }
                }

                // Hide progress bar if it exists
                progressBar?.visibility = View.GONE
            }
            .addOnFailureListener { exception ->
                Log.w("ProductFragment", "Error getting documents: ", exception)
                Toast.makeText(requireContext(), "Error fetching products: ${exception.message}", Toast.LENGTH_SHORT).show()

                // Hide progress bar in case of error
                progressBar?.visibility = View.GONE
            }
    }


    // Function to save product details to Firestore
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
            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
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
