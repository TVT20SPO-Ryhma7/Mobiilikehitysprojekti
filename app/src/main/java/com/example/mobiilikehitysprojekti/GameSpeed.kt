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

class GameSpeed : AppCompatActivity() {

    //Declaring Buttons and textviews
    private lateinit var startGameButton: Button
    private lateinit var redButton: Button
    private lateinit var yellowButton: Button
    private lateinit var greenButton: Button
    private lateinit var orangeButton: Button
    private lateinit var pointsTextView: TextView
    private lateinit var highScoreTextView: TextView

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

    private lateinit var highScoreManager: HighScoreManager

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
        highScoreManager = HighScoreManager(firebaseFirestore)
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
    }

    private fun loseGame() {
        //Stops the game logic loop
        handler.removeCallbacks(updater)
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

        //This prevents the interval from going to zero
        if (gameLogicInterval > 10) {
            gameLogicInterval -= when (gameLogicInterval) {
                in 600..700 -> {
                    5
                }
                in 500..599 -> {
                    4
                }
                in 400..499 -> {
                    3
                }
                in 300..399 -> {
                    2
                }
                else -> {
                    1
                }
            }
        }

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
        orangeButton.setBackgroundColor(getColor(R.color.orange_200))
    }

    private fun checkHighScore() {
        // Request possible high-score update and handle callback function
        highScoreManager.updateHighScore(points,HighScoreManager.Game.SPEED,firebaseAuth.currentUser) {
            result ->
            // If new high-score was recorded
            if (result){
                //Making congratulation textview visible
                highScoreTextView.visibility = View.VISIBLE
            }
            else {
                //Making congratulation textview invisible
                highScoreTextView.visibility = View.INVISIBLE
            }
        }
    }

    //This executes when the user goes back to main menu
    override fun onStop() {
        super.onStop()
        //Clearing the handler data from memory
        handler.removeCallbacksAndMessages(null)
    }
}