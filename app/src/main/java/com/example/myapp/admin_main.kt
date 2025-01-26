package com.example.myapp

import AdminAppointmentHistoryFragment
import UserManagement
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.button.MaterialButton

class AdminScreenFragment : Fragment() {
    data class Organization(
        val name: String,
        val address: String,
        val services: Array<String>
    )



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_admin_main, container, false)
        val productContainer: LinearLayout = view.findViewById(R.id.adminProductContainer)
        val appointment : Button = view.findViewById(R.id.btnAppointmentHistory)
        val ngoViewButton :Button = view.findViewById(R.id.btnViewAllNgo)
        val vetViewButton :Button = view.findViewById(R.id.btnViewAllVet)
        val groomerViewButton :Button = view.findViewById(R.id.btnViewAllGroomer)
        val payment : Button = view.findViewById(R.id.btnPayments)
        val ngoContainer: LinearLayout = view.findViewById(R.id.adminNgoRegisterContainer)
        val vetContainer: LinearLayout = view.findViewById(R.id.adminVetRegisterContainer)
        val groomerContainer: LinearLayout = view.findViewById(R.id.adminGroomerRegisterContainer)

        ngoViewButton.setOnClickListener{
            val ngoView =AdminNgoRegister()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ngoView)
                .addToBackStack(null)
                .commit()
        }
        vetViewButton.setOnClickListener{
            val vetView =AdminVetRegister()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, vetView)
                .addToBackStack(null)
                .commit()
        }
        groomerViewButton.setOnClickListener{
            val groomerView =AdminGroomerRegister()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, groomerView)
                .addToBackStack(null)
                .commit()
        }


        payment.setOnClickListener{
            val adminpay =PaymentHistoryAdminFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, adminpay)
                .addToBackStack(null)
                .commit()
        }
        loadStaticNGOs(ngoContainer)
        loadStaticVets(vetContainer)
        loadStaticGroomers(groomerContainer)

        appointment.setOnClickListener{
            val adminAppointment =AdminAppointmentHistoryFragment()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, adminAppointment)
                .addToBackStack(null)
                .commit()
        }
        

        // Fetch and display products dynamically
        fetchProducts(productContainer)

        // Handle Add Product button click
        val btnAddProduct: MaterialButton = view.findViewById(R.id.btnAddProduct)
        btnAddProduct.setOnClickListener {
            val addProductFragment = AddProductFragment() // Replace with your fragment class
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addProductFragment)
                .addToBackStack(null)
                .commit()
        }

        // Handle Manage Users button click
        val btnManageUsers: MaterialButton = view.findViewById(R.id.btnManageUsers)
        btnManageUsers.setOnClickListener {
            val userManagementFragment = UserManagement() // Replace with your fragment class
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, userManagementFragment)
                .addToBackStack(null)
                .commit()
        }

        // Fetch and update user count and product count dynamically
        fetchUserCount(view)
        fetchProductCount(view)

        return view
    }

    private fun loadStaticNGOs(container: LinearLayout) {
        val ngos = arrayOf(
            Organization("Helping Paws", "123 Main Street, City, Country", arrayOf("Adoption", "Rescue", "Fundraising")),
            Organization("Animal Rescue Foundation", "456 Park Avenue, City, Country", arrayOf("Rescue", "Medical Assistance", "Foster Care")),
            Organization("Save Strays", "789 Oak Road, City, Country", arrayOf("Rescue", "Adoption", "Awareness"))
        )
        displayStaticData(container, ngos)
    }


    private fun loadStaticVets(container: LinearLayout) {
        val vets = arrayOf(
            Organization("Dr. Smith - PetCare Clinic", "101 Vet Lane, City, Country", arrayOf("Veterinary Services", "Vaccination", "Emergency Care")),
            Organization("Dr. Emily - Happy Paws Veterinary", "202 Animal Road, City, Country", arrayOf("Spaying/Neutering", "Dental Care", "Emergency Services")),
            Organization("Dr. John - Animal Health Center", "303 Paws Boulevard, City, Country", arrayOf("Medical Checkup", "Vaccination", "Surgical Services"))
        )
        displayStaticData(container, vets)
    }

    private fun loadStaticGroomers(container: LinearLayout) {
        val groomers = arrayOf(
            Organization("Fur & Shine", "123 Groom Street, City, Country", arrayOf("Bathing", "Haircut", "Nail Clipping")),
            Organization("Paws & Relax", "456 Grooming Avenue, City, Country", arrayOf("Brushing", "Haircut", "Ear Cleaning")),
            Organization("The Grooming Lounge", "789 Poodle Way, City, Country", arrayOf("Full Grooming", "Teeth Cleaning", "Nail Care"))
        )
        displayStaticData(container, groomers)
    }

    private fun displayStaticData(container: LinearLayout, organizations: Array<Organization>) {
        val inflater = LayoutInflater.from(requireContext())
        container.removeAllViews()

        for (organization in organizations) {
            // Inflate the layout for each organization
            val itemView = inflater.inflate(R.layout.admin_organization_ragister, container, false)

            // Set the owner name and address
            val ownerNameTextView: TextView = itemView.findViewById(R.id.owner_name)
            ownerNameTextView.text = organization.name
            val addressTextView: TextView = itemView.findViewById(R.id.address)
            addressTextView.text = organization.address

            // Find the services container
            val servicesContainer: LinearLayout = itemView.findViewById(R.id.services_container)
            servicesContainer.removeAllViews() // Clear any previous services

            // Loop through the services and add them as TextViews
            for (service in organization.services) {
                val serviceTextView = TextView(requireContext()).apply {
                    text = service
                    textSize = 14f
                    setTextColor(Color.parseColor("#666666"))
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 4, 0, 4) // Add margin between services
                    }
                }
                servicesContainer.addView(serviceTextView)
            }

            // Set the layout params for the itemView
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val margin = 16
            val marginInPixels = (margin * resources.displayMetrics.density).toInt()
            layoutParams.setMargins(0, marginInPixels, 0, marginInPixels)
            itemView.layoutParams = layoutParams

            // Add the itemView (card) to the container
            container.addView(itemView)
        }
    }


    // Fetch product data from Firestore
    private fun fetchProducts(productContainer: LinearLayout) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                productContainer.removeAllViews() // Clear existing views
                val inflater = LayoutInflater.from(requireContext())
                var rowLayout: LinearLayout? = null
                var productCount = 0

                for ((index, document) in documents.withIndex()) {
                    val productData = document.data
                    val productCard = inflater.inflate(R.layout.admin_product_card, productContainer, false)

                    // Set product details
                    productCard.findViewById<TextView>(R.id.dynamicTextView).text = productData["name"].toString()
                    productCard.findViewById<TextView>(R.id.dynamicPrice).text = "₹${productData["price"].toString()}"

                    // Handle description with dynamic multiline support
                    val descriptionTextView: TextView = productCard.findViewById(R.id.dynamicDescription)
                    descriptionTextView.text = productData["description"].toString()
                    descriptionTextView.maxLines = 3
                    descriptionTextView.ellipsize = android.text.TextUtils.TruncateAt.END

                    // Load product image using Glide
                    Glide.with(requireContext())
                        .load(productData["imageUrl"].toString())
                        .placeholder(R.drawable.dummy_product)
                        .into(productCard.findViewById(R.id.imageView))

                    // Handle Edit button click
                    productCard.findViewById<Button>(R.id.btnEdit).setOnClickListener {
                        val editProductFragment = EditProductFragment().apply {
                            arguments = Bundle().apply {
                                putString("productId", document.id)
                                putString("productName", productData["name"].toString())
                                putString("productPrice", productData["price"].toString())
                                putString("productDescription", productData["description"].toString())
                                putString("productImageUrl", productData["imageUrl"].toString())
                            }
                        }

                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container, editProductFragment)
                            .addToBackStack(null)
                            .commit()
                    }

                    // Handle Delete button click
                    productCard.findViewById<Button>(R.id.deleteButton).setOnClickListener {
                        // Call deleteProduct method to remove product from Firestore
                        deleteProduct(document.id, productCard, productContainer)
                    }

                    // Add product card to the row
                    if (productCount % 2 == 0) {
                        rowLayout = LinearLayout(requireContext()).apply {
                            orientation = LinearLayout.HORIZONTAL
                            layoutParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            ).apply {
                                setMargins(0, 0, 0, 13)
                            }
                        }
                        productContainer.addView(rowLayout)
                    }

                    rowLayout?.addView(productCard, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
                    productCount++
                }
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error fetching products", e)
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch number of users from Firestore and update the UI
    private fun fetchUserCount(view: View) {
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .get()
            .addOnSuccessListener { documents ->
                val userCount = documents.size()
                view.findViewById<TextView>(R.id.registeredUsersCount).text = userCount.toString()
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error fetching users", e)
                Toast.makeText(requireContext(), "Error fetching users: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Fetch number of products from Firestore and update the UI
    private fun fetchProductCount(view: View) {
        val db = FirebaseFirestore.getInstance()

        db.collection("products")
            .get()
            .addOnSuccessListener { documents ->
                val productCount = documents.size()
                view.findViewById<TextView>(R.id.totalProductsCount).text = productCount.toString()
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error fetching products", e)
                Toast.makeText(requireContext(), "Error fetching products: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    // Delete product from Firestore
    private fun deleteProduct(productId: String, productCard: View, productContainer: LinearLayout) {
        val db = FirebaseFirestore.getInstance()

        // Delete product from the products collection
        db.collection("products").document(productId)
            .delete()
            .addOnSuccessListener {
                // Show success message and remove the product card from the UI
                Toast.makeText(requireContext(), "Product deleted successfully", Toast.LENGTH_SHORT).show()
                productContainer.removeView(productCard) // Remove the product card from the container
            }
            .addOnFailureListener { e ->
                Log.e("AdminScreenFragment", "Error deleting product", e)
                Toast.makeText(requireContext(), "Error deleting product: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
