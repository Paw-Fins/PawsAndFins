package com.example.myapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.fragment.app.Fragment

class HomeScreenFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home_screen, container, false)
        val filterSpinner: Spinner = view.findViewById(R.id.filterSpinner)

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_options,
            R.layout.spinner_item
        )

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        filterSpinner.adapter = adapter

        (requireActivity() as MainActivity).showBottomNavigation(true)

        val productContainer: LinearLayout = view.findViewById(R.id.productContainer)
        val inflater = LayoutInflater.from(requireContext())

        val n = 5

        for (i in 0 until n step 2) {
            val rowLayout = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                val params = layoutParams as LinearLayout.LayoutParams
                params.setMargins(0, 0, 0, 13) // 13px margin at the bottom for row spacing
                layoutParams = params
            }

            val productCard1 = inflater.inflate(R.layout.product_card, rowLayout, false)
            val params1 = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            params1.setMargins(0, 0, 16, 0)
            productCard1.layoutParams = params1
            rowLayout.addView(productCard1)

            if (i + 1 < n) {
                val productCard2 = inflater.inflate(R.layout.product_card, rowLayout, false)
                val params2 = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
//                params2.setMargins(0, 0, 0, 0)
                productCard2.layoutParams = params2
                rowLayout.addView(productCard2)
            }

            productContainer.addView(rowLayout)
        }

        return view
    }
}