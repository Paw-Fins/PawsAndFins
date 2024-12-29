import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton
import androidx.navigation.fragment.findNavController
import com.example.myapp.R
import com.bumptech.glide.Glide

class CartFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var cartContainer: LinearLayout
    private var totalPrice: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_cart, container, false)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        cartContainer = view.findViewById(R.id.cartContainer)
        val checkoutButton: MaterialButton = view.findViewById(R.id.checkoutButton)

        // Fetch the user's cart items
        fetchCartItems()

        checkoutButton.setOnClickListener {
            if (totalPrice > 0) {
                navigateToDeliveryAddress()
            } else {
                Toast.makeText(requireContext(), "Cart is empty or invalid total.", Toast.LENGTH_SHORT).show()
            }
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
                    cartContainer.removeAllViews()

                    totalPrice = 0
                    for (document in documents) {
                        val product = document.toObject(Product::class.java)
                        addProductToCart(product, document.id)
                    }
                    updateTotalPrice()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching cart items: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun addProductToCart(product: Product, documentId: String) {
        val inflater = LayoutInflater.from(requireContext())
        val productView = inflater.inflate(R.layout.cart_item, cartContainer, false)

        val productName = productView.findViewById<TextView>(R.id.productName)
        val productPrice = productView.findViewById<TextView>(R.id.productPrice)
        val productQuantity = productView.findViewById<TextView>(R.id.productQuantity)
        val increaseQuantityButton = productView.findViewById<MaterialButton>(R.id.plusButton)
        val decreaseQuantityButton = productView.findViewById<MaterialButton>(R.id.minusButton)
        val removeButton = productView.findViewById<MaterialButton>(R.id.removeButton)
        val productImage = productView.findViewById<ImageView>(R.id.productImage)

        // Set the text values for product name, price, and quantity
        productName.text = product.name
        productPrice.text = "₹${product.price}"
        productQuantity.text = product.quantity.toString()

        // Load the product image using Glide
        Glide.with(this)
            .load(product.imageUrl)  // Load image from Firestore data
            .placeholder(R.drawable.dummy_product)  // Optional placeholder
            .into(productImage)

        // Add event listener for increase quantity
        increaseQuantityButton.setOnClickListener {
            product.quantity++
            productQuantity.text = product.quantity.toString()
            updateProductInFirestore(documentId, product)
        }

        // Add event listener for decrease quantity
        decreaseQuantityButton.setOnClickListener {
            if (product.quantity > 1) {
                product.quantity--
                productQuantity.text = product.quantity.toString()
                updateProductInFirestore(documentId, product)
            } else {
                Toast.makeText(requireContext(), "Quantity cannot be less than 1", Toast.LENGTH_SHORT).show()
            }
        }

        // Add event listener for removing product
        removeButton.setOnClickListener {
            removeProductFromCart(documentId)
        }

        // Add the product to the cart container
        cartContainer.addView(productView)
        totalPrice += product.price * product.quantity
    }

    private fun updateTotalPrice() {
        val totalTextView = requireView().findViewById<TextView>(R.id.totalPrice)
        totalTextView.text = "Total: ₹$totalPrice"
    }

    private fun updateProductInFirestore(documentId: String, product: Product) {
        firestore.collection("orders")
            .document(documentId)
            .update(
                "quantity", product.quantity
            ).addOnSuccessListener {
                // Re-fetch cart to reflect the updates in real-time
                fetchCartItems()
            }.addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error updating cart: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProductFromCart(documentId: String) {
        firestore.collection("orders")
            .document(documentId)
            .delete()
            .addOnSuccessListener {
                // Refresh the cart after product removal
                Toast.makeText(requireContext(), "Product removed from cart", Toast.LENGTH_SHORT).show()
                fetchCartItems()  // Re-fetch to reload the updated cart
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error removing product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun navigateToDeliveryAddress() {
        val deliveryFragment = DeliveryAddressFragment()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, deliveryFragment)
            .addToBackStack(null)
            .commit()
    }

    data class Product(
        val name: String = "",
        val price: Int = 0,
        val imageUrl: String = "",
        var quantity: Int = 1
    )
}
