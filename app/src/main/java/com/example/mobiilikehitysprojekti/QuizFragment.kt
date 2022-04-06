package com.example.mobiilikehitysprojekti

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.mobiilikehitysprojekti.databinding.FragmentQuizBinding

class QuizFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil.inflate<FragmentQuizBinding>(inflater,
            R.layout.fragment_quiz, container, false)

        binding.optionOne.setOnClickListener(optionClick)
        binding.optionTwo.setOnClickListener(optionClick)
        binding.optionThree.setOnClickListener(optionClick)
        binding.optionFour.setOnClickListener(optionClick)

        return binding.root
    }

    // Listener for clicking question options
    private val optionClick: View.OnClickListener = View.OnClickListener { view ->
        view.findNavController().navigate(R.id.action_quizFragment_to_resultFragment)
    }
}