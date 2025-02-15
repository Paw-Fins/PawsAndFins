import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.NgoAppointmentFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.myapp.R
import java.text.SimpleDateFormat
import java.util.*

class NgoContactFragment : Fragment() {

    private lateinit var ngoContainer: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var ngoProfileImage: ImageView
    private lateinit var ngoName: TextView
    private lateinit var ngoContactInfo: TextView
    private lateinit var ngoAddress: TextView
    private lateinit var ngoServices: TextView
    private lateinit var donateButton: Button
    private lateinit var callNgoButton: Button

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var addReviewText: TextView
    private lateinit var ngoId: String
    private lateinit var ngoContactNumber: String
    private val reviews = mutableListOf<Map<String, String>>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ngo_contact, container, false)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        ngoContainer = view.findViewById(R.id.ngoContainer)
        detailsContainer = view.findViewById(R.id.detailsContainer)
        ngoProfileImage = view.findViewById(R.id.ngoProfileImage)
        ngoName = view.findViewById(R.id.ngoName)
        ngoContactInfo = view.findViewById(R.id.ngoContactInfo)
        ngoAddress = view.findViewById(R.id.ngoAddress)
        ngoServices = view.findViewById(R.id.ngoServices)
        donateButton = view.findViewById(R.id.donateButton)
        callNgoButton = view.findViewById(R.id.callNgoButton)
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        addReviewText = view.findViewById(R.id.addReviewText)

        ngoId = arguments?.getString("ngoId") ?: ""

        // RecyclerView setup
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.setHasFixedSize(true)
        val adapter = VetContactFragment.ReviewAdapter(reviews)
        reviewRecyclerView.adapter = adapter

        // Load reviews from Firestore
        loadReviewsFromFirestore(adapter)

        // Handle adding reviews
        addReviewText.setOnClickListener {
            showAddReviewDialog(adapter)
        }

        // Fetch NGO details from Firestore
        loadNgoDetails()

        return view
    }

    private fun loadNgoDetails() {
        if (ngoId.isEmpty()) {
            Toast.makeText(requireContext(), "NGO ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("ngos")
            .document(ngoId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val name = document.getString("name") ?: "N/A"
                    ngoContactNumber = document.getString("contact") ?: "N/A"
                    val address = document.getString("address") ?: "N/A"
                    val services = document.getString("services") ?: "N/A"
                    val profileImageUrl = document.getString("imageUrl")

                    // Set text values
                    ngoName.text = name
                    ngoContactInfo.text = "Contact: $ngoContactNumber"
                    ngoAddress.text = address
                    ngoServices.text = "Services: $services"

                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(profileImageUrl).centerCrop().into(ngoProfileImage)
                    } else {
                        ngoProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                    }

                    // Show details container
                    detailsContainer.visibility = View.VISIBLE

                    // Set up button actions
                    donateButton.setOnClickListener {
                        navigateToNgoVisitRequest(ngoId)
                    }
                    callNgoButton.setOnClickListener {
                        makePhoneCall(ngoContactNumber)
                    }

                } else {
                    Toast.makeText(requireContext(), "NGO details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("NgoContactFragment", "Error fetching NGO details", e)
                Toast.makeText(requireContext(), "Failed to load NGO details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadReviewsFromFirestore(adapter: VetContactFragment.ReviewAdapter) {
        firestore.collection("reviews")
            .whereEqualTo("ngoId", ngoId)
            .get()
            .addOnSuccessListener { documents ->
                reviews.clear()
                for (document in documents) {
                    val reviewerName = document.getString("reviewerName") ?: "Anonymous"
                    val reviewText = document.getString("reviewText") ?: ""
                    val reviewDate = document.getString("reviewDate") ?: ""

                    reviews.add(hashMapOf(
                        "reviewerName" to reviewerName,
                        "reviewText" to reviewText,
                        "reviewDate" to reviewDate
                    ))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                Log.e("NgoContactFragment", "Error fetching reviews", e)
            }
    }

    private fun navigateToNgoVisitRequest(ngoId: String) {
        val fragment = NgoAppointmentFragment()

        val bundle = Bundle()
        bundle.putString("ngoId", ngoId)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber != "N/A") {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } else {
            Toast.makeText(requireContext(), "No contact number available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddReviewDialog(adapter: VetContactFragment.ReviewAdapter) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)

        val reviewNameEditText = dialogView.findViewById<EditText>(R.id.reviewName)
        val reviewTextEditText = dialogView.findViewById<EditText>(R.id.reviewText)
        val submitButton = dialogView.findViewById<Button>(R.id.submitReviewButton)

        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Add Review")
            .setView(dialogView)
            .setCancelable(true)

        val dialog = dialogBuilder.create()

        submitButton.setOnClickListener {
            val name = reviewNameEditText.text.toString().trim()
            val reviewText = reviewTextEditText.text.toString().trim()

            if (name.isNotEmpty() && reviewText.isNotEmpty()) {
                val reviewData = hashMapOf(
                    "ngoId" to ngoId,
                    "reviewerName" to name,
                    "reviewText" to reviewText,
                    "reviewDate" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                )

                firestore.collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener {
                        reviews.add(reviewData)
                        adapter.notifyItemInserted(reviews.size - 1)
                        dialog.dismiss()
                        Toast.makeText(requireContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialog.show()
    }
}
