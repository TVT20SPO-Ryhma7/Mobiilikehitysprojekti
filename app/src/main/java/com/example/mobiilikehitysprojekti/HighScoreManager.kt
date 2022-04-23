package com.example.mobiilikehitysprojekti

import android.os.Debug
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import com.google.firestore.v1.Document
import java.lang.reflect.Array
import java.util.logging.Handler

// Interface to deal with game related data stored in the Firebase
// For example, game scores should be updated through this class in order to rank players accordingly
class HighScoreManager(firestoreInstance: FirebaseFirestore) {

    private var fs = firestoreInstance

    // Locally cached leaderboards for each game
    private var cachedLeaderboardSnake: MutableList<LeaderboardItem> = mutableListOf()
    private var cachedLeaderboardTetris: MutableList<LeaderboardItem> = mutableListOf()
    private var cachedLeaderboardTrivia: MutableList<LeaderboardItem> = mutableListOf()
    private var cachedLeaderboardSpeed: MutableList<LeaderboardItem> = mutableListOf()

    init {

    }

    // Representation of available games in the system
    enum class Game{
        SNAKE,
        TETRIS,
        TRIVIA,
        SPEED,
    }

    // Converts given game enum to a corresponding string ID in the database
    private fun gameToDocumentId(game: Game): String{
             return when(game){
            Game.SNAKE -> "Snake"
            Game.TETRIS -> "Tetris"
            Game.TRIVIA -> "Trivia"
            Game.SPEED -> "SpeedGame"
        }
    }

    // Creates new high-score field to a given document for specified user
    // Callback returns boolean value of whether new field was created
    private fun createFieldForUser(user: FirebaseUser? ,document: DocumentSnapshot, callback: (result: Boolean) -> Unit){
        if (user == null){
            Log.w("High-ScoreManager","Given FirebaseUser was null!")
            return
        }

        Log.i("High-ScoreManager", "Attempting to create new field in '" + document.id + "' for user '" + user!!.email + "'...")

        // If user has no field in the database, create new field
        if(document[user.uid] == null)
        {

            fs.collection("High-Scores")
                .document(document.id)
                .set(hashMapOf<String, Any>(
                    user.uid to 0
                ), SetOptions.merge()).addOnCompleteListener() {
                    Log.i("High-ScoreManager", "New field created in '" + document.id + "' for user '" + user!!.email + "' successfully")

                    // Invoke callback
                    callback.invoke(true)
                }
        }
        else{
            Log.w("High-ScoreManager", "Failed to create new field in '" + document.id + "' for user '" + user!!.email + "', field for this user may already exist!")

            // Invoke callback
            callback.invoke(false)
        }
    }

    // Updates high-score for the given game if it exceeds previous record
    // Callback returns boolean value of whether new high-score was recorded or not
    open fun updateHighScore(score: Int, game: Game, user: FirebaseUser?, callback: (result: Boolean) -> Unit){
        if (user == null){
            Log.w("High-ScoreManager","Given FirebaseUser was null, high-score cannot be updated!")
            return
        }

        val documentId = gameToDocumentId(game)

        fun compareAndUpdateHighScore(document: DocumentSnapshot){
            // Compare new score to already existing high-score
            // If new score exceeds previous amount, update it to the database
            if (score > document[user.uid].toString().toInt()){
                fs.collection("High-Scores")
                    .document(documentId)
                    .update(user.uid,score)
                Log.i("High-ScoreManager",user.email + "'s new high-score of '" + score +"' was updated to database for game " + documentId)
                // Invoke callback
                callback.invoke(true)
            }
            else{
                // Invoke callback
                callback.invoke(false)
            }
        }

        // Get snapshot of the 'High-Scores' document from the database
        fs.collection("High-Scores")
            .document(documentId).get().addOnSuccessListener {
                    document ->

                // If user has no field in the database, create new field and update high-score
                if(document[user.uid] == null)
                {
                    createFieldForUser(user, document){

                        // Fetch the updated document with the newly added field
                        fs.collection("High-Scores")
                            .document(documentId).get().addOnSuccessListener {
                                    document ->
                                    compareAndUpdateHighScore(document)
                            }
                    }
                }
                // If a field already exists in user's name, update high-score
                else{
                    compareAndUpdateHighScore(document)
                }
            }
    }

    // Updates the specified locally cached leaderboard with latest information from database
    private fun updateCachedLeaderboard(game: Game, callback: (result: Boolean) -> Unit){
            var allUsers: MutableMap<String, Any>?
            var updatedLeaderboard = mutableListOf<LeaderboardItem>()

            val documentId = gameToDocumentId(game)
            fs.collection("High-Scores")
                .document(documentId).get().addOnSuccessListener {
                        document ->

                    allUsers = document.data

                    // If document contains no data of user high-scores
                    if(allUsers == null){
                        Log.i("High-ScoreManager",
                            "Document '$documentId' has no stored user data"
                        )
                        callback.invoke(false)
                    }

                    allUsers?.forEach(){
                        // TODO: Find usernames by UID (likely requires backend code execution due to security risk of accessing unauthorized user data in client)
                        updatedLeaderboard.add(LeaderboardItem(it.key, it.key, it.value.toString().toFloat(), 0))
                    }

                    // Assign ranks for each user in cached leaderboard
                    updatedLeaderboard.sortByDescending { it.score }
                    var currentRank = 1
                    updatedLeaderboard.forEach(){
                        it.rank = currentRank
                        currentRank++
                    }

                    // Apply new list
                   when(game){
                       Game.SNAKE -> {
                           cachedLeaderboardSnake = updatedLeaderboard
                       }
                       Game.TETRIS -> {
                            cachedLeaderboardTetris = updatedLeaderboard
                       }
                       Game.TRIVIA -> {
                           cachedLeaderboardTrivia = updatedLeaderboard
                       }
                       Game.SPEED -> {
                           cachedLeaderboardSpeed = updatedLeaderboard
                       }
                   }

                    Log.i("High-ScoreManager","Updated locally cached leaderboard of '$game'")

                    // Invoke callback after task is done
                    callback.invoke(true)
                }
    }

    // Gets high-score if a given user in a game
    // Callback returns the value of user's high-score
    open fun getHighScore(user: FirebaseUser?, game: Game, callback: (score: Float) -> Unit){
        if (user == null){
            Log.w("High-ScoreManager","Given FirebaseUser was null, cannot get user's high-score!")
            return
        }

        // Initialize return variable
        var userHighScore = 0f

        val documentId = gameToDocumentId(game)
        fs.collection("High-Scores")
            .document(documentId).get().addOnSuccessListener {
                    document ->

                if (document.get(user.uid) == null){
                    Log.i("High-ScoreManager","Given FirebaseUser has no high-score in game '$game', returning 0")
                }
                else{
                    userHighScore = document.get(user.uid).toString().toFloat()
                }

                // Invoke callback after task is done
                callback.invoke(userHighScore)
            }
    }


    // Gets rank of a given user in a game
    // Callback returns value of the user's rank
    open fun getRanking(user: FirebaseUser? , game: Game, callback: (rank: Int) -> Unit){

        if (user == null){
            Log.w("High-ScoreManager","Given FirebaseUser was null, cannot get user's rank!")
            return
        }

        // Initialize return variable
        var userRank = 0

        var userHasRank = false

        // Update cached leaderboard
        updateCachedLeaderboard(game){

            // Fetch the corresponding cached leaderboard
            val cachedLeaderboard = when(game){
                Game.SNAKE -> {
                    cachedLeaderboardSnake
                }
                Game.TETRIS -> {
                    cachedLeaderboardTetris
                }
                Game.TRIVIA -> {
                    cachedLeaderboardTrivia
                }
                Game.SPEED -> {
                    cachedLeaderboardSpeed
                }
            }


            cachedLeaderboard.forEach(){
                if (user.uid == it.userId){
                    userRank = it.rank
                    userHasRank = true
                }
            }

            if (!userHasRank){
                Log.i("High-ScoreManager","Given FirebaseUser has no rank in game '$game', returning 0")
            }

            // Invoke callback after task is done
            callback.invoke(userRank)
        }
    }

    // Gets the full updated leaderboard of a game
    // Callback returns the list of LeaderboardItems
    open fun getLeaderboard(game: Game, callback: (leaderboard: List<LeaderboardItem>) -> Unit){
        when(game){
            Game.SNAKE -> {
                updateCachedLeaderboard(game){
                    callback.invoke(cachedLeaderboardSnake)
                }
            }
            Game.TETRIS -> {
                updateCachedLeaderboard(game){
                    callback.invoke(cachedLeaderboardTetris)
                }
            }
            Game.TRIVIA -> {
                updateCachedLeaderboard(game){
                    callback.invoke(cachedLeaderboardTrivia)
                }
            }
            Game.SPEED -> {
                updateCachedLeaderboard(game){
                    callback.invoke(cachedLeaderboardSpeed)
                }
            }
        }
    }

}

data class LeaderboardItem(val userId: String, val userName: String, var score: Float, var rank: Int){

}