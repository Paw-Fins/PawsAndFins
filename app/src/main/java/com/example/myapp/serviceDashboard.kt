package com.example.myapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ServiceDashboard : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_service_dashboard, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        fetchCurrentUser()

        return view
    }

    private fun fetchCurrentUser() {
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val userEmail = currentUser.email
            Log.d("ServiceDashboard", "User Email: $userEmail")
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userName = document.getString("name") ?: "Unknown User"
                        Log.d("ServiceDashboard", "User Name: $userName")
                    } else {
                        Log.w("ServiceDashboard", "User document does not exist.")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("ServiceDashboard", "Error fetching user details: ", exception)
                }
        } else {
            Log.w("ServiceDashboard", "No user is logged in")
        }
    }
}
