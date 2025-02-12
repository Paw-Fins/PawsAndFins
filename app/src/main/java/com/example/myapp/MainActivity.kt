package com.example.myapp

import AppointmentHistoryFragment
import CartFragment
import ErrorFragment
import SuccessFragment
import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.razorpay.PaymentResultListener

class MainActivity : AppCompatActivity(), PaymentResultListener {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
    }
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var layout: ConstraintLayout
    private lateinit var profileCircle: ImageView
    private lateinit var logo: ImageView
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var dashboardFrag :Fragment

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        checkAndRequestPermissions()

        layout = findViewById(R.id.topLayout)
        bottomNavigationView = findViewById(R.id.bottom_navigation)
        profileCircle = findViewById(R.id.logoImage)
        logo = findViewById(R.id.logoImage)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            // Fetch user role from Firestore
            FirebaseFirestore.getInstance().collection("users")
                .document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val userRole = document.getString("role")?.toLowerCase()

                        // Handle redirection based on role
                        when (userRole?.toLowerCase()) {
                            "admin" -> navigateToAdminScreen()
                            "doctor" -> navigateToDoctorDashboard()
                            "groomer" -> navigateToGroomerDashboard()
                            "trainer" -> navigateToTrainerDashboard()
                            "ngo manager" -> navigateToNgo()
                            else -> navigateToHomeScreen()
                        }

                        // Set up bottom navigation after role is fetched
                        if (userRole != null) {
                            setupNavigationForRole(userRole)
                        }
                    } else {
                        Toast.makeText(this, "User role not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        } else {
            // If user is not logged in, show login screen
            navigateToLoginScreen()
        }


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
    }

    private fun setupNavigationForRole(role: String) {
        // Clear existing menus
        bottomNavigationView.menu.clear()
        navigationView.menu.clear()

        // Role-specific bottom navigation and drawer menus
        if (role.lowercase() != "user") {
            bottomNavigationView.menu.clear()
            navigationView.menu.clear()
            bottomNavigationView.inflateMenu(R.menu.service_provider_navigation)
            navigationView.inflateMenu(R.menu.service_provider_drawer_menu)

            when (role.lowercase()) {
                "doctor" -> dashboardFrag = ServiceDashboard()
                "groomer" -> dashboardFrag = GroomerDashboard()
                "trainer" -> dashboardFrag = TrainerDashboard()
                "ngo manager" -> dashboardFrag = NGODashboard()
            }

            bottomNavigationView.setOnNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.navigation_dashboard -> {
                        loadFragment(dashboardFrag)
                        true
                    }
                    R.id.navigation_requests -> {
                        loadFragment(AppoinmentRequestFragment())
                        true
                    }
                    R.id.navigation_payment -> {
                        loadFragment(PaymentHistoryFragment())
                        true
                    }
                    R.id.navigation_slider -> {
                        openDrawer()
                        true
                    }
                    else -> false
                }
            }

            navigationView.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_organization_profile -> {
                        loadFragment(OrganizationRegistrationFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.nav_organization_review -> {
                        loadFragment(VetReviewFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    else -> false
                }
            }
        } else {
            bottomNavigationView.menu.clear()
            navigationView.menu.clear()
            bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu)
            navigationView.inflateMenu(R.menu.drawer_menu)

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
                    R.id.navigation_product -> {
                        loadFragment(ProductFragment())
                        true
                    }
                    R.id.navigation_slider -> {
                        openDrawer()
                        true
                    }
                    else -> false
                }
            }

            navigationView.setNavigationItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_profile -> {
                        loadFragment(PetRegistrationFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.nav_ngo -> {
                        loadFragment(NGOFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.nav_vet -> {
                        loadFragment(VetFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.nav_groomer -> {
                        loadFragment(GroomerFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.appointhistory -> {
                        loadFragment(AppointmentHistoryFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.nav_trainer -> {
                        loadFragment(TrainerFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.payhistory -> {
                        loadFragment(PaymentHistoryUserFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.guide -> {
                        loadFragment(GuideFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.donation -> {
                        loadFragment(DonationFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
                    R.id.adoption -> {
                        loadFragment(AdoptionFragment())
                        drawerLayout.closeDrawer(Gravity.RIGHT)
                        true
                    }
//                    DonationFragment
                    else -> false
                }
            }
        }
    }


    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    private fun openDrawer() {
        drawerLayout.openDrawer(Gravity.RIGHT)
    }

    private fun fetchProfileImage(callback: (String?) -> Unit) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    val imageUrl = document.getString("imageUrl")
                    callback(imageUrl)
                }
                .addOnFailureListener {
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
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateUIForTheme()
    }

    fun showBottomNavigation(show: Boolean) {
        if (show) {
            layout.visibility = View.VISIBLE
            bottomNavigationView.visibility = View.VISIBLE
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        } else {
            layout.visibility = View.GONE
            bottomNavigationView.visibility = View.GONE
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.RIGHT)) {
            drawerLayout.closeDrawer(Gravity.RIGHT)
        } else {
            super.onBackPressed()
        }
    }
    override fun onPaymentSuccess(paymentId: String?) {
        val currentUser = auth.currentUser
        if (currentUser != null && paymentId != null) {
            val userId = currentUser.uid

            // Get current time in hours
            val currentTimeInHours = System.currentTimeMillis() / (1000 * 60 * 60)

            val updatedData = mapOf(
                "paymentId" to paymentId,
                "timestampHours" to currentTimeInHours // Time in hours
            )

            // Update the existing payment record
            firestore.collection("payment_history")
                .whereEqualTo("userId", userId)
                .whereEqualTo("status", "PENDING")
                .get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        for (document in querySnapshot.documents) {
                            document.reference.update(updatedData)
                                .addOnSuccessListener {
                                    // Navigate to the SuccessFragment on successful database update
                                    val successFragment = SuccessFragment()
                                    val bundle = Bundle().apply {
                                        putString("paymentId", paymentId)
                                    }
                                    successFragment.arguments = bundle
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_container, successFragment)
                                        .commit()
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        this,
                                        "Payment successful, but failed to update database: ${exception.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                        }
                    } else {
                        Toast.makeText(this, "No pending payment record found to update.", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to retrieve payment record: ${exception.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            Toast.makeText(this, "Payment successful, but user not authenticated.", Toast.LENGTH_SHORT).show()
        }
    }

    fun resetUI() {
        // Clear back stack before replacing the fragment
        clearBackStack()

        // Load the LoginFragment
        loadFragment(LoginScreen())

        // Clear the menus
        bottomNavigationView.menu.clear()
        navigationView.menu.clear()

        // Reset the navigation based on user role
        setupNavigationForRole("user")
    }


    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        for (i in 0 until fragmentManager.backStackEntryCount) {
            fragmentManager.popBackStack()
        }
    }


    override fun onPaymentError(errorCode: Int, errorDescription: String?) {
        Toast.makeText(this, "Payment failed: $errorDescription", Toast.LENGTH_SHORT).show()
        val errorFragment = ErrorFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, errorFragment)
            .commit()
    }

    private fun navigateToHomeScreen() {
        val homeFragment = HomeScreenFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()
    }

    private fun navigateToAdminScreen() {
        val adminFragment = AdminScreenFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, adminFragment)
            .commit()
    }
    private fun navigateToNgo(){
        val ngoFrag = NGODashboard()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, ngoFrag)
            .commit()
    }

    private fun navigateToDoctorDashboard() {
        val doctorDashboardFragment = ServiceDashboard()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, doctorDashboardFragment)
            .commit()
    }

    private fun navigateToGroomerDashboard() {
        val groomerDashboardFragment = GroomerDashboard()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, groomerDashboardFragment)
            .commit()
    }

    private fun navigateToTrainerDashboard() {
        val trainerDashboardFragment = TrainerDashboard()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, trainerDashboardFragment)
            .commit()
    }

    private fun navigateToLoginScreen() {
        val trainerDashboardFragment = LoginScreen()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, trainerDashboardFragment)
            .commit()
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // List of permissions to check
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.READ_MEDIA_IMAGES
        )

        // Add permissions that are not granted to the list
        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        // Request permissions if any are needed
        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsNeeded.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            val deniedPermissions = mutableListOf<String>()

            // Check which permissions were denied
            for ((index, result) in grantResults.withIndex()) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    deniedPermissions.add(permissions[index])
                }
            }
        }
    }

}
