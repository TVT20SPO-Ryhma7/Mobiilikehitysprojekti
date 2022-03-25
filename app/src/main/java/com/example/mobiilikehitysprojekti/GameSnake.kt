package com.example.mobiilikehitysprojekti

import android.graphics.Bitmap
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import java.util.*

// Snake game activity class
class GameSnake : AppCompatActivity() {

    // Variables -----------------------------------------------------------------------------------

    // Activity variables--------------------
    private lateinit var gameView: ImageView
    private lateinit var buttonUp: Button
    private lateinit var buttonDown: Button
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button

    // Game variables -----------------------
    private var isStarted: Boolean = false  // Whether game has been started with startGame function
    private var isPaused: Boolean = false   // Whether already started game has been paused
    private var gameClock: Timer = Timer()
    private var currentTick: Long = 0
    private val gameLogicInterval: Long =
        500 // Interval in milliseconds how often game logic gets executed

    // Minimum size for each dimension is 10
    private val gameDimensionX: Int = 20
    private val gameDimensionY: Int = 20

    // Represents all available game input options
    enum class GameInput{NONE,UP,DOWN,LEFT,RIGHT}
    private var nextInput: GameInput = GameInput.NONE

    private var score: Int = 0
    private var snakeLength: Int = 0
    private var snakeStartLength: Int = 4   // Max safe start length is 4

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    private var gameState: GameState = GameState(gameDimensionX,gameDimensionY,snakeStartLength)



    // Functions -----------------------------------------------------------------------------------

    // When activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        // Default activity stuff, I guess. No touchy!
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_snake)

        // Get component references
        gameView = findViewById(R.id.gameView)
        buttonUp = findViewById(R.id.buttonMoveUp)
        buttonDown = findViewById(R.id.buttonMoveDown)
        buttonLeft = findViewById(R.id.buttonMoveLeft)
        buttonRight = findViewById(R.id.buttonMoveRight)

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

        // Prompt the user to start game by pressing any button
        Toast.makeText(this,"Press any button to start the game!",Toast.LENGTH_SHORT).show()



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
        gameClock.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                // Forces the logic to run on same thread as UI, prevents application from crashing
                runOnUiThread(Runnable(){
                    // Execute this logic every tick
                    onGameTick()
                })
            }
        }, 0, gameLogicInterval)

        gameState.setStateNewGame()

        isStarted = true
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
        if (isPaused){return}
        Log.i("GameStatus","Game Paused")

        // TODO: Pause game logic

        isPaused = true
    }

    // Continues paused game from its last state
    private fun resumeGame(){
        if (!isPaused){return}
        Log.i("GameStatus","Game Resumed")

        // TODO: Continue game logic

        isPaused = false
    }

    // Gets called in regular intervals to advance game state
    private fun onGameTick() {
        Log.i("GameTick", "Tick: $currentTick")
        handleGameLogic()

        // Update view on the user's device
        gameView.setImageBitmap(renderGameState())
        currentTick++
    }

    // Advances game state, call once per game tick
    private fun handleGameLogic() {

        // Handle input
        when(nextInput){
            GameInput.NONE -> gameState.snakeMoveForward()
            GameInput.LEFT -> gameState.snakeMoveLeft()
            GameInput.RIGHT -> gameState.snakeMoveRight()
            GameInput.UP -> gameState.snakeMoveForward()
            GameInput.DOWN -> gameState.snakeMoveForward()

        }

        // TODO: Other game logic


        // Reset next input
        nextInput = GameInput.NONE
    }

    // Draws a bitmap based on current game state, bitmap dimension are defined by game dimensions
    private fun renderGameState(): Bitmap {
        // Initialize bitmap
        val renderedView: Bitmap =
            Bitmap.createBitmap(gameDimensionX, gameDimensionY, Bitmap.Config.ARGB_8888)

        // Iterates all values from game state and set corresponding pixels
        for (x in 0 until gameState.getState().size - 1) {
            for (y in 0 until gameState.getState()[0].size - 1) {

                // If plot is empty (0)
                if (gameState.getState()[x][y] == 0) {
                    renderedView.setPixel(x, y, Color.WHITE)
                }

                // If plot is occupied by snake (1)
                else if (gameState.getState()[x][y] == 1) {
                    renderedView.setPixel(x, y, Color.BLACK)
                }

                // If plot is occupied by food (2)
                else if (gameState.getState()[x][y] == 2) {
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
private class GameState(gameSizeX: Int, gameSizeY: Int, snakeStartLength: Int){

    // Initialization ------------------------------------------------------------------------------

    private var sizeX: Int = gameSizeX
    private var sizeY: Int = gameSizeY
    private var startLength: Int = snakeStartLength

    init{
        // If any of the game dimensions are less than 10, set them to minimum of 10
        if (sizeX < 10){sizeX = 10}
        if (sizeY < 10){sizeY = 10}

        // If snake start length is more than safe starting length of 4, set to maximum of 4
        if (startLength > 4){startLength = 4}
    }

    // Automatically create a new snake with its head starting position in the center of the world
    private var snake: Snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength,sizeX,sizeY)


    // State Related -------------------------------------------------------------------------------

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    // 0 = Empty Plot
    // 1 = Plot Occupied by Snake
    // 2 = Plot Occupied by Food
    private var gameState: Array<IntArray> = Array(sizeX) { IntArray(sizeY) }

    // Update the current game state, gets automatically called whenever game state changes
    private fun updateState(){
        // Draw empty state
        setStateToSingleValue(0)

        // Add snake to state
        snake.getSnakeBody().forEach(){
            setPlot(1 ,it.x,it.y)
        }

        // Add food to state

    }

    // Sets single plot in the game state to a given value
    private fun setPlot(value: Int, x: Int, y: Int){
        // If given plot exists outside of world bounds, return
        if ( x >sizeX || y>sizeY || sizeX < 0 || sizeY< 0 ) {return}

        gameState[x][y] = value
    }

    // Returns 2D array of the current game state
    open fun getState(): Array<IntArray> {
        return gameState
    }


    // Sets all values in game state to given parameter
    open fun setStateToSingleValue(value: Int){

        // Iterates all values from game state and sets them
        for (x in 0 until gameState.size - 1) {
            for (y in 0 until gameState[0].size - 1) {

                gameState[x][y] = value
            }
        }
    }

    // Sets current game state to a default beginning of a new game of snake
    open fun setStateNewGame(){

        // Set all values to 0
        for (x in 0 until gameState.size - 1) {
            for (y in 0 until gameState[0].size - 1) {

                gameState[x][y] = 0
            }
        }

        // Create a new snake
        snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength,sizeX,sizeY)

        // Place starting snake food
        // TODO

        // Apply updates
        updateState()
    }



    // Snake Related -------------------------------------------------------------------------------



    // Gets the current length of the snake
    open fun getSnakeLength(): Int{
        // TODO

        return 0
    }

    // Moves snake's head and its following body to this direction from snake heading
    open fun snakeMoveForward(){
        snake.moveSnake(Snake.SnakeSteer.FORWARD)
        updateState()
    }

    // Moves snake's head and its following body to this direction from snake heading
    open fun snakeMoveLeft(){
        snake.moveSnake(Snake.SnakeSteer.LEFT)
        updateState()
    }

    // Moves snake's head and its following body to this direction from snake heading
    open fun snakeMoveRight(){
        snake.moveSnake(Snake.SnakeSteer.RIGHT)
        updateState()
    }


}


// Class to keep track of the snake's state and position of its body, by default new snake's body is placed left from its head
private class Snake(headStartPosition: Vector2Int, startLength: Int, worldSizeX: Int, worldSizeY: Int){

    // Stores the coordinate values of the snakes body, array index 0 is the snakes head
    private var snakeBody: MutableList<Vector2Int>

    // Directions of which the snake can move on the map
    enum class SnakeHeading{UP,DOWN,LEFT,RIGHT}
    // Current true direction of which the snake is moving if it headed forward
    private var snakeHeading: SnakeHeading = SnakeHeading.RIGHT

    // Direction of which the snake can steer itself
    enum class SnakeSteer{FORWARD,LEFT,RIGHT}
    // Current direction which the snake will attempt to steer itself
    private var snakeSteer: SnakeSteer = SnakeSteer.FORWARD

    init {
        // Create mutable local variable for snake head start position
        var startPosition: Vector2Int = headStartPosition

        // Check if snake's head start position is out of world bounds
        if (startPosition.y > worldSizeX || startPosition.y > worldSizeY){
            // Reposition snake's head within the world bounds in the center
            startPosition.x = worldSizeX/2
            startPosition.y = worldSizeY/2
        }

        // Initialize snake body by first placing its head and then constructing its body onto the left side
        snakeBody = mutableListOf(startPosition)
        for(i in 0 until startLength){
            snakeBody += Vector2Int(startPosition.x - (i+1), startPosition.y)
        }
    }

    // Gets the current state of the snake body
    open fun getSnakeBody(): List<Vector2Int>{
        return snakeBody
    }

    // Gets the current state of the snake head
    open fun getSnakeHead(): Vector2Int{
        return snakeBody.first()
    }

    // Gets the current attempted steering direction of the snake
    open fun getSnakeSteering(): SnakeSteer{
        return snakeSteer
    }

    // Gets the current true heading of the snake if it moved forward
    open fun getSnakeHeading(): SnakeHeading{
        return snakeHeading
    }

    // Moves snake towards a heading, returns true if snake has collided with itself
    open fun moveSnake(steer: SnakeSteer): Boolean{
        var hasCollidedWithSelf: Boolean = false
        var headPreviousPlot: Vector2Int = getSnakeHead()
        var headNewPlot: Vector2Int = getPlotNextToHead(steer)



        // Check whether snake would collide with itself if it moved to this plot
        hasCollidedWithSelf = checkPlotNextToHead(steer)

        // Physically move the snake if it hasn't collided with itself
        if (!hasCollidedWithSelf){
            moveSnakeToPlot(headNewPlot)
        }

        // Debug
        println(hasCollidedWithSelf)
        println("true plot: " + getSnakeHead().x + " " + getSnakeHead().y)
        println("new plot: " + headNewPlot.x + " " + headNewPlot.y)
        println("old plot: " + headPreviousPlot.x + " " + headNewPlot.y)

        // Reset snake steering back to forward
        snakeSteer = SnakeSteer.FORWARD

        // Update snake true heading
        snakeHeading = headingFromOrigin(headPreviousPlot,headNewPlot)
        return hasCollidedWithSelf
    }

    // Moves snakes head and its body to a plot
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


            }
        }
    }

    // Checks whether a snake body part occupies a plot proportional to the snake's head's heading and given steering direction
    // Returns true if occupied
    private fun checkPlotNextToHead(steer: SnakeSteer):Boolean{
        when(snakeHeading){
            SnakeHeading.UP -> {
                return when(steer){
                    SnakeSteer.FORWARD -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                    SnakeSteer.LEFT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                    SnakeSteer.RIGHT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                }
            }
            SnakeHeading.DOWN -> {
                return when(steer){
                    SnakeSteer.FORWARD -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                    SnakeSteer.LEFT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                    SnakeSteer.RIGHT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                }
            }
            SnakeHeading.LEFT -> {
                return when(steer){
                    SnakeSteer.FORWARD -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                    SnakeSteer.LEFT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                    SnakeSteer.RIGHT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                }
            }
            SnakeHeading.RIGHT -> {
                return when(steer){
                    SnakeSteer.FORWARD -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                    SnakeSteer.LEFT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                    SnakeSteer.RIGHT -> checkAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                }
            }
        }
    }

    // Gets the adjacent plot proportional to the snake's head's heading and given steering direction
    private fun getPlotNextToHead(steer: SnakeSteer):Vector2Int{
        when(snakeHeading){
            SnakeHeading.UP -> {
                return when(steer){
                    SnakeSteer.FORWARD -> getAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                    SnakeSteer.LEFT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                    SnakeSteer.RIGHT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                }
            }
            SnakeHeading.DOWN -> {
                return when(steer){
                    SnakeSteer.FORWARD -> getAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                    SnakeSteer.LEFT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                    SnakeSteer.RIGHT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                }
            }
            SnakeHeading.LEFT -> {
                return when(steer){
                    SnakeSteer.FORWARD -> getAdjacentPlot(getSnakeHead(),SnakeHeading.LEFT)
                    SnakeSteer.LEFT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                    SnakeSteer.RIGHT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                }
            }
            SnakeHeading.RIGHT -> {
                return when(steer){
                    SnakeSteer.FORWARD -> getAdjacentPlot(getSnakeHead(),SnakeHeading.RIGHT)
                    SnakeSteer.LEFT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.UP)
                    SnakeSteer.RIGHT -> getAdjacentPlot(getSnakeHead(),SnakeHeading.DOWN)
                }
            }
        }
    }


    // Checks whether given plot is occupied by a snake body part, returns true if occupied
    private fun checkPlot(plot: Vector2Int): Boolean{
        getSnakeBody().forEach(){
            if (it == plot){return true}
        }
        return false
    }

    // Checks whether given blocks given direction is occupied, returns true if occupied
    private fun checkAdjacentPlot(plot: Vector2Int, direction: SnakeHeading):Boolean{
        var adjacentPlot: Vector2Int = Vector2Int(0,0) // Initialize variable
        when(direction){
            SnakeHeading.UP -> {
                adjacentPlot.x = plot.x
                adjacentPlot.y = plot.y + 1
            }
            SnakeHeading.DOWN -> {
                adjacentPlot.x = plot.x
                adjacentPlot.y = plot.y - 1
            }
            SnakeHeading.LEFT -> {
                adjacentPlot.x = plot.x - 1
                adjacentPlot.y = plot.y
            }
            SnakeHeading.RIGHT -> {
                adjacentPlot.x = plot.x + 1
                adjacentPlot.y = plot.y
            }
        }

        return checkPlot(adjacentPlot)
    }

    // Gets the position of a plot adjacent the the given origin and given heading
    private fun getAdjacentPlot(plot: Vector2Int, direction: SnakeHeading): Vector2Int{
        var adjacentPlot: Vector2Int = Vector2Int(0,0) // Initialize variable

        when(direction){
            SnakeHeading.UP -> {
                adjacentPlot.x = plot.x
                adjacentPlot.y = plot.y + 1
            }
            SnakeHeading.DOWN -> {
                adjacentPlot.x = plot.x
                adjacentPlot.y = plot.y - 1
            }
            SnakeHeading.LEFT -> {
                adjacentPlot.x = plot.x - 1
                adjacentPlot.y = plot.y
            }
            SnakeHeading.RIGHT -> {
                adjacentPlot.x = plot.x + 1
                adjacentPlot.y = plot.y
            }
        }

        return adjacentPlot
    }

    // Checks from origin that in which proportional direction does a destination reside
    private fun headingFromOrigin(origin: Vector2Int, destination: Vector2Int): SnakeHeading{
        var dominantHeading: SnakeHeading // Initialize local variable

        var longestMagnitude: Int = 0
        val posXMag: Int = destination.x - origin.x
        val negXMag: Int = origin.x - destination.x
        val posYMag: Int = destination.y - origin.y
        val negYMag: Int = origin.y - destination.y

        // Check positive X magnitude (initial)
        longestMagnitude = posXMag
        dominantHeading = SnakeHeading.RIGHT

        // Check negative X magnitude
        if (longestMagnitude < negXMag){
            longestMagnitude = negXMag
            dominantHeading = SnakeHeading.LEFT
        }

        // Check positive Y magnitude
        if (longestMagnitude < posYMag){
            longestMagnitude = posYMag
            dominantHeading = SnakeHeading.UP
        }

        // Check negative Y magnitude
        if (longestMagnitude < negYMag){
            longestMagnitude = negYMag
            dominantHeading = SnakeHeading.DOWN
        }

        return dominantHeading
    }

}


// 2 dimensional class representation of a vector presented in integers
private class Vector2Int(valueX: Int, valueY: Int){

    open var x: Int = valueX
    open var y: Int = valueY

}