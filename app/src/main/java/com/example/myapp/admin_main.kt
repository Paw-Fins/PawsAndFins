package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore

class AdminScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_main, container, false)
        val productContainer: LinearLayout = view.findViewById(R.id.adminProductContainer)

        // Fetch and display products dynamically
        fetchProducts(productContainer)

        // Handle Add Product button click
        val btnAddProduct: MaterialButton = view.findViewById(R.id.btnAddProduct)
        btnAddProduct.setOnClickListener {
            val addProductFragment = AddProductFragment() // Replace with your fragment class
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addProductFragment) // Replace with your container ID
                .addToBackStack(null) // Optional: To allow navigation back
                .commit()
        }

        // Fetch and update user count and product count dynamically
        fetchUserCount(view)
        fetchProductCount(view)

        return view
    }

    // Fetch product data from Firestore
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
                    val productCard = inflater.inflate(R.layout.admin_product_card, productContainer, false)

                    // Set product details
                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productData["name"].toString()
                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹${productData["price"].toString()}"

                    // Handle description with dynamic multiline support
                    val descriptionTextView: TextView = productCard.findViewById(R.id.dynamicDescription)
                    descriptionTextView.text = productData["description"].toString()
                    descriptionTextView.maxLines = 3 // Max 3 lines shown, can expand if required
                    descriptionTextView.ellipsize = android.text.TextUtils.TruncateAt.END

                    // Load product image using Glide
                    Glide.with(requireContext())
                        .load(productData["imageUrl"].toString())
                        .placeholder(R.drawable.dummy_product) // Placeholder in case image is loading or fails
                        .into(productCard.findViewById(R.id.imageView))

                    // Add delete functionality
                    productCard.findViewById<View>(R.id.deleteButton).setOnClickListener {
                        deleteProduct(document.id, productContainer)
                    }

                    // Add product card to the row
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
                Log.e("AdminScreenFragment", "Error fetching products", e)
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch number of users from Firestore and update the UI
    private fun fetchUserCount(view: View) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userCount = documents.size()
                view.findViewById<TextView>(R.id.registeredUsersCount).text = userCount.toString()
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error fetching users", e)
                Toast.makeText(requireContext(), "Error fetching users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch number of products from Firestore and update the UI
    private fun fetchProductCount(view: View) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val productCount = documents.size()
                view.findViewById<TextView>(R.id.totalProductsCount).text = productCount.toString()
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error fetching products", e)
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete a product from Firestore
    private fun deleteProduct(productId: String, productContainer: LinearLayout) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product deleted", Toast.LENGTH_SHORT).show()
                fetchProducts(productContainer) // Refresh product list
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error deleting product", e)
                Toast.makeText(requireContext(), "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
