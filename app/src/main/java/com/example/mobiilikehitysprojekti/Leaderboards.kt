package com.example.mobiilikehitysprojekti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class Leaderboards : AppCompatActivity() {

    // Firebase
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseFirestore: FirebaseFirestore

    // HighScoreManager
    private lateinit var highScoreManager: HighScoreManager

    // Components
    private lateinit var tvLeaderboard: TextView
    private lateinit var tvLeaderboardName: TextView
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
        tvLeaderboardName = findViewById(R.id.tvLeaderboardGameName)
        bSelectSnake = findViewById(R.id.buttonSelectSnake)
        bSelectTetris = findViewById(R.id.buttonSelectTetris)
        bSelectTrivia = findViewById(R.id.buttonSelectTrivia)
        bSelectSpeed = findViewById(R.id.buttonSelectSpeed)

        // Set button listeners
        bSelectSnake.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.SNAKE, getString(R.string.snake_name))
        }
        bSelectTetris.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.TETRIS, getString(R.string.tetris_name))
        }
        bSelectTrivia.setOnClickListener(){
            loadLeaderboard(HighScoreManager.Game.TRIVIA, getString(R.string.trivia_name))
        }
        bSelectSpeed.setOnClickListener() {
            loadLeaderboard(HighScoreManager.Game.SPEED, getString(R.string.speedtest_name))
        }


    }

    // Loads given game leaderboard on to the leaderboard view
    private fun loadLeaderboard(game: HighScoreManager.Game, gameName: String){

        // Gets the leaderboard
        highScoreManager.getLeaderboard(game){
            leaderboard ->

            tvLeaderboardName.visibility = View.VISIBLE

            tvLeaderboardName.text = gameName + " " + getString(R.string.leaderboard)

            // Begin constructing text to display for the leaderboard
            var leaderboardText: String = ""

            leaderboardText += getString(R.string.rank) + " / " + getString(R.string.score) + " / " + getString(R.string.username)
            leaderboardText += "\n"

            leaderboard.forEach(){
                leaderboardText += it.rank.toString()
                leaderboardText += "         "
                leaderboardText += it.score.roundToInt()
                leaderboardText += "             "

                if(it.userId == firebaseAuth.currentUser?.uid){
                    leaderboardText += getString(R.string.you)
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