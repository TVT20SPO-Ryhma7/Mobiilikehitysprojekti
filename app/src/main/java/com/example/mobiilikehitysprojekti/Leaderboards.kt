package com.example.mobiilikehitysprojekti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Leaderboards : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    // HighScoreManager
    private lateinit var highScoreManager: HighScoreManager

    // Components
    private lateinit var tvLeaderboard: TextView
    private lateinit var bSelectSnake: Button
    private lateinit var bSelectTetris: Button
    private lateinit var bSelectTrivia: Button
    private lateinit var bSelectSpeed: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboards)

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseFirestore = FirebaseFirestore.getInstance()

        // Initialize hsm
        highScoreManager = HighScoreManager(firebaseFirestore)

        // Initialize components
        tvLeaderboard = findViewById(R.id.textViewLeaderboard)
        bSelectSnake = findViewById(R.id.buttonSelectSnake)
        bSelectTetris = findViewById(R.id.buttonSelectTetris)
        bSelectTrivia = findViewById(R.id.buttonSelectTrivia)
        bSelectSpeed = findViewById(R.id.buttonSelectSpeed)

        // Set button listeners
        bSelectSnake.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.SNAKE)
        }
        bSelectTetris.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.TETRIS)
        }
        bSelectTrivia.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.TRIVIA)
        }
        bSelectSpeed.setOnClickListener() {
            loadLeaderboard(HighScoreManager.Game.SPEED)
        }


    }

    // Loads given game leaderboard on to the leaderboard view
    private fun loadLeaderboard(game: HighScoreManager.Game){

        // Gets the leaderboard
        highScoreManager.getLeaderboard(game){
            leaderboard ->

            // Begin constructing text to display for the leaderboard
            var leaderboardText: String = ""

            leaderboardText += "$game LEADERBOARD"
            leaderboardText += "\n"
            leaderboardText += "Rank / Score / Username"
            leaderboardText += "\n"

            leaderboard.forEach(){
                leaderboardText += it.rank.toString()
                leaderboardText += "   "
                leaderboardText += it.score
                leaderboardText += "   "

                if(it.userId == firebaseAuth.currentUser?.uid){
                    leaderboardText += "You"
                }
                else{
                    leaderboardText += it.userName
                }

                leaderboardText += "\n"
            }

            // Apply new leaderboard view
            tvLeaderboard.text = leaderboardText

        }
    }


}