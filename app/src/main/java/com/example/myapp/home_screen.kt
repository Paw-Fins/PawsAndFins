package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
//        val logoImageView: ImageView = view.findViewById(R.id.profile_circle)

        // Fetch the image URL based on the currently logged-in user's UID
//        fetchImageUrl { imageUrl ->
//            if (imageUrl != null) {
//                // Load the image from the URL using Glide
//                Glide.with(requireContext())
//                    .load(imageUrl)
//                    .into(logoImageView)
//            } else {
//                // Set the default image if no URL is available
//                logoImageView.setImageResource(R.drawable.profile_circle)
//            }
//        }

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
//    private fun fetchProducts(productContainer: LinearLayout) {
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection("products")
//            .get()
//            .addOnSuccessListener { documents ->
//                productContainer.removeAllViews() // Clear existing views
//                val inflater = LayoutInflater.from(requireContext())
//                var rowLayout: LinearLayout? = null
//                var productCount = 0
//
//                for ((index, document) in documents.withIndex()) {
//                    val productData = document.data
//                    val productCard = inflater.inflate(R.layout.product_card, productContainer, false)
//
//                    // Set product details
//                    val productName = productData["name"].toString()
//                    val productPrice = productData["price"]?.toString() ?: "0"
//                    val productDescription = productData["description"].toString()
//                    val productImageUrl = productData["imageUrl"].toString()
//
//                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
//                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹$productPrice"
//                    productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription
//
//                    // Load product image using Glide
//                    Glide.with(requireContext())
//                        .load(productImageUrl)
//                        .into(productCard.findViewById(R.id.imageView))
//
//                    // Handle "Buy Now" button click
//                    val buyNowButton = productCard.findViewById<MaterialButton>(R.id.buyNowButton)
//                    buyNowButton.setOnClickListener {
//                        // Check if the product is already in the cart
//                        val currentUser = auth.currentUser
//                        if (currentUser != null) {
//                            val userId = currentUser.uid
//
//                            // Query Firestore to check if the product already exists in the user's cart
//                            firestore.collection("orders")
//                                .whereEqualTo("userId", userId)
//                                .whereEqualTo("name", productName)
//                                .get()
//                                .addOnSuccessListener { querySnapshot ->
//                                    if (querySnapshot.isEmpty) {
//                                        // Product not in the cart, add it with quantity 1
//                                        val product = Product(
//                                            name = productName,
//                                            price = productPrice.toInt(),
//                                            description = productDescription,
//                                            imageUrl = productImageUrl,
//                                            quantity = 1 // Set default quantity to 1
//                                        )
//                                        saveProductToFirestore(product)
//                                    } else {
//                                        // Product already in the cart
//                                        Toast.makeText(requireContext(), "Product already in cart", Toast.LENGTH_SHORT).show()
//                                    }
//                                }
//                        } else {
//                            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    // Add the product card to the row
//                    if (productCount % 2 == 0) {
//                        rowLayout = LinearLayout(requireContext()).apply {
//                            orientation = LinearLayout.HORIZONTAL
//                            layoutParams = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT
//                            ).apply {
//                                setMargins(0, 0, 0, 13)
//                            }
//                        }
//                        productContainer.addView(rowLayout)
//                    }
//
//                    rowLayout?.addView(productCard, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
//                    productCount++
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("HomeScreenFragment", "Error fetching products", e)
//                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//    }

//    private fun fetchProducts(productContainer: LinearLayout) {
//        val db = FirebaseFirestore.getInstance()
//
//        db.collection("products")
//            .get()
//            .addOnSuccessListener { documents ->
//                productContainer.removeAllViews() // Clear existing views
//                val inflater = LayoutInflater.from(requireContext())
//                var rowLayout: LinearLayout? = null
//                var productCount = 0
//
//                for ((index, document) in documents.withIndex()) {
//                    val productData = document.data
//                    val productCard = inflater.inflate(R.layout.product_card, productContainer, false)
//
//                    // Set product details
//                    val productName = productData["name"].toString()
//                    val productPrice = productData["price"]?.toString() ?: "0"
//                    val productDescription = productData["description"].toString()
//                    val productImageUrl = productData["imageUrl"].toString()
//
//                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
//                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹$productPrice"
//                    productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription
//
//                    // Load product image using Glide
//                    Glide.with(requireContext())
//                        .load(productImageUrl)
//                        .into(productCard.findViewById(R.id.imageView))
//
//                    // Handle "Buy Now" button click
//                    val buyNowButton = productCard.findViewById<MaterialButton>(R.id.buyNowButton)
//                    buyNowButton.setOnClickListener {
//                        val currentUser = auth.currentUser
//                        if (currentUser != null) {
//                            val userId = currentUser.uid
//                            firestore.collection("orders")
//                                .whereEqualTo("userId", userId)
//                                .whereEqualTo("name", productName)
//                                .get()
//                                .addOnSuccessListener { querySnapshot ->
//                                    if (querySnapshot.isEmpty) {
//                                        val product = Product(
//                                            name = productName,
//                                            price = productPrice.toInt(),
//                                            description = productDescription,
//                                            imageUrl = productImageUrl,
//                                            quantity = 1 // Default quantity is 1
//                                        )
//                                        saveProductToFirestore(product)
//                                    } else {
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "Product already in cart",
//                                            Toast.LENGTH_SHORT
//                                        ).show()
//                                    }
//                                }
//                        } else {
//                            Toast.makeText(
//                                requireContext(),
//                                "User not authenticated!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//
//                    // Create rows with two cards each
//                    if (productCount % 2 == 0) {
//                        rowLayout = LinearLayout(requireContext()).apply {
//                            orientation = LinearLayout.HORIZONTAL
//                            layoutParams = LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT
//                            ).apply {
//                                setMargins(0, 8, 0, 8) // Row spacing
//                            }
//                        }
//                        productContainer.addView(rowLayout)
//                    }
//
//                    // Add card to row with equal width
//                    val layoutParams = LinearLayout.LayoutParams(
//                        0,
//                        LinearLayout.LayoutParams.WRAP_CONTENT,
//                        1f // Equal weight for each card
//                    ).apply {
//                        setMargins(0, 8, 8, 8) // Card spacing
//                    }
//                    productCard.layoutParams = layoutParams
//                    rowLayout?.addView(productCard)
//
//                    productCount++
//                }
//            }
//            .addOnFailureListener { e ->
//                Log.e("HomeScreenFragment", "Error fetching products", e)
//                Toast.makeText(
//                    requireContext(),
//                    "Error fetching products: ${e.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//    }
private fun fetchProducts(productContainer: LinearLayout) {
    val db = FirebaseFirestore.getInstance()

    db.collection("products")
        .get()
        .addOnSuccessListener { documents ->
            productContainer.removeAllViews() // Clear existing views
            val inflater = LayoutInflater.from(requireContext())

            for (document in documents) {
                val productData = document.data
                val productCard = inflater.inflate(R.layout.product_card, productContainer, false)

                // Set product details
                val productName = productData["name"].toString()
                val productPrice = productData["price"]?.toString() ?: "$0"
                val productDescription = productData["description"].toString()
                val productImageUrl = productData["imageUrl"].toString()
                val productId = document.id // Get the document ID to pass as a product identifier

                productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
                productCard.findViewById<TextView>(R.id.dynamicPrice).text = "$$productPrice"
                productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription

                // Load image using Glide or similar library
                val imageView = productCard.findViewById<ImageView>(R.id.imageView)
                Glide.with(requireContext())
                    .load(productImageUrl)
                    .into(imageView)

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
                    }
                }

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

                productContainer.addView(productCard)
            }
        }
        .addOnFailureListener { exception ->
            // Handle any errors
            Log.w( "Error getting documents: ", exception)
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

    // Function to fetch the image URL from Firestore
//    private fun fetchImageUrl(callback: (String?) -> Unit) {
//        val user = auth.currentUser
//        if (user != null) {
//            val userUid = user.uid // Get the logged-in user's UID
//            Log.d("HomeScreenFragment", "User ID: $userUid")
//
//            val userRef = firestore.collection("users").document(userUid)
//
//            userRef.get().addOnSuccessListener { document ->
//                if (document.exists()) {
//                    val imageUrl = document.getString("imageUrl")
//                    Log.d("HomeScreenFragment", "Fetched Image URL: $imageUrl")
//                    callback(imageUrl) // Pass the image URL to the callback
//                } else {
//                    Log.w("HomeScreenFragment", "No document found for user: $userUid")
//                    callback(null) // No document found
//                }
//            }.addOnFailureListener { exception ->
//                Log.e("HomeScreenFragment", "Error fetching image URL: ${exception.message}")
//                callback(null) // Handle Firestore error
//            }
//        } else {
//            Log.w("HomeScreenFragment", "No authenticated user found.")
//            callback(null) // No logged-in user
//        }
//    }

    // Data class to represent a product
    data class Product(
        val name: String = "",
        val price: Int = 0,
        val description: String = "",
        val imageUrl: String = "",
        val quantity: Int = 1 // Added quantity field with default value of 1
    )
}