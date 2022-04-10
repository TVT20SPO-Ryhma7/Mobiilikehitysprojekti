package com.example.mobiilikehitysprojekti

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.navigation.findNavController
import com.bumptech.glide.Glide
import com.example.mobiilikehitysprojekti.databinding.FragmentQuizBinding
import com.google.firebase.firestore.FirebaseFirestore

class QuizFragment : Fragment() {

    //Database
    private lateinit var db: FirebaseFirestore
    private lateinit var binding: FragmentQuizBinding
    private var questionList: MutableList<Question> = arrayListOf()
    private var index: Int = 0
    private var answer: Int = 0
    private var points: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Initializing firestore database
        db = FirebaseFirestore.getInstance()

        binding = DataBindingUtil.inflate<FragmentQuizBinding>(inflater,
            R.layout.fragment_quiz, container, false)

        // Get category name from categoryFragment
        setFragmentResultListener("category") { key, bundle ->
            val categoryName = bundle.getString("name")

            // Get questions from database and shuffle them
            val collectionName = "${categoryName}Questions"
            db.collection(collectionName)
                .get()
                .addOnSuccessListener {
                    for (question in it) {
                        val questionObject = question.toObject(Question::class.java)
                        questionList.add(questionObject)
                    }
                    questionList.shuffle()
                    setQuestion()
                }
                .addOnFailureListener { exception ->
                    println("Error getting questions: $exception")
                }
        }
        binding.optionOne.setOnClickListener(optionClick)
        binding.optionTwo.setOnClickListener(optionClick)
        binding.optionThree.setOnClickListener(optionClick)
        binding.optionFour.setOnClickListener(optionClick)
        return binding.root
    }

    private fun setQuestion() {
        binding.question.text = questionList[index].question
        if (questionList[index].imageURL != "") {
            Glide.with(this)
                .load(questionList[index].imageURL)
                .into(binding.questionImage)
        } else {
            binding.questionImage.setImageResource(R.drawable.ic_baseline_question)
        }
        binding.optionOne.text = questionList[index].optionOne
        binding.optionTwo.text = questionList[index].optionTwo
        binding.optionThree.text = questionList[index].optionThree
        binding.optionFour.text = questionList[index].optionFour
    }

    private val optionClick: View.OnClickListener = View.OnClickListener { view ->
        answer = when (view.id) {
            R.id.optionOne -> 1
            R.id.optionTwo -> 2
            R.id.optionThree -> 3
            else -> 4
        }

        // Check the answer
        if (answer == questionList[index].correctOption) {
            points++
        }

        if (index < 4) {
            index++
            setQuestion()
        } else {
            setFragmentResult("playerPoints", bundleOf("points" to points))
            view.findNavController().navigate(R.id.action_quizFragment_to_resultFragment)
        }
    }
}