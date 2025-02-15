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
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class GroomerContactFragment : Fragment() {

    private lateinit var groomerContainer: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var groomerProfileImage: ImageView
    private lateinit var groomerName: TextView
    private lateinit var groomerSpecialty: TextView
    private lateinit var groomerContactInfo: TextView
    private lateinit var groomerAddress: TextView
    private lateinit var groomerServices: TextView
    private lateinit var bookGroomingButton: Button
    private lateinit var callGroomerButton: Button

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var addReviewText: TextView
    private lateinit var groomerId: String
    private lateinit var groomerContactNumber: String
    private val reviews = mutableListOf<Map<String, String>>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_groomer_contact, container, false)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        groomerContainer = view.findViewById(R.id.groomerContainer)
        detailsContainer = view.findViewById(R.id.detailsContainer)
        groomerProfileImage = view.findViewById(R.id.groomerProfileImage)
        groomerName = view.findViewById(R.id.groomerName)
        groomerSpecialty = view.findViewById(R.id.groomerSpecialty)
        groomerContactInfo = view.findViewById(R.id.groomerContactInfo)
        groomerAddress = view.findViewById(R.id.groomerAddress)
        groomerServices = view.findViewById(R.id.groomerServices)
        bookGroomingButton = view.findViewById(R.id.bookGroomingButton)
        callGroomerButton = view.findViewById(R.id.callGroomerButton)
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        addReviewText = view.findViewById(R.id.addReviewText)

        groomerId = arguments?.getString("groomerId") ?: ""

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

        // Fetch groomer details from Firestore
        loadGroomerDetails()

        return view
    }

    private fun loadGroomerDetails() {
        if (groomerId.isEmpty()) {
            Toast.makeText(requireContext(), "Groomer ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users")
            .document(groomerId)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val fullName = document.getString("name") ?: "N/A"
                    val specialty = document.getString("specialty") ?: "N/A"
                    groomerContactNumber = document.getString("mobile") ?: "N/A"
                    val address = document.getString("location") ?: "N/A"
                    val services = document.getString("services") ?: "N/A"
                    val profileImageUrl = document.getString("imageUrl")

                    // Set text values
                    groomerName.text = "Name: $fullName"
                    groomerSpecialty.text = "Specialty: $specialty"
                    groomerContactInfo.text = "Contact: $groomerContactNumber"
                    groomerAddress.text = address
                    groomerServices.text = services

                    // Load profile image
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext()).load(profileImageUrl).centerCrop().into(groomerProfileImage)
                    } else {
                        groomerProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                    }

                    // Show details container
                    detailsContainer.visibility = View.VISIBLE

                    // Set up button actions
                    bookGroomingButton.setOnClickListener {
                        navigateToGroomingAppointmentFragment(groomerId)
                    }
                    callGroomerButton.setOnClickListener {
                        makePhoneCall(groomerContactNumber)
                    }

                } else {
                    Toast.makeText(requireContext(), "Groomer details not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("GroomerContactFragment", "Error fetching groomer details", e)
                Toast.makeText(requireContext(), "Failed to load groomer details", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadReviewsFromFirestore(adapter: VetContactFragment.ReviewAdapter) {
        firestore.collection("reviews")
            .whereEqualTo("groomerId", groomerId)
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
                Log.e("GroomerContactFragment", "Error fetching reviews", e)
            }
    }

    private fun navigateToGroomingAppointmentFragment(groomerId: String) {
        val fragment = GroomerAppointmentFragment()

        val bundle = Bundle()
        bundle.putString("groomerId", groomerId)
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

        submitButton.setOnClickListener {
            val name = reviewNameEditText.text.toString().trim()
            val reviewText = reviewTextEditText.text.toString().trim()

            if (name.isNotEmpty() && reviewText.isNotEmpty()) {
                val reviewData = hashMapOf(
                    "groomerId" to groomerId,
                    "reviewerName" to name,
                    "reviewText" to reviewText,
                    "reviewDate" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
                )

                firestore.collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener {
                        reviews.add(reviewData)
                        adapter.notifyItemInserted(reviews.size - 1)
                        dialogBuilder.create().dismiss()
                        Toast.makeText(requireContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        dialogBuilder.create().show()
    }
}
