package com.example.mobiilikehitysprojekti

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.example.mobiilikehitysprojekti.databinding.FragmentResultBinding


class ResultFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val binding = DataBindingUtil.inflate<FragmentResultBinding>(inflater,
            R.layout.fragment_result, container, false)

        // Get points name from QuizFragment
        setFragmentResultListener("playerPoints") { key, bundle ->
            val points = bundle.getInt("points")
            val resultString = when (points) {
                0,1 -> "You can do better!"
                2,3 -> "Good job!"
                else -> "Excellent job!"
            }
            binding.resultText.text = resultString
            binding.resultPoints.text = "You got $points points out of 5!"
        }

        binding.backToCategoriesButton.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_resultFragment_to_categoryFragment)
        }
        return binding.root
    }

}