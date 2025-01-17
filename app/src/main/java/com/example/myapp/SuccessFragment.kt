import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.myapp.HomeScreenFragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SuccessFragment : Fragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_payment_success, container, false)

        val buttonBackToHome: Button = rootView.findViewById(R.id.button_back_to_home)

        // Clear user's cart
        clearUserCart()

        // Navigate to Home on button click
        buttonBackToHome.setOnClickListener {
            navigateToHome()
        }

        return rootView
    }

    private fun clearUserCart() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("orders").document(userId)
                .delete()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Cart cleared successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Error clearing cart: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun navigateToHome() {
        val homeScreenFragment = HomeScreenFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        val currentFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, homeScreenFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}
