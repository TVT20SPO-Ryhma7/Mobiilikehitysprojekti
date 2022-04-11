package com.example.mobiilikehitysprojekti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class GameSpeed : AppCompatActivity() {

    //Declaring Buttons and textviews
    private lateinit var startGameButton: Button
    private lateinit var redButton: Button
    private lateinit var yellowButton: Button
    private lateinit var greenButton: Button
    private lateinit var orangeButton: Button
    private lateinit var pointsTextView: TextView
    private lateinit var highScoreTextView: TextView

    //Timer to speed up the game
    private val timer = Timer()

    //Time (ms) between game ticks
    private var gameLogicInterval: Long = 700

    //Currently lit up button 1/red, 2/yellow, 3/green, 4/orange
    private var currentButton = 1
    private var randomInt = 0

    //String the game button sequence creates with button numbers "1234"
    private var gameString = ""
    //String the player button presses create
    private var playerString = ""

    private var points = 0

    //This allows running the game code on repeat
    private val handler: Handler = Handler(Looper.getMainLooper())

    //This runs the game logic on repeat
    private val updater = object : Runnable {
        override fun run() {
            onGameTick()
            handler.postDelayed(this, gameLogicInterval)
        }
    }

    //Authentication
    private lateinit var firebaseAuth: FirebaseAuth

    //Database
    private lateinit var firebaseFirestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_speed)

        //Initializing buttons and textview
        startGameButton = findViewById(R.id.btnStartSpeedGame)
        redButton = findViewById(R.id.btnRed)
        yellowButton = findViewById(R.id.btnYellow)
        greenButton = findViewById(R.id.btnGreen)
        orangeButton = findViewById(R.id.btnOrange)
        pointsTextView = findViewById(R.id.tvPointCounter)
        highScoreTextView = findViewById(R.id.tvHighScoreSpeedGame)

        //Listener for clicking the "start the game"-button
        startGameButton.setOnClickListener {
            startGame()
            //Makes the "start the game"-button invisible
            startGameButton.visibility = View.INVISIBLE
        }

        //Listener for clicking the red button
        redButton.setOnClickListener {
            //Adds the button number into the player string
            playerString += "1"
            checkLose()
        }

        //Listener for clicking the yellow button
        yellowButton.setOnClickListener {
            //Adds the button number into the player string
            playerString += "2"
            checkLose()
        }

        //Listener for clicking the green button
        greenButton.setOnClickListener {
            //Adds the button number into the player string
            playerString += "3"
            checkLose()
        }

        //Listener for clicking the orange button
        orangeButton.setOnClickListener {
            //Adds the button number into the player string
            playerString += "4"
            checkLose()
        }

        //Initializing firebase authentication
        firebaseAuth = FirebaseAuth.getInstance()

        //Initializing firestore database
        firebaseFirestore = FirebaseFirestore.getInstance()
    }

    private fun startGame() {
        //re-initializing the game variables
        points = 0
        pointsTextView.text = points.toString()
        highScoreTextView.visibility = View.INVISIBLE
        gameString = ""
        playerString = ""
        gameLogicInterval = 700

        //Starts the game logic loop
        handler.post(updater)

        //Decreases the game logic interval every second
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                //This prevents the interval from going to zero
                if (gameLogicInterval > 10) {
                    gameLogicInterval -= 8
                }
            }
        }, 0, 1000)
    }

    private fun loseGame() {
        //Stops the game logic loop
        handler.removeCallbacks(updater)
        timer.purge()
        //Makes the "start the game"-button visible
        startGameButton.visibility = View.VISIBLE
        //Turns the light off from every button
        clearButtons()
        //Checks if player got a new highscore
        checkHighScore()
    }

    //This executes every game loop
    private fun onGameTick() {
        //Gets a random number between 1-4
        currentButton = getRandomInt()
        //Turns the light off from every button
        clearButtons()

        //Checking which button is current button
        when (currentButton) {
            1 -> {
                //Setting the button color to bright red
                redButton.setBackgroundColor(getColor(R.color.red_A700))
                //Adds the button number into the game string
                gameString += "1"
            }
            2 -> {
                //Setting the button color to bright yellow
                yellowButton.setBackgroundColor(getColor(R.color.yellow_A700))
                //Adds the button number into the game string
                gameString += "2"
            }
            3 -> {
                //Setting the button color to bright green
                greenButton.setBackgroundColor(getColor(R.color.green_A700))
                //Adds the button number into the game string
                gameString += "3"
            }
            4 -> {
                //Setting the button color to bright orange
                orangeButton.setBackgroundColor(getColor(R.color.orange_A700))
                //Adds the button number into the game string
                gameString += "4"
            }
        }
    }

    //Checking if player pressed a wrong button
    private fun checkLose() {
        //Checking if the game string contains the player string
        if (gameString.contains(playerString)) {
            //Adding a point
            points++
            pointsTextView.text = points.toString()
        } else {
            loseGame()
        }
    }

    //Returns a random number between 1-4
    private fun getRandomInt(): Int {
        randomInt = (1..4).random()
        //Checks that the new number is not the same as the one before
        while (randomInt == currentButton) {
            randomInt = (1..4).random()
        }
        return randomInt
    }

    //Turns the light off from every button
    private fun clearButtons() {
        redButton.setBackgroundColor(getColor(R.color.red_200))
        yellowButton.setBackgroundColor(getColor(R.color.yellow_100))
        greenButton.setBackgroundColor(getColor(R.color.green_200))
        orangeButton.setBackgroundColor(getColor(R.color.orange_100))
    }

    private fun checkHighScore() {
        //Gets players current highscore from database
        firebaseFirestore.collection("Scores")
            .document(firebaseAuth.currentUser!!.uid).get().addOnSuccessListener { document ->
                //Checks if the score is bigger than current highscore
                if (points > document["SpeedGamePts"].toString().toInt()) {
                    //Updates the highscore into database
                    firebaseFirestore.collection("Scores")
                        .document(firebaseAuth.currentUser!!.uid)
                        .update(hashMapOf<String, Any>(
                            "SpeedGamePts" to points
                        ))

                    //Congratulates player for new highscore
                    highScoreTextView.visibility = View.VISIBLE
            }
        }
    }

    //This executes when the user goes back to main menu
    override fun onStop() {
        super.onStop()
        //Stopping and clearing the timer
        timer.cancel()
        timer.purge()
        //Clearing the handler data from memory
        handler.removeCallbacksAndMessages(null)
    }
}