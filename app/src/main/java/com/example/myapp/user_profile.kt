package com.example.myapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.example.myapp.EditUserDetail
import com.google.firebase.firestore.FirebaseFirestore

class UserProfile : Fragment() {

    private lateinit var userImage: ImageView
    private lateinit var userName: TextView
    private lateinit var userEmail: TextView
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
        logOutButton = rootView.findViewById(R.id.logOut)
        editDetailBtn = rootView.findViewById(R.id.editDetailBtn)
        val mainActivity = activity as? MainActivity
        mainActivity?.findViewById<ImageView>(R.id.logoImage)?.visibility = View.GONE
        editDetailBtn.setOnClickListener {
            val editUser  = EditUserDetail()
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

    private fun fetchUserData() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userRef = firestore.collection("users").document(currentUser.uid)
            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val name = document.getString("name") ?: "No Name"
                        val email = currentUser.email ?: "No Email"
                        val imageUrl = document.getString("imageUrl")

                        // Set the data to views
                        userName.text = name
                        userEmail.text = email

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
                }
        } else {
            userName.text = "No User Logged In"
            userEmail.text = ""
        }
    }

    private fun logOutUser() {
        auth.signOut()
        val intent = Intent(requireContext(), LoginScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear back stack
        startActivity(intent)
    }
}
