package com.example.myapp


import EditUserDetailFragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.fragment.app.Fragment

class UserProfile : Fragment() {

    private lateinit var userImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
    private lateinit var userMobile: TextView  // New TextView for mobile number
    private lateinit var logOutButton: MaterialButton
    private lateinit var editDetailBtn: MaterialButton
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_user_profile, container, false)
        userImage = rootView.findViewById(R.id.user_image)
        userName = rootView.findViewById(R.id.user_name)
        userEmail = rootView.findViewById(R.id.user_email)
        userMobile = rootView.findViewById(R.id.user_number)  // Initialize the new TextView
        logOutButton = rootView.findViewById(R.id.logOut)
        editDetailBtn = rootView.findViewById(R.id.editDetailBtn)

        val mainActivity = activity as? MainActivity
        mainActivity?.findViewById<ImageView>(R.id.logoImage)?.visibility = View.GONE

        editDetailBtn.setOnClickListener {
            val editUser = EditUserDetailFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, editUser)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Fetch user data
        fetchUserData()

        // Handle Log Out button click
        logOutButton.setOnClickListener {
            logOutUser()
        }

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val mainActivity = activity as? MainActivity
        mainActivity?.findViewById<ImageView>(R.id.logoImage)?.visibility = View.VISIBLE
    }

    private fun fetchUserData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userRef = firestore.collection("users").document(currentUser.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "No Name"
                        val mobile = document.getString("mobile") ?: "No Mobile Number"
                        val imageUrl = document.getString("imageUrl")

                        // Set the data to views
                        userName.text = name
                        userEmail.text = currentUser.email ?: "No Email"
                        userMobile.text = mobile  // Display mobile number

                        if (imageUrl != null) {
                            Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(userImage)
                        } else {
                            userImage.setImageResource(R.drawable.profile_circle)
                        }
                    }
                }
                .addOnFailureListener {
                    userName.text = "Error loading user data"
                    userEmail.text = ""
                    userMobile.text = ""
                }
        } else {
            userName.text = "No User Logged In"
            userEmail.text = ""
            userMobile.text = ""
        }
    }

    private fun logOutUser() {
        // Sign out from Firebase
        auth.signOut()

        // Call the method to navigate to the HomeScreenFragment
        navigateToLogin()
    }

    private fun navigateToLogin() {
        // Navigate to the HomeScreenFragment
        val LoginFragment = LoginScreen()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoginFragment)
            .addToBackStack(null)
            .commit()
    }
}
