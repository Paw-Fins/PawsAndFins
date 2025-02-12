package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment

class AdoptionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_adoption, container, false)

        // Find the container safely
        val adoptionContainer = view.findViewById<LinearLayout>(R.id.adoptionContainer)
        if (adoptionContainer == null) {
            throw RuntimeException("adoptionContainer not found in fragment_adoption.xml")
        }

        // Static pet list
        val pets = listOf(
            Pet("Max", "3 years", "40 cm", "Golden Retriever", "Male", R.drawable.dog),
            Pet("Bella", "2 years", "35 cm", "Labrador", "Female", R.drawable.dog),
            Pet("Charlie", "4 years", "45 cm", "Poodle", "Male", R.drawable.dog)
        )

        pets.forEach { pet ->
            val cardView = LayoutInflater.from(requireContext()).inflate(R.layout.adoption_card, adoptionContainer, false)


            // Bind UI components safely
//            cardView.findViewById<ImageView>(R.id.petImage)?.setImageResource(pet.imageRes)
            cardView.findViewById<TextView>(R.id.petName)?.text = "Name: ${pet.name}"
            cardView.findViewById<TextView>(R.id.petAge)?.text = "Age: ${pet.age}"
            cardView.findViewById<TextView>(R.id.petHeight)?.text = "Height: ${pet.height}"
            cardView.findViewById<TextView>(R.id.petBreed)?.text = "Breed: ${pet.breed}"
            cardView.findViewById<TextView>(R.id.petGender)?.text = "Gender: ${pet.gender}"

            adoptionContainer.addView(cardView)
        }

        return view
    }
}

data class Pet(
    val name: String,
    val age: String,
    val height: String,
    val breed: String,
    val gender: String,
    val imageRes: Int
)
