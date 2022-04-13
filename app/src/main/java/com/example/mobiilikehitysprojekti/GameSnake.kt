package com.example.mobiilikehitysprojekti

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text
import java.security.acl.Group
import java.util.*
import kotlin.random.Random

// Snake game activity class
class GameSnake : AppCompatActivity() {

    // Variables -----------------------------------------------------------------------------------

    // Activity variables--------------------
    private lateinit var firebaseAuth :FirebaseAuth

    private lateinit var gameView: ImageView
    private lateinit var buttonUp: Button
    private lateinit var buttonDown: Button
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button
    private lateinit var buttonTogglePause: Button
    private lateinit var textScore: TextView

    private lateinit var viewGameOverBackground: ImageView
    private lateinit var textGameOverTitle: TextView
    private lateinit var textGameOverScore: TextView
    private lateinit var textGameOverReason: TextView
    private lateinit var buttonPlayAgain: Button
    private lateinit var groupGameOver: androidx.constraintlayout.widget.Group

    private lateinit var groupGamePaused: androidx.constraintlayout.widget.Group

    // Game variables -----------------------
    private var isStarted: Boolean = false  // Whether game has been started with startGame function
    private var isPaused: Boolean = false   // Whether already started game has been paused
    private var gameClock: Timer = Timer()
    private val gameLogicInterval: Long =
        500 // Interval in milliseconds how often game logic gets executed

    // Minimum size for each dimension is 10
    private val gameDimensionX: Int = 20
    private val gameDimensionY: Int = 20

    // Represents all available game input options
    enum class GameInput{NONE,UP,DOWN,LEFT,RIGHT}
    private var nextInput: GameInput = GameInput.NONE

    private var snakeStartLength: Int = 5   // Max safe start length is 4

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    private var gameSnakeEngine: GameSnakeEngine = GameSnakeEngine(gameDimensionX,gameDimensionY,snakeStartLength)



    // Functions -----------------------------------------------------------------------------------

    // When activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Default activity stuff, I guess. No touchy!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_snake)

        // Initialize firebase
        firebaseAuth = FirebaseAuth.getInstance()

        // Get component references
        gameView = findViewById(R.id.gameView)
        buttonUp = findViewById(R.id.buttonMoveUp)
        buttonDown = findViewById(R.id.buttonMoveDown)
        buttonLeft = findViewById(R.id.buttonMoveLeft)
        buttonRight = findViewById(R.id.buttonMoveRight)
        buttonTogglePause = findViewById(R.id.buttonTogglePause)
        textScore = findViewById(R.id.textViewScore)
        viewGameOverBackground = findViewById(R.id.imageViewEndScreenBackground)
        textGameOverTitle = findViewById(R.id.textViewTitleGameOver)
        textGameOverScore = findViewById(R.id.textViewGameOverScore)
        textGameOverReason = findViewById(R.id.textViewGameOverReason)
        buttonPlayAgain = findViewById(R.id.buttonGameOverPlayAgain)
        groupGameOver = findViewById(R.id.groupEndScreen)
        groupGamePaused = findViewById(R.id.groupPause)

        // Set button listeners
        buttonUp.setOnClickListener(){
            // Do this when up is pressed
            if (!isStarted){startGame()}

            nextInput = GameInput.UP
        }
        buttonDown.setOnClickListener(){
            // Do this when down is pressed
            if (!isStarted){startGame()}

            nextInput = GameInput.DOWN
        }
        buttonLeft.setOnClickListener(){
            // Do this when left is pressed
            if (!isStarted){startGame()}

            nextInput = GameInput.LEFT
        }
        buttonRight.setOnClickListener(){
            // Do this when right is pressed
            if (!isStarted){startGame()}

            nextInput = GameInput.RIGHT
        }
        buttonTogglePause.setOnClickListener(){
            if (isPaused){
                resumeGame()
            }
            else{
                pauseGame()
            }
        }
        buttonPlayAgain.setOnClickListener(){
            // Restarts the game
            stopGame()
            startGame()
        }

        // Prompt the user to start game by pressing any button
        Toast.makeText(this,"Press any button to start the game!",Toast.LENGTH_SHORT).show()

        groupGameOver.visibility = View.INVISIBLE

    }

    // When another activity takes in place and this one is no longer visible
    override fun onStop() {
        // Default activity stuff, I guess. No touchy!
        super.onStop()

        // Stop the game
        stopGame()
    }

    // Starts the game from the beginning
    private fun startGame(){
        if (isStarted){return}
        Log.i("GameStatus","Game Started")

        // Initialize game clock and start it, effectively starting the game
        gameClock = Timer()
        gameClock.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Forces the logic to run on same thread as UI, prevents application from crashing
                runOnUiThread(Runnable(){
                    // Execute this logic every tick
                    onGameTick()
                })
            }
        }, 0, gameLogicInterval)

        gameSnakeEngine.newGame()

        isStarted = true
        resumeGame()
    }

    // Stops the game entirely, can only be started again with startGame function
    private fun stopGame(){
        if (!isStarted){return}
        Log.i("GameStatus","Game Stopped")

        // Stops and clears the game clock
        gameClock.cancel()
        gameClock.purge()

        isStarted = false
    }

    // Pauses the game to its current state, resume with continueGame function
    private fun pauseGame(){
        if (isPaused || gameSnakeEngine.gameOver){return}
        Log.i("GameStatus","Game Paused")

        gameSnakeEngine.isPaused = true
        buttonTogglePause.text = "Resume"
        groupGamePaused.visibility = View.VISIBLE

        isPaused = true
    }

    // Continues paused game from its last state
    private fun resumeGame(){
        if (!isPaused){return}
        Log.i("GameStatus","Game Resumed")

        gameSnakeEngine.isPaused = false
        buttonTogglePause.text = "Pause"
        groupGamePaused.visibility = View.INVISIBLE

        isPaused = false
    }

    // Gets called in regular intervals to advance game state
    private fun onGameTick() {

        handleGameLogic()

        // Update view on the user's device
        gameView.setImageBitmap(renderGameState())
    }

    // Advances game state, call once per game tick
    private fun handleGameLogic() {
        // If the game is not over
        if (!gameSnakeEngine.gameOver){

            groupGameOver.visibility = View.INVISIBLE

            // Handle input
            when(nextInput){
                GameInput.NONE -> {}
                GameInput.LEFT -> gameSnakeEngine.snakeStageMoveLeft()
                GameInput.RIGHT -> gameSnakeEngine.snakeStageMoveRight()
                GameInput.UP -> gameSnakeEngine.snakeStageMoveUp()
                GameInput.DOWN -> gameSnakeEngine.snakeStageMoveDown()

            }

            // Advance game state
            gameSnakeEngine.nextTick()

            // Update text view for the score
            val scoreText = "Score: " + gameSnakeEngine.score.toString()
            textScore.text = scoreText

            // Reset next input
            nextInput = GameInput.NONE
        }

        // If the game is over (even during the same logic cycle)
        if (gameSnakeEngine.gameOver){
            val scoreText = "Your score was: " + gameSnakeEngine.score.toString()
            val gameOverReasonText = "Game ended because " + gameSnakeEngine.gameOverReason

            textGameOverScore.text = scoreText
            textGameOverReason.text = gameOverReasonText

            groupGameOver.visibility = View.VISIBLE
        }



    }

    // Draws a bitmap based on current game state, bitmap dimension are defined by game dimensions
    private fun renderGameState(): Bitmap {
        // Initialize bitmap
        val renderedView: Bitmap =
            Bitmap.createBitmap(gameDimensionX, gameDimensionY, Bitmap.Config.ARGB_8888)

        // Iterates all values from game state and set corresponding pixels
        for (x in 0 until gameSnakeEngine.getState().size) {
            for (y in 0 until gameSnakeEngine.getState()[0].size) {

                // If plot is empty (0)
                if (gameSnakeEngine.getState()[x][y] == 0) {
                    renderedView.setPixel(x, y, Color.WHITE)
                }

                // If plot is occupied by snake (1)
                else if (gameSnakeEngine.getState()[x][y] == 1) {
                    renderedView.setPixel(x, y, Color.BLACK)
                }

                // If plot is occupied by food (2)
                else if (gameSnakeEngine.getState()[x][y] == 2) {
                    renderedView.setPixel(x, y, Color.RED)
                }

                // If plot value is unsupported
                else {
                    renderedView.setPixel(x, y, Color.MAGENTA)
                }
            }


        }
        return renderedView
    }



}

// Class to handle and keep track of current state of a snake game
private class GameSnakeEngine(gameSizeX: Int, gameSizeY: Int, snakeStartLength: Int){

    // Initialization ------------------------------------------------------------------------------

    // The current state cycle where the game logic is at
    var currentTick: Long = 0
        get
        private set

    private var sizeX: Int = gameSizeX
    private var sizeY: Int = gameSizeY
    private var startLength: Int = snakeStartLength

    init{

        // If any of the game dimensions are less than 10, set them to minimum of 10
        if (sizeX < 10){sizeX = 10}
        if (sizeY < 10){sizeY = 10}

        // If snake start length is more than safe starting length of 4, set to maximum of 4
        //if (startLength > 4){startLength = 4}
    }

    // Game Related --------------------------------------------------------------------------------

    open var score: Int = 0
    get
    private set


    // State Related -------------------------------------------------------------------------------

    open var gameOver: Boolean = false
    get
    private set

    open var gameOverReason: String = ""
    get
    private set

    // If game state is set to pause, nextTick() method has no effect
    open var isPaused: Boolean = false
    get
    set

    // Directions of how the game map can be navigated with
    enum class Direction{UP,DOWN,LEFT,RIGHT}

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    // 0 = Empty Plot
    // 1 = Plot Occupied by Snake
    // 2 = Plot Occupied by Food
    private var gameState: Array<IntArray> = Array(sizeX) { IntArray(sizeY) }

    // Automatically create a new snake with its head starting position in the center of the world
    private var snake: Snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength)

    // Creates new instance for food
    private var food: Food = Food()


    // Staged Variables ----------------------------------------------------------------------------
    // These variables get reset to their default values each game cycle

    // Next path the snake will attempt to take in current game cycle
    private var stagedSnakeNextMoveDirection: Snake.MoveDirection = Snake.MoveDirection.FORWARD





    // Functions -----------------------------------------------------------------------------------

    // Applies staged changes to current game state cycle and
    // Should be called once per tick to advance the game!
    open fun nextTick(){
        // Do not execute any logic if game is paused or over
        if (isPaused || gameOver){return}

        Log.i("Game", "Tick: $currentTick")

        // Move snake according to proposed changes
        if (snake.move(stagedSnakeNextMoveDirection)){
            gameOver = true
            gameOverReason = "Snake hit its own tail"
        }


        // Reset proposed change variables
        stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD

        // Check whether snake has gone outside the bounds
        if ( snake.getSnakeHead().x > sizeX - 1 || snake.getSnakeHead().y > sizeY - 1
            || snake.getSnakeHead().x < 0 || snake.getSnakeHead().y  < 0 )
        {
            gameOver = true
            gameOverReason = "Snake hit a wall"
        }

        // If game over conditions have been met
        if (gameOver){
            Log.i("Game", "Game Over! Reason: $gameOverReason Your score was: $score")
        }

        // Check if snake ate food
        if(snake.getSnakeHead().x == food.position.x && snake.getSnakeHead().y == food.position.y){
            snake.expand(1)
            food.spawnNewFood(sizeX,sizeY,snake.getSnakeBody().toTypedArray())
            score++
            Log.i("Game", "Food eaten! Current score is: $score")
        }



        // Set empty state
        setStateToSingleValue(0)

        // Add snake to state
        snake.getSnakeBody().forEach(){
            setPlot(1 ,it.x,it.y)
        }

        // Add food to state
        if (food.exists){
            setPlot(2,food.position.x,food.position.y)
        }

        currentTick++
    }

    // Sets single plot in the game state to a given value
    private fun setPlot(value: Int, x: Int, y: Int){
        // If given plot exists outside of world bounds, return
        if ( x > sizeX - 1 || y > sizeY - 1 || x < 0 || y  < 0 ) {return}

        gameState[x][y] = value
    }

    // Returns 2D array of the current game state
    open fun getState(): Array<IntArray> {
        return gameState
    }


    // Sets all values in game state to given parameter
    private fun setStateToSingleValue(value: Int){

        // Iterates all values from game state and sets them
        for (x in 0 until gameState.size) {
            for (y in 0 until gameState[0].size) {

                gameState[x][y] = value
            }
        }
    }

    // Sets current game state to a default beginning of a new game of snake
    open fun newGame(){
        gameOver = false
        currentTick = 0
        score = 0

        // Set all values to 0
        for (x in 0 until gameState.size) {
            for (y in 0 until gameState[0].size) {

                gameState[x][y] = 0
            }
        }

        // Create a new snake
        snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength)

        // Place starting snake food
        food.spawnNewFood(sizeX,sizeY,snake.getSnakeBody().toTypedArray())

    }



    // Snake Related -------------------------------------------------------------------------------



    // Gets the current length of the snake
    open fun getSnakeLength(): Int{
        // TODO

        return 0
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveUp(){
        when(snake.getSnakeHeading()){
            GameSnakeEngine.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
            GameSnakeEngine.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveDown(){
        when(snake.getSnakeHeading()){
            GameSnakeEngine.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
            GameSnakeEngine.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveLeft(){
        when(snake.getSnakeHeading()){
            GameSnakeEngine.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
            GameSnakeEngine.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
            GameSnakeEngine.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveRight() {
        when (snake.getSnakeHeading()) {
            GameSnakeEngine.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
            GameSnakeEngine.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
            GameSnakeEngine.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            GameSnakeEngine.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
        }
    }


    // Helper --------------------------------------------------------------------------------------

    // Returns the value of an adjacent block
    private fun checkAdjacentPlotValue(plot: Vector2Int, direction: GameSnakeEngine.Direction):Int{
        var adjacentPlot: Vector2Int = getAdjacentPlotPosition(plot,direction)
        return gameState[adjacentPlot.x][adjacentPlot.y]
    }


    // Static --------------------------------------------------------------------------------------
    companion object{

        // Gets the position of a plot adjacent the the given origin and given direction
        open fun getAdjacentPlotPosition(plot: Vector2Int, direction: GameSnakeEngine.Direction): Vector2Int{
            var adjacentPlot: Vector2Int = Vector2Int(0,0) // Initialize variable

            when(direction){
                GameSnakeEngine.Direction.UP -> {
                    adjacentPlot.x = plot.x
                    adjacentPlot.y = plot.y - 1
                }
                GameSnakeEngine.Direction.DOWN -> {
                    adjacentPlot.x = plot.x
                    adjacentPlot.y = plot.y + 1
                }
                GameSnakeEngine.Direction.LEFT -> {
                    adjacentPlot.x = plot.x - 1
                    adjacentPlot.y = plot.y
                }
                GameSnakeEngine.Direction.RIGHT -> {
                    adjacentPlot.x = plot.x + 1
                    adjacentPlot.y = plot.y
                }
            }
            return adjacentPlot
        }

        // Checks from origin that in which proportional direction does a destination reside
        open fun directionFromOrigin(origin: Vector2Int, destination: Vector2Int): GameSnakeEngine.Direction{
            var dominantDirection: GameSnakeEngine.Direction // Initialize local variable

            var longestMagnitude: Int = 0
            val posXMag: Int = destination.x - origin.x
            val negXMag: Int = origin.x - destination.x
            val posYMag: Int = destination.y - origin.y
            val negYMag: Int = origin.y - destination.y

            // Check positive X magnitude (initial)
            longestMagnitude = posXMag
            dominantDirection = GameSnakeEngine.Direction.RIGHT

            // Check negative X magnitude
            if (longestMagnitude < negXMag){
                longestMagnitude = negXMag
                dominantDirection = GameSnakeEngine.Direction.LEFT
            }

            // Check positive Y magnitude
            if (longestMagnitude < posYMag){
                longestMagnitude = posYMag
                dominantDirection = GameSnakeEngine.Direction.DOWN
            }

            // Check negative Y magnitude
            if (longestMagnitude < negYMag){
                longestMagnitude = negYMag
                longestMagnitude = negYMag
                dominantDirection = GameSnakeEngine.Direction.UP
            }

            return dominantDirection
        }
    }


}




// Class to keep track of the snake's position
private class Snake(headStartPosition: Vector2Int, startLength: Int){

    // Current length of the snake
    var length: Int = 0
    get
    private set

    // The amount of times the snake should expand itself by the tail when it moves
    private var expandBy: Int = 0

    // Stores the coordinate values of the snakes body, array index 0 is the snakes head
    private var snakeBody: MutableList<Vector2Int> = mutableListOf(headStartPosition)

    // Current true direction of which the snake is moving if it headed forward
    private var headingSnakeEngine: GameSnakeEngine.Direction = GameSnakeEngine.Direction.RIGHT

    // The movement paths snake can take
    enum class MoveDirection{FORWARD,LEFT,RIGHT}

    init {
        // Initialize snake body by first placing its head and then constructing its body onto the left side
        for(i in 0 until startLength){
            snakeBody.add(Vector2Int(headStartPosition.x - (i+1), headStartPosition.y))
        }
        length = snakeBody.size
    }

    // Gets the current state of the snake body including head
    open fun getSnakeBody(): MutableList<Vector2Int>{
        return snakeBody
    }

    // Gets the current state of the snake head only
    open fun getSnakeHead(): Vector2Int{
        return snakeBody.first()
    }


    // Gets the current true heading of the snake if it moved forward
    open fun getSnakeHeading(): GameSnakeEngine.Direction{
        return headingSnakeEngine
    }

    // Adds new piece to the snake next time it moves
    open fun expand(amount: Int){
        expandBy += amount
    }

    // Tries to move snake to a given direction if it is not obstructed by its own body, returns true if obstructed
    open fun move(direction: MoveDirection): Boolean{
        var hasCollidedWithSelf: Boolean = false
        var headPositionOld: Vector2Int = getSnakeHead()
        var headPositionNew: Vector2Int = getPlotPositionOnSnakePath(direction)



        // Check whether snake would collide with itself if it moved to this plot
        hasCollidedWithSelf = snakePathIsObstructed(direction)

        // Physically move the snake if it hasn't collided with itself
        if (!hasCollidedWithSelf){

            // Move the snake
            moveSnakeToPlot(headPositionNew)

            // Update snake true heading
            headingSnakeEngine = GameSnakeEngine.directionFromOrigin(headPositionOld,headPositionNew)

            return hasCollidedWithSelf
        }
        else{

            Log.i("Snake","The snake has collided with itself, and thus cannot move to this direction!")
            return hasCollidedWithSelf
        }


    }


    // Moves snakes head and its body to a plot, add new snake pieces to the end if requested
    private fun moveSnakeToPlot(plot: Vector2Int){
        var headPreviousPlot: Vector2Int = getSnakeHead()
        var headNewPlot: Vector2Int = plot

        var bodyPartNewPlot: Vector2Int = headPreviousPlot
        // Move rest of the body
        for(i in 0 until getSnakeBody().size){
            // Move the head first to its new position
            if (snakeBody[i] == getSnakeBody().first()){
                snakeBody[i] = headNewPlot
            }
            // Move rest of the body
            else{
                var cachedBodyNewPlot = snakeBody[i]
                snakeBody[i] = bodyPartNewPlot
                bodyPartNewPlot = cachedBodyNewPlot

                // Add new snake piece if this is the last movable body part and expansion has been requested
                if (expandBy > 0 && snakeBody[i] == getSnakeBody().last()){
                    snakeBody.add(Vector2Int(bodyPartNewPlot.x,bodyPartNewPlot.y))
                    expandBy--
                }

            }
        }
        // Update length variable
        length = snakeBody.size
    }



    // Gets the adjacent plot position proportional to the snake's head's heading and given move direction
    private fun getPlotPositionOnSnakePath(direction: MoveDirection):Vector2Int{
        when(headingSnakeEngine){
            GameSnakeEngine.Direction.UP -> {
                return when(direction){
                    MoveDirection.FORWARD -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.UP)
                    MoveDirection.LEFT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.LEFT)
                    MoveDirection.RIGHT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.RIGHT)
                }
            }
            GameSnakeEngine.Direction.DOWN -> {
                return when(direction){
                    MoveDirection.FORWARD -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.DOWN)
                    MoveDirection.LEFT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.RIGHT)
                    MoveDirection.RIGHT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.LEFT)
                }
            }
            GameSnakeEngine.Direction.LEFT -> {
                return when(direction){
                    MoveDirection.FORWARD -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.LEFT)
                    MoveDirection.LEFT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.DOWN)
                    MoveDirection.RIGHT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.UP)
                }
            }
            GameSnakeEngine.Direction.RIGHT -> {
                return when(direction){
                    MoveDirection.FORWARD -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.RIGHT)
                    MoveDirection.LEFT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.UP)
                    MoveDirection.RIGHT -> GameSnakeEngine.getAdjacentPlotPosition(getSnakeHead(),GameSnakeEngine.Direction.DOWN)
                }
            }
        }
    }

    // Checks whether a snake body part occupies a plot on one of the snake's possible paths
    // Returns true if occupied
    private fun snakePathIsObstructed(direction: MoveDirection):Boolean{
        val plotToCheck: Vector2Int = getPlotPositionOnSnakePath(direction)
        getSnakeBody().forEach(){
            if (it.x == plotToCheck.x && it.y == plotToCheck.y){ return true }
        }
        return false

    }


 // Legacy -----------------------------------------------------------------------------------------

    // Checks whether given plot is occupied by a snake body part, returns true if occupied
    //private fun checkPlot(plot: Vector2Int): Boolean{
   //  getSnakeBody().forEach(){
     //    if (it == plot){return true}
     //}
     //return false
 }


// Class for handling the snakes food
private class Food(){

    open var exists: Boolean = false
    open var position: Vector2Int = Vector2Int.zero



    // Spawns a new piece of food with given world constrains
    open fun spawnNewFood(maxWorldBoundX: Int, maxWorldBoundY: Int, obstructedPlots: Array<Vector2Int>?){

        // Generate a list of vacant plots based on given constraints and obstructions
        val vacantPlots: MutableList<Vector2Int> = mutableListOf()
        for (x in 0 until maxWorldBoundX){
            for (y in 0 until maxWorldBoundY){
                if (obstructedPlots != null){
                    var isObstructed: Boolean = false
                        obstructedPlots.forEach {
                            if (x == it.x && y == it.y){
                                isObstructed = true
                                return@forEach
                            }
                    }
                    if (!isObstructed){
                        vacantPlots.add(Vector2Int(x,y))
                    }
                }
                else{
                    vacantPlots.add(Vector2Int(x,y))
                }
            }
        }

        // Select random vacant spot from the list
        var newFoodPosition = vacantPlots[Random.nextInt(vacantPlots.size)]
        position = newFoodPosition
        exists = true

        Log.i("Food", "New food spawned at: (" + position.x + "," + position.y + ").")
    }
}



// 2 dimensional class representation of a vector presented in integers
private class Vector2Int(valueX: Int, valueY: Int){

    open var x: Int = valueX
    open var y: Int = valueY

    companion object{

        open var zero: Vector2Int = Vector2Int(0,0)
            get(){return Vector2Int(0,0) }
    }



}