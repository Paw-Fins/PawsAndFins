import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapp.AdminScreenFragment
import com.example.myapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class UserManagement : Fragment() {

    private lateinit var userDataContainer: LinearLayout
    private lateinit var totalUsersTextView: TextView
    private lateinit var bannedUsersTextView: TextView
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val users = mutableListOf<User>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_management, container, false)
        userDataContainer = view.findViewById(R.id.UserDataContainer)
        totalUsersTextView = view.findViewById(R.id.totalUsersCount)
        bannedUsersTextView = view.findViewById(R.id.bannedUsersCount)

        // Initialize Product Management button
        val productManagementButton: Button = view.findViewById(R.id.ProductManage)
        productManagementButton.setOnClickListener {
            // Redirect to AdminScreenFragment when Product Management button is clicked
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container, AdminScreenFragment()) // replace with your container ID
            fragmentTransaction.addToBackStack(null) // optional: add fragment to back stack if you want to allow back navigation
            fragmentTransaction.commit()
        }

        // Fetch users and update counts
        fetchUsers()

        return view
    }

    private fun fetchUsers() {
        firestore.collection("users").get()
            .addOnSuccessListener { result ->
                users.clear()
                var bannedCount = 0

                for (document in result) {
                    val name = document.getString("name") ?: "Unknown"
                    val email = document.getString("email") ?: "Unknown"
                    val isBanned = document.getBoolean("isBanned") ?: false
                    val profileImageUrl = document.getString("imageUrl")
                    users.add(User(name, email, profileImageUrl, isBanned))

                    if (isBanned) bannedCount++
                }

                // Update UI with the fetched data
                populateUserData(users)
                updateUserCounts(bannedCount, users.size)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to fetch users: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserManagement", "Error fetching users", exception)
            }
    }

    private fun populateUserData(users: List<User>) {
        userDataContainer.removeAllViews()
        for (user in users) {
            val userView = LayoutInflater.from(requireContext()).inflate(R.layout.admin_user, userDataContainer, false)

            val userNameTextView: TextView = userView.findViewById(R.id.user_name)
            val userEmailTextView: TextView = userView.findViewById(R.id.user_email)
            val userImageView: ImageView = userView.findViewById(R.id.user_image)
            val banButton: Button = userView.findViewById(R.id.ban_button)
            val unbanButton: Button = userView.findViewById(R.id.unban_button)

            userNameTextView.text = user.name
            userEmailTextView.text = user.email

            Glide.with(this)
                .load(user.profileImageUrl ?: R.drawable.profile_circle) // Default image
                .circleCrop()
                .into(userImageView)

            if (user.isBanned) {
                banButton.visibility = View.GONE
                unbanButton.visibility = View.VISIBLE
            } else {
                banButton.visibility = View.VISIBLE
                unbanButton.visibility = View.GONE
            }

            // Ban button click listener
            banButton.setOnClickListener { banUser(user) }

            // Unban button click listener
            unbanButton.setOnClickListener { unbanUser(user) }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(0, 16, 0, 0)
            userView.layoutParams = params

            userDataContainer.addView(userView)
        }
    }

    private fun updateUserCounts(bannedCount: Int, totalCount: Int) {
        totalUsersTextView.text = totalCount.toString()
        bannedUsersTextView.text = bannedCount.toString()
    }

    private fun banUser(user: User) {
        firestore.collection("users")
            .whereEqualTo("email", user.email)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = result.documents.first()
                firestore.collection("users").document(document.id)
                    .update("isBanned", true)
                    .addOnSuccessListener {
                        Toast.makeText(context, "User banned successfully", Toast.LENGTH_SHORT).show()
                        Log.d("UserManagement", "User banned: ${user.email}")
                        user.isBanned = true
                        populateUserData(users)
                        updateUserCounts(users.count { it.isBanned }, users.size)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Failed to ban user: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("UserManagement", "Error banning user", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching user: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserManagement", "Error fetching user for ban", exception)
            }
    }

    private fun unbanUser(user: User) {
        firestore.collection("users")
            .whereEqualTo("email", user.email)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    Toast.makeText(context, "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val document = result.documents.first()
                firestore.collection("users").document(document.id)
                    .update("isBanned", false)
                    .addOnSuccessListener {
                        Toast.makeText(context, "User unbanned successfully", Toast.LENGTH_SHORT).show()
                        Log.d("UserManagement", "User unbanned: ${user.email}")
                        user.isBanned = false
                        populateUserData(users)
                        updateUserCounts(users.count { it.isBanned }, users.size)
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Failed to unban user: ${exception.message}", Toast.LENGTH_SHORT).show()
                        Log.e("UserManagement", "Error unbanning user", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching user: ${exception.message}", Toast.LENGTH_SHORT).show()
                Log.e("UserManagement", "Error fetching user for unban", exception)
            }
    }

    // Data class to represent a user
    data class User(
        val name: String,
        val email: String,
        val profileImageUrl: String?,  // URL to the profile image
        var isBanned: Boolean
    )
}
