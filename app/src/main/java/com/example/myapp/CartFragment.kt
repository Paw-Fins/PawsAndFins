package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton

class CartFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cartContainer: LinearLayout
    private var totalPrice: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        cartContainer = view.findViewById(R.id.cartContainer)
        val payButton: MaterialButton = view.findViewById(R.id.payButton)

        // Fetch the user's cart items
        fetchCartItems()

        payButton.setOnClickListener {
            // Handle payment logic here (this can be linked to a payment gateway)
            Toast.makeText(requireContext(), "Proceeding to Payment...", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun fetchCartItems() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("orders")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    cartContainer.removeAllViews() // Clear existing cart items

                    totalPrice = 0
                    for (document in documents) {
                        val product = document.toObject(Product::class.java)
                        addProductToCart(product, document.id)
                    }

                    // Update total price display
                    updateTotalPrice()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching cart items: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addProductToCart(product: Product, orderId: String) {
        val inflater = LayoutInflater.from(requireContext())
        val productView = inflater.inflate(R.layout.cart_item, cartContainer, false)

        // Set product details
        val productName: TextView = productView.findViewById(R.id.productName)
        val productPrice: TextView = productView.findViewById(R.id.productPrice)
        val productQuantity: TextView = productView.findViewById(R.id.productQuantity)
        val productImage: ImageView = productView.findViewById(R.id.productImage)

        productName.text = product.name
        productPrice.text = "₹${product.price}"
        productQuantity.text = product.quantity.toString()

        Glide.with(requireContext())
            .load(product.imageUrl)
            .into(productImage)

        // Handle Quantity change
        val minusButton: MaterialButton = productView.findViewById(R.id.minusButton)
        val plusButton: MaterialButton = productView.findViewById(R.id.plusButton)

        minusButton.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity -= 1
                productQuantity.text = product.quantity.toString()
                updateProductQuantity(orderId, product.quantity)
                // Reload the cart after updating quantity
                fetchCartItems()
            } else {
                // Show toast if quantity is 1 and user tries to decrease
                Toast.makeText(requireContext(), "Quantity cannot be 0", Toast.LENGTH_SHORT).show()
            }
        }

        plusButton.setOnClickListener {
            product.quantity += 1
            productQuantity.text = product.quantity.toString()
            updateProductQuantity(orderId, product.quantity)
            // Reload the cart after updating quantity
            fetchCartItems()
        }

        // Handle Remove Product
        val removeButton: MaterialButton = productView.findViewById(R.id.removeButton)
        removeButton.setOnClickListener {
            removeProductFromCart(orderId)
            // Reload the cart after removing the product
            fetchCartItems()
        }

        // Add to cart container
        cartContainer.addView(productView)

        // Update total price for this product
        totalPrice += product.price * product.quantity
    }

    private fun updateTotalPrice() {
        val totalTextView: TextView = requireView().findViewById(R.id.totalPrice)
        totalTextView.text = "Total: ₹$totalPrice"
    }

    private fun updateProductQuantity(orderId: String, newQuantity: Int) {
        firestore.collection("orders")
            .document(orderId)
            .update("quantity", newQuantity)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Quantity updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating quantity: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProductFromCart(orderId: String) {
        firestore.collection("orders")
            .document(orderId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Product removed from cart", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error removing product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    data class Product(
        val name: String = "",
        val price: Int = 0,
        val imageUrl: String = "",
        var quantity: Int = 1
    )
}
