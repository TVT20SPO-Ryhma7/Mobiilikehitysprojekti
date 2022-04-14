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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResultFragment : Fragment() {

    // Database and auth
    private lateinit var db: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var binding: FragmentResultBinding

    private var points: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        // Initialize firestore database and auth
        db = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()

        binding = DataBindingUtil.inflate<FragmentResultBinding>(inflater,
            R.layout.fragment_result, container, false)

        // Get points name from QuizFragment
        setFragmentResultListener("playerPoints") { key, bundle ->
            points = bundle.getInt("points")

            //Checks if player got a new high score
            checkHighScore()

            var resultString = ""
            when (points) {
                0,1 -> {
                    resultString = getString(R.string.you_can_do_better)
                    binding.resultImage.setImageResource(R.drawable.ic_baseline_mood_bad_24)
                }
                2,3 ->  {
                    resultString = getString(R.string.good_job)
                    binding.resultImage.setImageResource(R.drawable.ic_baseline_mood_24)
                }
                else -> {
                    resultString = getString(R.string.excellent_job)
                    binding.resultImage.setImageResource(R.drawable.ic_baseline_mood_24)
                }
            }
            val txtYouGot = getString(R.string.you_got)
            val txtPoints = getString(R.string.points)

            binding.resultText.text = resultString
            binding.resultPoints.text = "$txtYouGot $points / 5 $txtPoints !"
        }

        binding.backToCategoriesButton.setOnClickListener { view : View ->
            view.findNavController().navigate(R.id.action_resultFragment_to_categoryFragment)
        }
        return binding.root
    }

    private fun checkHighScore() {
        //Gets players current high score from database
        db.collection("Scores")
            .document(firebaseAuth.currentUser!!.uid).get().addOnSuccessListener { document ->
                //Checks if the score is bigger than current high score
                if (points > document["TriviaPts"].toString().toInt()) {
                    binding.highScoreText.visibility = View.VISIBLE
                    //Updates the high score into database
                    db.collection("Scores")
                        .document(firebaseAuth.currentUser!!.uid)
                        .update(hashMapOf<String, Any>(
                            "TriviaPts" to points
                        ))
                }
            }
    }

}