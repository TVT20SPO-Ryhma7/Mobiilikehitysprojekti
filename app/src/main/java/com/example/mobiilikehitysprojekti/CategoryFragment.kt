package com.example.mobiilikehitysprojekti

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.navigation.findNavController
import com.example.mobiilikehitysprojekti.databinding.FragmentCategoryBinding

class CategoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentCategoryBinding>(inflater,
            R.layout.fragment_category, container, false)

        binding.categoryGames.setOnClickListener(categoryClick)
        binding.categorySports.setOnClickListener(categoryClick)
        binding.categoryMusic.setOnClickListener(categoryClick)
        binding.categoryCars.setOnClickListener(categoryClick)
        binding.categoryCountries.setOnClickListener(categoryClick)

        return binding.root
    }

    // Listener for clicking categories
    private val categoryClick: View.OnClickListener = View.OnClickListener { view ->
        val categoryName = when (view.id) {
            R.id.categoryGames -> "Game"
            R.id.categorySports -> "Sport"
            R.id.categoryMusic -> "Music"
            R.id.categoryCars -> "Car"
            R.id.categoryCountries -> "Country"
            else -> ""
        }
        setFragmentResult("category", bundleOf("name" to categoryName))
        view.findNavController().navigate(R.id.action_categoryFragment_to_quizFragment)
    }
}