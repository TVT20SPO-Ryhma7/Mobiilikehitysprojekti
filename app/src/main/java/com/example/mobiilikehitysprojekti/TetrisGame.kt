package com.example.mobiilikehitysprojekti

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mobiilikehitysprojekti.tetris_models.AppModel
import com.example.mobiilikehitysprojekti.tetris_storage.AppPreference
import com.example.mobiilikehitysprojekti.tetris_view.TetrisView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TetrisGame : AppCompatActivity() {

    var tvHighScore: TextView? = null
    var tvCurrentScore: TextView? = null
    private lateinit var tetrisView: TetrisView

    // Initialize firebase and hsm
    private lateinit var firestore: FirebaseFirestore
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var highScoreManager: HighScoreManager

    var appPreferences: AppPreference? = null
    private val appModel: AppModel = AppModel()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tetrisgame)

        firestore = FirebaseFirestore.getInstance()
        firebaseAuth = FirebaseAuth.getInstance()
        highScoreManager = HighScoreManager(firestore)

        appPreferences = AppPreference(this)
        appModel.setPreferences(appPreferences)

        val btnRestart = findViewById<Button>(R.id.btn_restart)
        tvHighScore = findViewById<TextView>(R.id.tv_high_score)
        tvCurrentScore = findViewById<TextView>(R.id.tv_current_score)
        tetrisView = findViewById<TetrisView>(R.id.view_tetris)
        tetrisView.setActivity(this)
        tetrisView.setModel(appModel)

        tetrisView.setOnTouchListener(this::onTetrisViewTouch)
        btnRestart.setOnClickListener(this::btnRestartClick)

        updateHighScore()
        updateCurrentScore()
    }

    private fun btnRestartClick(view: View) {
        appModel.restartGame()
    }

    private fun onTetrisViewTouch(view: View, event: MotionEvent): 
            Boolean {
        if (appModel.isGameOver() || appModel.isGameAwaitingStart()) { 
            appModel.startGame() 
            tetrisView.setGameCommandWithDelay(AppModel.Motions.DOWN) 
            //Keeps track on the screen and games state to decide if the game is to be launched or block moved when the screen is touched

        } else if(appModel.isGameActive()) {
            when (resolveTouchDirection(view, event)) {
                0 -> moveTetromino(AppModel.Motions.LEFT)
                1 -> moveTetromino(AppModel.Motions.ROTATE)
                2 -> moveTetromino(AppModel.Motions.DOWN)
                3 -> moveTetromino(AppModel.Motions.RIGHT)
            } //on-touch listener for the blocks movement
        }
        return true
    }

    private fun resolveTouchDirection(view: View, event: MotionEvent):
            Int {
        val x = event.x / view.width
        val y = event.y / view.height
        val direction: Int

        direction = if (y > x) {
            if (x > 1 - y) 2 else 0
        }
        else {
            if (x > 1 - y) 3 else 1
        }
        return direction
    }

    private fun moveTetromino(motion: AppModel.Motions) {
        if (appModel.isGameActive()) {
            tetrisView.setGameCommand(motion)
        }
    }

    private fun updateHighScore() {
        tvHighScore?.text = "${appPreferences?.getHighScore()}"

        if(appPreferences != null){
            highScoreManager.updateHighScore(appPreferences!!.getHighScore(),HighScoreManager.Game.TETRIS, firebaseAuth.currentUser){

            }
        }
    }

    private fun updateCurrentScore() {
        tvCurrentScore?.text = "0"
    }
}
