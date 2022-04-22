package com.example.mobiilikehitysprojekti

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.w3c.dom.Text
import java.util.*

class MainActivityLoggedIn : AppCompatActivity() {

    //Authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    //Database
    private lateinit var firebaseFirestore: FirebaseFirestore

    //Things needed for sidebar
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // HighScoreManager
    private lateinit var highScoreManager: HighScoreManager

    //Error logging tag
    private companion object{
        private const val TAG = "FIRESTORE"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_logged_in)

        //Configuring the options for google sign-in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.def_web_client_id))
            .requestEmail()
            .build()

        //Initializing google sign in client
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //Initializing firebase authentication
        firebaseAuth = FirebaseAuth.getInstance()

        //Initializing firestore database
        firebaseFirestore = FirebaseFirestore.getInstance()

        // Initialize HS Manager
        highScoreManager = HighScoreManager(firebaseFirestore)

        //Sign out button in sidebar
        navigationView = findViewById(R.id.navigationViewLoggedIn)
        val headerView: View = navigationView.getHeaderView(0)
        val signOutButton: Button = headerView.findViewById(R.id.btnLogOut)
        signOutButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
                //Starting sign out intent
                firebaseAuth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        //Displaying user info in main menu and sidebar
        val textViewUserName: TextView = headerView.findViewById(R.id.tvUserName)
        textViewUserName.text = firebaseAuth.currentUser!!.displayName
        val textViewUserInfo: TextView = findViewById(R.id.tvUserInfo)
        textViewUserInfo.text = firebaseAuth.currentUser!!.displayName

        //Initializing game cardview buttons
        val matopeliCard: View = findViewById(R.id.mcvMatopeli)
        matopeliCard.setOnClickListener(gameClick)
        val tetrisCard: View = findViewById(R.id.mcvTetris)
        tetrisCard.setOnClickListener(gameClick)
        val triviaCard: View = findViewById(R.id.mcvTrivia)
        triviaCard.setOnClickListener(gameClick)
        val nopeusPeliCard: View = findViewById(R.id.mcvNopeuspeli)
        nopeusPeliCard.setOnClickListener(gameClick)

        //Initializing game points textviews
        val textViewMatopeliPts: TextView = findViewById(R.id.tvMatopeliPoints)
        val textViewTetrisPts: TextView = findViewById(R.id.tvTetrisPoints)
        val textViewTriviaPts: TextView = findViewById(R.id.tvTriviaPoints)
        val textViewSpeedGamePoints: TextView = findViewById(R.id.tvNopeuspeliPoints)

        // Initialize game ranks
        val textViewGameSnakeRank: TextView = findViewById(R.id.tvMatopeliRank)
        val textViewGameTetrisRank: TextView = findViewById(R.id.tvTetrisRank)
        val textViewGameTriviaRank: TextView = findViewById(R.id.tvTriviaRank)
        val textViewGameSpeedRank: TextView = findViewById(R.id.tvNopeuspeliRank)

        // Initialize buttons
        val buttonViewLeaderboards: Button = findViewById(R.id.btnLeaderboards)


        // Update game score view from db
        highScoreManager.getHighScore(firebaseAuth.currentUser,HighScoreManager.Game.SNAKE, callback = {
            score ->
            textViewMatopeliPts.text = score.toString()
        })
        highScoreManager.getHighScore(firebaseAuth.currentUser,HighScoreManager.Game.TETRIS, callback = {
                score ->
            textViewTetrisPts.text = score.toString()
        })
        highScoreManager.getHighScore(firebaseAuth.currentUser,HighScoreManager.Game.TRIVIA, callback = {
                score ->
            textViewTriviaPts.text = score.toString()
        })
        highScoreManager.getHighScore(firebaseAuth.currentUser,HighScoreManager.Game.SPEED, callback = {
                score ->
            textViewSpeedGamePoints.text = score.toString()
        })

        // Update game rank view from db
        highScoreManager.getRanking(firebaseAuth.currentUser,HighScoreManager.Game.SNAKE, callback = {
                rank ->
            // If user has no rank in game
            if (rank == 0){
                textViewGameSnakeRank.text = getString(R.string.unranked)
            }
            else{
                textViewGameSnakeRank.text = rank.toString()
            }

        })
        highScoreManager.getRanking(firebaseAuth.currentUser,HighScoreManager.Game.TETRIS, callback = {
                rank ->
            // If user has no rank in game
            if (rank == 0){
                textViewGameTetrisRank.text = getString(R.string.unranked)
            }
            else{
                textViewGameTetrisRank.text = rank.toString()
            }
        })
        highScoreManager.getRanking(firebaseAuth.currentUser,HighScoreManager.Game.TRIVIA, callback = {
                rank ->
            // If user has no rank in game
            if (rank == 0){
                textViewGameTriviaRank.text = getString(R.string.unranked)
            }
            else{
                textViewGameTriviaRank.text = rank.toString()
            }
        })
        highScoreManager.getRanking(firebaseAuth.currentUser,HighScoreManager.Game.SPEED, callback = {
                rank ->
            // If user has no rank in game
            if (rank == 0){
                textViewGameSpeedRank.text = getString(R.string.unranked)
            }
            else{
                textViewGameSpeedRank.text = rank.toString()
            }
        })


        // Set button listeners
        buttonViewLeaderboards.setOnClickListener(){
            // Start leaderboards activity
            val leaderboardsIntent = Intent(this, Leaderboards::class.java)
            startActivity(leaderboardsIntent)
        }

        //Sidebar button in appbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Initializing sidebar
        drawerLayout = findViewById(R.id.drawerLoggedIn)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        //Listener for opening sidebar
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        //Settings
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        var sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //Setting for changing theme
        var themePref: String? = sharedPref.getString("theme", "")
        if (themePref == getString(R.string.light)) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        } else if (themePref == getString(R.string.dark)) {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        }

        //Setting for changing language
        var languagePref: String? = sharedPref.getString("language", "")
        if (languagePref == getString(R.string.english)) {
            changeLanguage("en")
        } else if (languagePref == getString(R.string.finnish)) {
            changeLanguage("fi")
        }

        //Listener for clicking sidebar items
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_settings -> {
                    val settingsIntent = Intent(this, SettingsActivity::class.java)
                    startActivity(settingsIntent)
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    //Listener for clicking game buttons
    private val gameClick: View.OnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.mcvMatopeli -> {
                // Creates new intent and loads 'GameSnake' activity
                val snakeIntent: Intent = Intent(this,GameSnake::class.java)
                this.startActivity(snakeIntent)
            }
            R.id.mcvTetris -> {
                val tetrisIntent = Intent(this, TetrisGame::class.java)
                startActivity(tetrisIntent)
                Toast.makeText(this, "Tetris", Toast.LENGTH_SHORT).show()
            }
            R.id.mcvTrivia -> {
                val triviaIntent = Intent(this, GameTrivia::class.java)
                startActivity(triviaIntent)
            }
            R.id.mcvNopeuspeli -> {
                // Creates new intent and loads 'GameSpeed' activity
                val speedGameIntent = Intent(this, GameSpeed::class.java)
                startActivity(speedGameIntent)
            }
        }
    }


    // Listener for leaderboard view button


    //Handler for clicking appbar menu button
    override fun onSupportNavigateUp(): Boolean {
        drawerLayout.openDrawer(navigationView)
        return true
    }

    //Handler for pressing back button
    override fun onBackPressed() {
        if(this.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    //Refreshes main activity when returning from another activity
    override fun onRestart() {
        super.onRestart()
        var intent = intent
        finish()
        startActivity(intent)
    }

    private fun changeLanguage(language: String) {
        val config = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale)
        } else {
            config.locale = locale
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        }
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}