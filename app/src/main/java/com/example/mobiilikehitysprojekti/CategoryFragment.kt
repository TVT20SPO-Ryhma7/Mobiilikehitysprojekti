package com.example.mobiilikehitysprojekti

import android.os.Bundle

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
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
        view.findNavController().navigate(R.id.action_categoryFragment_to_quizFragment)
    }
}