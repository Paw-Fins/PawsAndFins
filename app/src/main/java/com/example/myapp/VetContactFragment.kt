import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class VetContactFragment : Fragment() {

    private lateinit var vetContainer: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var vetProfileImage: ImageView
    private lateinit var vetName: TextView
    private lateinit var vetSpecialty: TextView
    private lateinit var vetContactInfo: TextView
    private lateinit var vetAddress: TextView
    private lateinit var vetServices: TextView
    private lateinit var bookAppointmentButton: Button
    private lateinit var callVetButton: Button

    private lateinit var reviewRecyclerView: RecyclerView
    private lateinit var addReviewText: TextView
    private lateinit var vetId: String
    private lateinit var vetContactNumber: String
    private val reviews = mutableListOf<Map<String, String>>()
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_vet_contact, container, false)

        // Initialize Firebase
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Initialize views
        vetContainer = view.findViewById(R.id.vetContainer)
        detailsContainer = view.findViewById(R.id.detailsContainer)
        vetProfileImage = view.findViewById(R.id.vetProfileImage)
        vetName = view.findViewById(R.id.vetName)
        vetSpecialty = view.findViewById(R.id.vetSpecialty)
        vetContactInfo = view.findViewById(R.id.vetContactInfo)
        vetAddress = view.findViewById(R.id.vetAddress)
        vetServices = view.findViewById(R.id.vetServices)
        bookAppointmentButton = view.findViewById(R.id.bookAppointmentButton)
        callVetButton = view.findViewById(R.id.callVetButton)
        reviewRecyclerView = view.findViewById(R.id.reviewRecyclerView)
        addReviewText = view.findViewById(R.id.addReviewText)
        vetId = arguments?.getString("vetId") ?: ""

        // RecyclerView setup
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        reviewRecyclerView.setHasFixedSize(true)
        val adapter = ReviewAdapter(reviews)
        reviewRecyclerView.adapter = adapter

        // Load reviews from Firestore
        loadReviewsFromFirestore(adapter)

        // Handle adding reviews (triggering the review dialog)
        addReviewText.setOnClickListener {
            showAddReviewDialog(adapter)
        }

        // Get the arguments passed from VetFragment
        arguments?.let {
            val doctorName = it.getString("doctorName") ?: ""
            val doctorSpecialty = it.getString("doctorSpecialty") ?: ""
            vetContactNumber = it.getString("doctorContact") ?: ""
            val doctorAddress = it.getString("doctorAddress") ?: ""
            val doctorServices = it.getString("doctorServices") ?: ""
            val isEmergencyAvailable = it.getBoolean("isEmergencyAvailable", false)

            if (vetId.isNotEmpty()) {
                // Populate the views with the passed data
                vetName.text = "Name: $doctorName"
                vetSpecialty.text = "Specialty: $doctorSpecialty"
                vetContactInfo.text = "Contact: $vetContactNumber"
                vetAddress.text = doctorAddress
                vetServices.text = doctorServices

                callVetButton.visibility = if (isEmergencyAvailable) View.VISIBLE else View.GONE
                detailsContainer.visibility = View.VISIBLE

                // Load the vet's profile image
                loadVetProfileImage()

                bookAppointmentButton.setOnClickListener {
                    navigateToVetAppointmentFragment(vetId)
                }

                callVetButton.setOnClickListener {
                    makePhoneCall(vetContactNumber)
                }
            } else {
                // Handle the case where vetId is empty or not passed properly
                Toast.makeText(requireContext(), "Vet ID is missing", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun loadReviewsFromFirestore(adapter: ReviewAdapter) {
        firestore.collection("reviews")
            .whereEqualTo("vetId", vetId)
            .get()
            .addOnSuccessListener { documents ->
                reviews.clear()
                for (document in documents) {
                    val reviewerName = document.getString("reviewerName") ?: ""
                    val reviewText = document.getString("reviewText") ?: ""
                    val reviewDate = document.getString("reviewDate") ?: ""

                    // Add review data directly to the list (no Review object)
                    reviews.add(hashMapOf(
                        "reviewerName" to reviewerName,
                        "reviewText" to reviewText,
                        "reviewDate" to reviewDate
                    ))
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { e ->
                // Handle error
                Log.e("VetContactFragment", "Error fetching reviews", e)
            }
    }

    private fun navigateToVetAppointmentFragment(vetId: String) {
        val fragment = VetAppointmentFragment()

        val bundle = Bundle()
        bundle.putString("vetId", vetId)
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    private fun makePhoneCall(phoneNumber: String) {
        if (phoneNumber.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
            startActivity(intent)
        } else {
            // Handle the case where there is no phone number available
            Toast.makeText(requireContext(), "No contact number available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddReviewDialog(adapter: ReviewAdapter) {
        // Inflate the custom dialog layout
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_review, null)

        val reviewNameEditText = dialogView.findViewById<EditText>(R.id.reviewName)
        val reviewTextEditText = dialogView.findViewById<EditText>(R.id.reviewText)
        val submitButton = dialogView.findViewById<Button>(R.id.submitReviewButton)

        // Create an AlertDialog
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle("Add Review")
            .setView(dialogView)
            .setCancelable(true)

        // Handle submit button click
        submitButton.setOnClickListener {
            val name = reviewNameEditText.text.toString().trim()
            val reviewText = reviewTextEditText.text.toString().trim()

            if (name.isNotEmpty() && reviewText.isNotEmpty()) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val reviewDate = dateFormat.format(Date())

                // Create a map for the new review
                val reviewData = hashMapOf(
                    "vetId" to vetId,
                    "userId" to auth.currentUser?.uid,
                    "reviewerName" to name,
                    "reviewText" to reviewText,
                    "reviewDate" to reviewDate
                )

                // Upload the review to Firestore
                firestore.collection("reviews")
                    .add(reviewData)
                    .addOnSuccessListener {
                        // On success, add the review to the list and update the adapter
                        reviews.add(hashMapOf(
                            "reviewerName" to name,
                            "reviewText" to reviewText,
                            "reviewDate" to reviewDate
                        ))
                        adapter.notifyItemInserted(reviews.size - 1)
                        reviewRecyclerView.smoothScrollToPosition(reviews.size - 1)
                        dialogBuilder.create().dismiss() // Dismiss dialog on success

                        // Show success message
                        Toast.makeText(requireContext(), "Review submitted successfully", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        // Handle failure and show error
                        Toast.makeText(requireContext(), "Failed to submit review: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Show error if fields are empty
                reviewNameEditText.error = if (name.isEmpty()) "Name is required" else null
                reviewTextEditText.error = if (reviewText.isEmpty()) "Review is required" else null
            }
        }

        // Show the dialog
        dialogBuilder.create().show()
    }

    private fun loadVetProfileImage() {
        // Fetch the vet's user document from Firestore using vetId (assuming users collection exists)
        firestore.collection("users")
            .document(vetId)  // Using vetId as the document ID
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val profileImageUrl = document.getString("imageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        Glide.with(requireContext())
                            .load(profileImageUrl)
                            .centerCrop()
                            .into(vetProfileImage)
                    } else {
                        vetProfileImage.setImageResource(R.drawable.ic_launcher_foreground)  // Default image if none found
                    }
                } else {
                    Toast.makeText(requireContext(), "Vet profile not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("VetContactFragment", "Error fetching vet profile image", e)
                Toast.makeText(requireContext(), "Failed to load profile image", Toast.LENGTH_SHORT).show()
            }
    }

    // ReviewAdapter class for RecyclerView
    class ReviewAdapter(private val reviewList: List<Map<String, String>>) :
        RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.vet_review, parent, false)
            return ReviewViewHolder(view)
        }

        override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
            val review = reviewList[position]
            holder.reviewerName.text = review["reviewerName"]
            holder.reviewText.text = review["reviewText"]
            holder.reviewDate.text = review["reviewDate"]
        }

        override fun getItemCount(): Int = reviewList.size

        class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val reviewerName: TextView = itemView.findViewById(R.id.reviewerName)
            val reviewText: TextView = itemView.findViewById(R.id.reviewText)
            val reviewDate: TextView = itemView.findViewById(R.id.reviewDate)
        }
    }
}
