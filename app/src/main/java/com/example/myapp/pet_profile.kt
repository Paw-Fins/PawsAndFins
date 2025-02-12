package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment

class PetDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_profile, container, false)

        // Get data from Bundle
        val petName = arguments?.getString("pet_name") ?: "Unknown"
        val petType = arguments?.getString("pet_type") ?: "Unknown"
        val petBreed = arguments?.getString("pet_breed") ?: "Unknown"
        val petAge = arguments?.getInt("pet_age", 0)
        val petWeight = arguments?.getFloat("pet_weight", 0.0f)
        val petGender = arguments?.getString("pet_gender") ?: "Unknown"

        // Assign data to views
        view.findViewById<TextView>(R.id.tvPetName).text = petName
        view.findViewById<TextView>(R.id.tvPetType).text = petType
        view.findViewById<TextView>(R.id.tvPetBreed).text = petBreed
        view.findViewById<TextView>(R.id.tvPetAge).text = "$petAge years"
        view.findViewById<TextView>(R.id.tvPetWeight).text = "$petWeight kg"
        view.findViewById<TextView>(R.id.tvPetGender).text = petGender

        // Handle Add Pet Button Click
        view.findViewById<Button>(R.id.btnAddPet).setOnClickListener {
            val addPetFragment = PetRegistrationFragment() // Navigate to add pet screen
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, addPetFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    companion object {
        fun newInstance(petName: String, petType: String, petBreed: String, petAge: Int, petWeight: Float, petGender: String): PetDetailsFragment {
            val fragment = PetDetailsFragment()
            val args = Bundle()
            args.putString("pet_name", petName)
            args.putString("pet_type", petType)
            args.putString("pet_breed", petBreed)
            args.putInt("pet_age", petAge)
            args.putFloat("pet_weight", petWeight)
            args.putString("pet_gender", petGender)
            fragment.arguments = args
            return fragment
        }
    }
}
