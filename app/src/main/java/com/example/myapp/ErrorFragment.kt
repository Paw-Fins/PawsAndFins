import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.HomeScreenFragment
import com.example.myapp.MainActivity
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ErrorFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).showBottomNavigation(false)

        val rootView = inflater.inflate(R.layout.fragment_error_page, container, false)

        // Update payment status to Failed
        updatePaymentStatusToFailed()
        clearUserCart()
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToHome()
        }, 5000)

        return rootView
    }

    private fun updatePaymentStatusToFailed() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("payment_hiwstory")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.update("status", "Failed")
                            .addOnSuccessListener {
                                Toast.makeText(
                                    requireContext(),
                                    "Payment status updated to Failed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    requireContext(),
                                    "Error updating payment status: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Error fetching payment records: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    private fun navigateToHome() {
        val cartFragment = HomeScreenFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, cartFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun clearUserCart() {
        val userId = auth.currentUser?.uid
        userId?.let {
            firestore.collection("orders").whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    querySnapshot.documents.forEach { document ->
                        document.reference.delete()
                    }
                    activity?.let { context ->
                        Toast.makeText(context, "Cart cleared successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    activity?.let { context ->
                        Toast.makeText(context, "Error clearing cart: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

}
