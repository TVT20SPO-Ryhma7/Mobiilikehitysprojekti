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
    private var snakeStartLength: Int = 5   // Max safe start length is 4

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    private var snakeGame: SnakeGame = SnakeGame(gameDimensionX,gameDimensionY,snakeStartLength)



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

        snakeGame.newGame()

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
        Log.i("GameTick", "Tick: ${snakeGame.currentTick}")
        handleGameLogic()

        // Update view on the user's device
        gameView.setImageBitmap(renderGameState())
    }

    // Advances game state, call once per game tick
    private fun handleGameLogic() {

        // Handle input
        when(nextInput){
            GameInput.NONE -> {}
            GameInput.LEFT -> snakeGame.snakeStageMoveLeft()
            GameInput.RIGHT -> snakeGame.snakeStageMoveRight()
            GameInput.UP -> snakeGame.snakeStageMoveUp()
            GameInput.DOWN -> snakeGame.snakeStageMoveDown()

        }

        // Advance game state
        snakeGame.next()

        // Reset next input
        nextInput = GameInput.NONE
    }

    // Draws a bitmap based on current game state, bitmap dimension are defined by game dimensions
    private fun renderGameState(): Bitmap {
        // Initialize bitmap
        val renderedView: Bitmap =
            Bitmap.createBitmap(gameDimensionX, gameDimensionY, Bitmap.Config.ARGB_8888)

        // Iterates all values from game state and set corresponding pixels
        for (x in 0 until snakeGame.getState().size - 1) {
            for (y in 0 until snakeGame.getState()[0].size - 1) {

                // If plot is empty (0)
                if (snakeGame.getState()[x][y] == 0) {
                    renderedView.setPixel(x, y, Color.WHITE)
                }

                // If plot is occupied by snake (1)
                else if (snakeGame.getState()[x][y] == 1) {
                    renderedView.setPixel(x, y, Color.BLACK)
                }

                // If plot is occupied by food (2)
                else if (snakeGame.getState()[x][y] == 2) {
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
private class SnakeGame(gameSizeX: Int, gameSizeY: Int, snakeStartLength: Int){

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




    // State Related -------------------------------------------------------------------------------

    // Directions of how the game map can be navigated with
    enum class Direction{UP,DOWN,LEFT,RIGHT}

    // This 2D array represents the 'game board' (made of 'plots') of which the snake and its food move around
    // 0 = Empty Plot
    // 1 = Plot Occupied by Snake
    // 2 = Plot Occupied by Food
    private var gameState: Array<IntArray> = Array(sizeX) { IntArray(sizeY) }

    // Automatically create a new snake with its head starting position in the center of the world
    private var snake: Snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength)


    // Staged Variables ----------------------------------------------------------------------------
    // These variables get reset to their default values each game cycle

    // Next path the snake will attempt to take in current game cycle
    private var stagedSnakeNextMoveDirection: Snake.MoveDirection = Snake.MoveDirection.FORWARD





    // Functions -----------------------------------------------------------------------------------

    // Applies staged changes to current game state cycle and
    // Should be called once per tick to advance the game!
    open fun next(){

        // Move snake according to proposed changes
        snake.move(stagedSnakeNextMoveDirection)

        // Reset proposed change variables
        stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD




        // Set empty state
        setStateToSingleValue(0)

        // Add snake to state
        snake.getSnakeBody().forEach(){
            setPlot(1 ,it.x,it.y)
        }

        // Add food to state


        currentTick++
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
    private fun setStateToSingleValue(value: Int){

        // Iterates all values from game state and sets them
        for (x in 0 until gameState.size - 1) {
            for (y in 0 until gameState[0].size - 1) {

                gameState[x][y] = value
            }
        }
    }

    // Sets current game state to a default beginning of a new game of snake
    open fun newGame(){

        // Set all values to 0
        for (x in 0 until gameState.size - 1) {
            for (y in 0 until gameState[0].size - 1) {

                gameState[x][y] = 0
            }
        }

        // Create a new snake
        snake = Snake(Vector2Int(sizeX/2,sizeY/2), startLength)

        // Place starting snake food
        // TODO

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
            SnakeGame.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            SnakeGame.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            SnakeGame.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
            SnakeGame.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveDown(){
        when(snake.getSnakeHeading()){
            SnakeGame.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            SnakeGame.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            SnakeGame.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
            SnakeGame.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveLeft(){
        when(snake.getSnakeHeading()){
            SnakeGame.Direction.UP -> stagedSnakeNextMoveDirection = Snake.MoveDirection.LEFT
            SnakeGame.Direction.DOWN -> stagedSnakeNextMoveDirection = Snake.MoveDirection.RIGHT
            SnakeGame.Direction.LEFT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
            SnakeGame.Direction.RIGHT -> stagedSnakeNextMoveDirection = Snake.MoveDirection.FORWARD
        }
    }

    // Attempts to move if possible snake's head and its following body to this direction from snake heading
    open fun snakeStageMoveRight(){
        when(snake.getSnakeHeading()){
            SnakeGame.Direction.UP -> snake.move(Snake.MoveDirection.RIGHT)
            SnakeGame.Direction.DOWN -> snake.move(Snake.MoveDirection.LEFT)
            SnakeGame.Direction.LEFT -> snake.move(Snake.MoveDirection.FORWARD)
            SnakeGame.Direction.RIGHT -> snake.move(Snake.MoveDirection.FORWARD)
        }
    }


    // Helper --------------------------------------------------------------------------------------

    // Returns the value of an adjacent block
    private fun checkAdjacentPlotValue(plot: Vector2Int, direction: SnakeGame.Direction):Int{
        var adjacentPlot: Vector2Int = getAdjacentPlotPosition(plot,direction)
        return gameState[adjacentPlot.x][adjacentPlot.y]
    }


    // Static --------------------------------------------------------------------------------------
    companion object{

        // Gets the position of a plot adjacent the the given origin and given direction
        open fun getAdjacentPlotPosition(plot: Vector2Int, direction: SnakeGame.Direction): Vector2Int{
            var adjacentPlot: Vector2Int = Vector2Int(0,0) // Initialize variable

            when(direction){
                SnakeGame.Direction.UP -> {
                    adjacentPlot.x = plot.x
                    adjacentPlot.y = plot.y - 1
                }
                SnakeGame.Direction.DOWN -> {
                    adjacentPlot.x = plot.x
                    adjacentPlot.y = plot.y + 1
                }
                SnakeGame.Direction.LEFT -> {
                    adjacentPlot.x = plot.x - 1
                    adjacentPlot.y = plot.y
                }
                SnakeGame.Direction.RIGHT -> {
                    adjacentPlot.x = plot.x + 1
                    adjacentPlot.y = plot.y
                }
            }
            return adjacentPlot
        }

        // Checks from origin that in which proportional direction does a destination reside
        open fun directionFromOrigin(origin: Vector2Int, destination: Vector2Int): SnakeGame.Direction{
            var dominantDirection: SnakeGame.Direction // Initialize local variable

            var longestMagnitude: Int = 0
            val posXMag: Int = destination.x - origin.x
            val negXMag: Int = origin.x - destination.x
            val posYMag: Int = destination.y - origin.y
            val negYMag: Int = origin.y - destination.y

            // Check positive X magnitude (initial)
            longestMagnitude = posXMag
            dominantDirection = SnakeGame.Direction.RIGHT

            // Check negative X magnitude
            if (longestMagnitude < negXMag){
                longestMagnitude = negXMag
                dominantDirection = SnakeGame.Direction.LEFT
            }

            // Check positive Y magnitude
            if (longestMagnitude < posYMag){
                longestMagnitude = posYMag
                dominantDirection = SnakeGame.Direction.DOWN
            }

            // Check negative Y magnitude
            if (longestMagnitude < negYMag){
                longestMagnitude = negYMag
                longestMagnitude = negYMag
                dominantDirection = SnakeGame.Direction.UP
            }

            return dominantDirection
        }
    }


}




// Class to keep track of the snake's position
private class Snake(headStartPosition: Vector2Int, startLength: Int){

    // Stores the coordinate values of the snakes body, array index 0 is the snakes head
    private var snakeBody: MutableList<Vector2Int> = mutableListOf(headStartPosition)

    // Current true direction of which the snake is moving if it headed forward
    private var snakeHeading: SnakeGame.Direction = SnakeGame.Direction.RIGHT

    // The movement paths snake can take
    enum class MoveDirection{FORWARD,LEFT,RIGHT}

    init {
        // Initialize snake body by first placing its head and then constructing its body onto the left side
        for(i in 0 until startLength){
            snakeBody.add(Vector2Int(headStartPosition.x - (i+1), headStartPosition.y))
        }
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
    open fun getSnakeHeading(): SnakeGame.Direction{
        return snakeHeading
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
            snakeHeading = SnakeGame.directionFromOrigin(headPositionOld,headPositionNew)

            return hasCollidedWithSelf
        }
        else{

            Log.i("Snake","The snake has collided with itself, and thus cannot move to this direction!")
            return hasCollidedWithSelf
        }


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



    // Gets the adjacent plot position proportional to the snake's head's heading and given move direction
    private fun getPlotPositionOnSnakePath(direction: MoveDirection):Vector2Int{
        when(snakeHeading){
            SnakeGame.Direction.UP -> {
                return when(direction){
                    MoveDirection.FORWARD -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.UP)
                    MoveDirection.LEFT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.LEFT)
                    MoveDirection.RIGHT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.RIGHT)
                }
            }
            SnakeGame.Direction.DOWN -> {
                return when(direction){
                    MoveDirection.FORWARD -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.DOWN)
                    MoveDirection.LEFT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.RIGHT)
                    MoveDirection.RIGHT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.LEFT)
                }
            }
            SnakeGame.Direction.LEFT -> {
                return when(direction){
                    MoveDirection.FORWARD -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.LEFT)
                    MoveDirection.LEFT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.DOWN)
                    MoveDirection.RIGHT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.UP)
                }
            }
            SnakeGame.Direction.RIGHT -> {
                return when(direction){
                    MoveDirection.FORWARD -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.RIGHT)
                    MoveDirection.LEFT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.UP)
                    MoveDirection.RIGHT -> SnakeGame.getAdjacentPlotPosition(getSnakeHead(),SnakeGame.Direction.DOWN)
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

}



// 2 dimensional class representation of a vector presented in integers
private class Vector2Int(valueX: Int, valueY: Int){

    open var x: Int = valueX
    open var y: Int = valueY

}