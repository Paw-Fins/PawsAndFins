package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        val filterSpinner: Spinner = view.findViewById(R.id.filterSpinner)
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            R.layout.spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter
        (requireActivity() as MainActivity).showBottomNavigation(true)
        val productContainer: LinearLayout = view.findViewById(R.id.productContainer)
        fetchProducts(productContainer)

        return view
    }

//private fun fetchProducts(productContainer: LinearLayout) {
//    val db = FirebaseFirestore.getInstance()
//
//    db.collection("products")
//        .get()
//        .addOnSuccessListener { documents ->
//            productContainer.removeAllViews() // Clear existing views
//            val inflater = LayoutInflater.from(requireContext())
//
//            for (document in documents) {
//                val productData = document.data
//                val productCard = inflater.inflate(R.layout.product_card, productContainer, false)
//
//                // Set product details
//                val productName = productData["name"].toString()
//                val productPrice = productData["price"]?.toString() ?: "$0"
//                val productDescription = productData["description"].toString()
//                val productImageUrl = productData["imageUrl"].toString()
//                val productId = document.id // Get the document ID to pass as a product identifier
//
//                productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
//                productCard.findViewById<TextView>(R.id.dynamicPrice).text = "$$productPrice"
//                productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription
//
//                // Load image using Glide or similar library
//                val imageView = productCard.findViewById<ImageView>(R.id.imageView)
//                Glide.with(requireContext())
//                    .load(productImageUrl)
//                    .into(imageView)
//
//                productCard.findViewById<Button>(R.id.buyNowButton).setOnClickListener {
//                    val currentUser = auth.currentUser
//                    if (currentUser != null) {
//                        val userId = currentUser.uid
//                        firestore.collection("orders")
//                            .whereEqualTo("userId", userId)
//                            .whereEqualTo("name", productName)
//                            .get()
//                            .addOnSuccessListener { querySnapshot ->
//                                if (querySnapshot.isEmpty) {
//                                    val product = Product(
//                                        name = productName,
//                                        price = productPrice.toInt(),
//                                        description = productDescription,
//                                        imageUrl = productImageUrl,
//                                        quantity = 1 // Default quantity is 1
//                                    )
//                                    saveProductToFirestore(product)
//                                } else {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        "Product already in cart",
//                                        Toast.LENGTH_SHORT
//                                    ).show()
//                                }
//                            }
//                    }
//                }
//
//                productCard.findViewById<Button>(R.id.viewMoreButton).setOnClickListener {
//                    val bundle = Bundle().apply {
//                        putString("product_id", productId) // Pass the product ID
//                        putString("product_name", productName)
//                        putString("product_price", productPrice)
//                        putString("product_desc", productDescription)
//                        putString("product_image_url", productImageUrl)
//                    }
//                    val productDetail = ProductDetailFragment().apply {
//                        arguments = bundle
//                    }
//                    requireActivity().supportFragmentManager.beginTransaction()
//                        .replace(R.id.fragment_container, productDetail)
//                        .addToBackStack(null) // Add to back stack to allow navigation back
//                        .commit()
//
//                }
//
//                productContainer.addView(productCard)
//            }
//        }
//        .addOnFailureListener { exception ->
//            // Handle any errors
//            Log.w( "Error getting documents: ", exception)
//        }
//}
private fun fetchProducts(productContainer: LinearLayout) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    productContainer.removeAllViews()

    val gridLayout = GridLayout(requireContext()).apply {
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        columnCount = 2 // 2-column grid layout
    }
    productContainer.addView(gridLayout)

    db.collection("products")
        .get()
        .addOnSuccessListener { documents ->
            val inflater = LayoutInflater.from(requireContext())

            for (document in documents) {
                val productData = document.data
                val productCard = inflater.inflate(R.layout.product_card, gridLayout, false)

                // Get product details
                val productName = productData["name"].toString()
                val productPrice = productData["price"]?.toString() ?: "0"
                val productDescription = productData["description"].toString()
                val productImageUrl = productData["imageUrl"].toString()
                val productId = document.id

                // Set product details in UI
                productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
                productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹$productPrice"
                productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription

                // Load image using Glide
                val imageView = productCard.findViewById<ImageView>(R.id.imageView)
                Glide.with(requireContext())
                    .load(productImageUrl)
                    .centerCrop()
                    .into(imageView)

                // Buy Now button click listener
                productCard.findViewById<Button>(R.id.buyNowButton).setOnClickListener {
                    val currentUser = auth.currentUser
                    if (currentUser != null) {
                        val userId = currentUser.uid
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
                                        quantity = 1
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
                    }
                }

                // View More button click listener
                productCard.findViewById<Button>(R.id.viewMoreButton).setOnClickListener {
                    val bundle = Bundle().apply {
                        putString("product_id", productId)
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
                        .addToBackStack(null)
                        .commit()
                }

                // Set layout parameters (without margins and paddings)
                val params = GridLayout.LayoutParams().apply {
                    width = 0
                    height = GridLayout.LayoutParams.WRAP_CONTENT
                    columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f) // Equal width, no margin
                }
                productCard.layoutParams = params

                gridLayout.addView(productCard) // Add card to grid layout
            }
        }
        .addOnFailureListener { exception ->
            Log.w("Error getting products: ", exception)
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