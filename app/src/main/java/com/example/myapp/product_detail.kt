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

    // Firebase Firestore reference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // Assume you have a product object initialized with the current product details
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

            // Create the current product
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
        }

        // Setup the similar products container
        val similarProductsContainer: LinearLayout = view.findViewById(R.id.similarProductsContainer)
        fetchSimilarProducts(similarProductsContainer)

        return view
    }

    private fun fetchSimilarProducts(productContainer: LinearLayout) {
        val db = FirebaseFirestore.getInstance()

        // Example: Fetch products based on the category of the current product
        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productContainer.removeAllViews() // Clear existing views
                val inflater = LayoutInflater.from(requireContext())
                var rowLayout: LinearLayout? = null
                var productCount = 0

                for (document in documents) {
                    val productData = document.data
                    val productCard = inflater.inflate(R.layout.product_card, productContainer, false)

                    // Set product details
                    val productName = productData["name"].toString()
                    val productPrice = productData["price"]?.toString() ?: "0"
                    val productDescription = productData["description"].toString()
                    val productImageUrl = productData["imageUrl"].toString()

                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productName
                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹$productPrice"
                    productCard.findViewById<TextView>(R.id.dynamicDes).text = productDescription

                    // Load product image using Glide
                    Glide.with(requireContext())
                        .load(productImageUrl)
                        .into(productCard.findViewById(R.id.imageView))

                    // Handle "Buy Now" button click
                    val buyNowButton = productCard.findViewById<Button>(R.id.buyNowButton)
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
                                        val product = Product(
                                            name = productName,
                                            price = productPrice.toInt(),
                                            description = productDescription,
                                            imageUrl = productImageUrl,
                                            quantity = 1 // Default quantity is 1
                                        )
                                        saveProductToFirestore(product)
                                    } else {
                                        Toast.makeText(requireContext(), "Product already in cart", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(requireContext(), "User not authenticated!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // Create rows with two cards each
                    if (productCount % 2 == 0) {
                        rowLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 8, 0, 8) // Row spacing
                            }
                        }
                        productContainer.addView(rowLayout)
                    }

                    // Add card to row with equal width
                    val layoutParams = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f // Equal weight for each card
                    ).apply {
                        setMargins(0, 8, 8, 8) // Card spacing
                    }
                    productCard.layoutParams = layoutParams
                    rowLayout?.addView(productCard)

                    productCount++
                }
            }
            .addOnFailureListener { e ->
                Log.e("ProductDetailFragment", "Error fetching similar products", e)
                Toast.makeText(requireContext(), "Error fetching similar products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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