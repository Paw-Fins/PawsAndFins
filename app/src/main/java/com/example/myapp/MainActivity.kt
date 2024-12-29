package com.example.myapp



import CartFragment
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        if (savedInstanceState == null) {
            loadFragment(LoginScreen())
        }

        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    loadFragment(HomeScreenFragment())
                    true
                }
//                R.id.navigation_search -> {
//                    loadFragment(SearchFragment())
//                    true
//                }
                R.id.navigation_cart -> {
                    loadFragment(CartFragment())
                    true
                }
//                R.id.navigation_slider -> {
//                    loadFragment(SliderFragment())
//                    true
//                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment)
        transaction.commit()
    }
    fun showBottomNavigation(show: Boolean) {
        if (show) {
            bottomNavigationView.visibility = View.VISIBLE
        } else {
            bottomNavigationView.visibility = View.GONE
        }
    }
}

