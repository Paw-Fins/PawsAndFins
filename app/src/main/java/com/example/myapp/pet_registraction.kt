package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

class PetRegistrationFragment : Fragment() {

    private lateinit var etPetName: EditText
    private lateinit var etPetType: EditText
    private lateinit var etPetBreed: EditText
    private lateinit var etPetWeight: EditText
    private lateinit var etPetAge: EditText
    private lateinit var radioGroupGender: RadioGroup
    private lateinit var btnUpload: Button
    private lateinit var btnSaveProfile: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_pet_registration, container, false)

        // Initialize UI Components
        etPetName = view.findViewById(R.id.etPetName)
        etPetType = view.findViewById(R.id.etPetType)
        etPetBreed = view.findViewById(R.id.etPetBreed)
        etPetWeight = view.findViewById(R.id.etPetWeight)
        etPetAge = view.findViewById(R.id.etPetAge)
        radioGroupGender = view.findViewById(R.id.radioGroupGender)
        btnUpload = view.findViewById(R.id.uploadButton)
        btnSaveProfile = view.findViewById(R.id.saveProfileButton)

        // Handle Save Button Click
        btnSaveProfile.setOnClickListener {
            if (validateInputs()) {
                showSuccessMessage(view)
                navigateToPetDetails()
            }
        }

        return view
    }

    // Validate form inputs
    private fun validateInputs(): Boolean {
        if (etPetName.text.toString().trim().isEmpty()) {
            etPetName.error = "Pet name is required!"
            return false
        }
        if (etPetType.text.toString().trim().isEmpty()) {
            etPetType.error = "Pet type is required!"
            return false
        }
        if (etPetBreed.text.toString().trim().isEmpty()) {
            etPetBreed.error = "Pet breed is required!"
            return false
        }
        if (etPetWeight.text.toString().trim().isEmpty()) {
            etPetWeight.error = "Pet weight is required!"
            return false
        }
        if (etPetAge.text.toString().trim().isEmpty()) {
            etPetAge.error = "Pet age is required!"
            return false
        }

        val weight = etPetWeight.text.toString().toDoubleOrNull()
        val age = etPetAge.text.toString().toIntOrNull()

        if (weight == null || weight <= 0) {
            etPetWeight.error = "Enter a valid weight!"
            return false
        }
        if (age == null || age <= 0) {
            etPetAge.error = "Enter a valid age!"
            return false
        }

        if (radioGroupGender.checkedRadioButtonId == -1) {
            Toast.makeText(requireContext(), "Please select a gender!", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    // Collect pet data
    private fun navigateToPetDetails() {
        val petName = etPetName.text.toString().trim()
        val petType = etPetType.text.toString().trim()
        val petBreed = etPetBreed.text.toString().trim()
        val petWeight = etPetWeight.text.toString().toFloat()
        val petAge = etPetAge.text.toString().toInt()

        val selectedGenderId = radioGroupGender.checkedRadioButtonId
        val gender = view?.findViewById<RadioButton>(selectedGenderId)?.text.toString()

        // Pass data to PetDetailsFragment
        val petDetailsFragment = PetDetailsFragment.newInstance(
            petName = petName,
            petType = petType,
            petBreed = petBreed,
            petAge = petAge,
            petWeight = petWeight,
            petGender = gender
        )

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, petDetailsFragment)
            .addToBackStack(null)
            .commit()
    }

    // Show success message
    private fun showSuccessMessage(view: View) {
        Snackbar.make(view, "Pet profile saved successfully!", Snackbar.LENGTH_LONG).show()
    }
}
