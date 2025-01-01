package com.example.myapp

import CartFragment
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myapp.HomeScreenFragment
import com.example.myapp.LoginScreen
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var layout: LinearLayout
    private lateinit var profileCircle: ImageView
    private lateinit var logoText: TextView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        layout = findViewById(R.id.topLayout)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        profileCircle = findViewById(R.id.logoImage)
        logoText = findViewById(R.id.logo2)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Update theme-related colors
        updateUIForTheme()

        profileCircle.setOnClickListener {
            loadFragment(UserProfile())
        }

        fetchProfileImage { imageUrl ->
            if (imageUrl != null) {
                Glide.with(this)
                    .load(imageUrl)
                    .circleCrop()
                    .into(profileCircle)
            } else {
                profileCircle.setImageResource(R.drawable.profile_circle)
            }
        }

        if (savedInstanceState == null) {
            loadFragment(LoginScreen())
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeScreenFragment())
                    true
                }
                R.id.navigation_cart -> {
                    loadFragment(CartFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment != null) {
            transaction.hide(currentFragment)
        }
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun showBottomNavigation(show: Boolean) {
        if (show) {
            bottomNavigationView.visibility = View.VISIBLE
            layout.visibility = View.VISIBLE
        } else {
            bottomNavigationView.visibility = View.GONE
            layout.visibility = View.GONE
        }
    }

    private fun fetchProfileImage(callback: (String?) -> Unit) {
        val currentUser  = auth.currentUser
        if (currentUser != null) {
            val userRef = firestore.collection("users").document(currentUser.uid)
            userRef.get().addOnSuccessListener { document ->
                val imageUrl = document.getString("imageUrl")
                callback(imageUrl)
            }.addOnFailureListener {
                callback(null)
            }
        } else {
            callback(null)
        }
    }

    private fun updateUIForTheme() {
        val isDarkMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        val textColor = if (isDarkMode) {
            resources.getColor(R.color.onBackground, theme)
        } else {
            resources.getColor(R.color.onBackground, theme)
        }
        logoText.setTextColor(textColor)

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIForTheme()
    }
}