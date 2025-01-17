import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myapp.R

class ErrorFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_error_page, container, false)

        // Redirect to Cart after 10 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToCart()
        }, 10000)

        return rootView
    }

    private fun navigateToCart() {
        val cartFragment = CartFragment()
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
}
