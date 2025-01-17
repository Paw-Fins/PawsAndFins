import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class DeliveryAddressFragment : Fragment(), PaymentResultListener {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var inputFullName: EditText
    private lateinit var inputPhoneNumber: EditText
    private lateinit var inputStreetAddress: EditText
    private lateinit var inputCity: EditText
    private lateinit var inputState: EditText
    private lateinit var inputPostalCode: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var buttonClear: Button

    private var totalPrice: Int = 0

    data class Address(
        val fullName: String,
        val phoneNumber: String,
        val streetAddress: String,
        val city: String,
        val state: String,
        val postalCode: String
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_get_address, container, false)

        inputFullName = rootView.findViewById(R.id.input_full_name)
        inputPhoneNumber = rootView.findViewById(R.id.input_phone_number)
        inputStreetAddress = rootView.findViewById(R.id.input_street_address)
        inputCity = rootView.findViewById(R.id.input_city)
        inputState = rootView.findViewById(R.id.input_state)
        inputPostalCode = rootView.findViewById(R.id.input_postal_code)
        buttonSubmit = rootView.findViewById(R.id.button_submit)
        buttonClear = rootView.findViewById(R.id.button_clear)

        buttonSubmit.setOnClickListener {
            saveAddressToFirestore()
        }

        buttonClear.setOnClickListener {
            clearForm()
        }

        Checkout.preload(requireContext())

        return rootView
    }

    private fun saveAddressToFirestore() {
        val fullName = inputFullName.text.toString().trim()
        val phoneNumber = inputPhoneNumber.text.toString().trim()
        val streetAddress = inputStreetAddress.text.toString().trim()
        val city = inputCity.text.toString().trim()
        val state = inputState.text.toString().trim()
        val postalCode = inputPostalCode.text.toString().trim()

        if (TextUtils.isEmpty(fullName) || TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(streetAddress) ||
            TextUtils.isEmpty(city) || TextUtils.isEmpty(state) || TextUtils.isEmpty(postalCode)
        ) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        val userAddress = Address(
            fullName = fullName,
            phoneNumber = phoneNumber,
            streetAddress = streetAddress,
            city = city,
            state = state,
            postalCode = postalCode
        )

        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(userId)
            .set(mapOf("address" to userAddress))
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Address saved successfully!", Toast.LENGTH_SHORT).show()
                fetchCartAndInitiatePayment()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to save address.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchCartAndInitiatePayment() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("orders")
                .whereEqualTo("userId", user.uid)
                .get()
                .addOnSuccessListener { documents ->
                    totalPrice = 0
                    for (document in documents) {
                        val product = document.toObject(Product::class.java)
                        totalPrice += product.price * product.quantity
                    }
                    initiatePayment()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error fetching cart items: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun initiatePayment() {
        val user = auth.currentUser
        if (user != null) {
            firestore.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val email = document.getString("email") ?: "example@domain.com"
                        val contact = document.getString("mobile") ?: "9876543210"
                        startRazorpayCheckout(email, contact)
                    } else {
                        Toast.makeText(requireContext(), "User data not found in Firestore.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("UserDataError", "Error fetching user data: ${e.message}", e)
                    Toast.makeText(requireContext(), "Error fetching user details.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "User not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startRazorpayCheckout(email: String, contact: String) {
        val checkout = Checkout()
        checkout.setKeyID("rzp_test_P9PyXF3R9dZq2c")

        try {
            val options = JSONObject()
            options.put("name", "Paws and Fins")
            options.put("description", "Cart Checkout")
            options.put("currency", "INR")
            options.put("amount", totalPrice * 100)

            val prefill = JSONObject()
            prefill.put("email", email)
            prefill.put("contact", contact)
            options.put("prefill", prefill)

            checkout.open(requireActivity(), options)
        } catch (e: Exception) {
            Log.e("PaymentError", "Error during payment initiation: ${e.message}", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentSuccess(paymentId: String?) {
        (activity as? PaymentResultListener)?.onPaymentSuccess(paymentId)
    }

    override fun onPaymentError(errorCode: Int, errorDescription: String?) {
        (activity as? PaymentResultListener)?.onPaymentError(errorCode, errorDescription)
    }

    private fun navigateToFragment(fragment: Fragment) {
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun clearForm() {
        inputFullName.text.clear()
        inputPhoneNumber.text.clear()
        inputStreetAddress.text.clear()
        inputCity.text.clear()
        inputState.text.clear()
        inputPostalCode.text.clear()
    }

    data class Product(
        val name: String = "",
        val price: Int = 0,
        val imageUrl: String = "",
        var quantity: Int = 1
    )
}
