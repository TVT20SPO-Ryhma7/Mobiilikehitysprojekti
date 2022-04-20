package com.example.mobiilikehitysprojekti

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.preference.PreferenceManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    //Authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    //Database
    private lateinit var firebaseFirestore: FirebaseFirestore

    //Error logging tag
    private companion object{
        private const val TAG = "GOOGLE_SIGN_IN_TAG"
    }

    //Things needed for sidebar
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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

        //Sign in button in sidebar
        navigationView = findViewById(R.id.navigationViewLoggedOut)
        val headerView: View = navigationView.getHeaderView(0)
        val signInButton: Button = headerView.findViewById(R.id.btnLogIn)
        signInButton.setOnClickListener {
            //Starting sign in intent
            val signInIntent:Intent = googleSignInClient.signInIntent
            signInGoogle.launch(signInIntent)
        }

        //Initializing game cardview buttons
        val matopeliCard: View = findViewById(R.id.mcvMatopeli)
        matopeliCard.setOnClickListener(gameClick)
        val tetrisCard: View = findViewById(R.id.mcvTetris)
        tetrisCard.setOnClickListener(gameClick)
        val triviaCard: View = findViewById(R.id.mcvTrivia)
        triviaCard.setOnClickListener(gameClick)
        val nopeusPeliCard: View = findViewById(R.id.mcvNopeuspeli)
        nopeusPeliCard.setOnClickListener(gameClick)

        //Sidebar button in appbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Initializing sidebar
        drawerLayout = findViewById(R.id.drawerLoggedOut)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        //Listener for opening sidebar
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        //Settings
        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false)
        var sharedPref: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        //Setting for changing theme
        var themePref: String? = sharedPref.getString("theme", "")
        if (themePref == "Light" || themePref == "Vaalea") {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO)
        } else if (themePref == "Dark" || themePref == "Tumma") {
            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
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

    //Starting activity for google sign in
    private var signInGoogle = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult()) { result ->
        Log.i("Auth","Starting Google sign in activity...")
        if (result.resultCode == Activity.RESULT_OK) { // Execution fails here if you have no linked SHA fingerprint to Firebase!
            //Getting signed in google account and passing it to handleResult() function
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
        else {
            Log.i("Auth","Google sign in failed with result code of: ${result.resultCode}")
            println("Check SHA Fingerprint!")
        }
    }

    //Handling google sign in result
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        Log.i("Auth","Handling Google sign in results...")
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            //If task returns account object passing it to firebaseAuthWithGoogleAccount() function
            if (account != null) {
                firebaseAuthWithGoogleAccount(account)
            }
        //Logging error message
        } catch (e: Exception){
            Log.d(TAG, "onActivityResult: ${e.message}")
        }
    }

    //Authenticating user to firebase with google user
    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount){
        Log.i("Auth","Making authentication with Google account to Firebase...")
        //Getting a credential with google account id token
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        //Using the credential to sign the account in to firebase
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task->
            if(task.isSuccessful) {
                //Checking if user is new or already registered
                if (task.result.additionalUserInfo!!.isNewUser) {
                    Log.i("Auth","New user sign-in detected, creating a new Firestore profile for the user...")
                    //Creating a document with user id in firestore and initializing points to 0
                    firebaseFirestore
                        .collection("Scores")
                        .document(firebaseAuth.currentUser!!.uid)
                        .set(hashMapOf<String, Any>(
                            "MatopeliPts" to 0,
                            "TetrisPts" to 0,
                            "TriviaPts" to 0,
                            "SpeedGamePts" to 0
                        ))
                }
                //Starting logged in -activity
                Log.i("Auth","Starting activity in signed in state...")
                val intent = Intent(this, MainActivityLoggedIn::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    //Signing in automatically when starting the app
    override fun onStart() {
        super.onStart()
        //Looking for an account that was signed in last with this device
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            //Starting logged in -activity
            startActivity(Intent(this, MainActivityLoggedIn::class.java))
            finish()
        }
    }

    //Listener for clicking game buttons
    private val gameClick: View.OnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.mcvMatopeli -> {
                // Creates new intent and loads 'GameSnake' activity
                val snakeIntent: Intent = Intent(this,GameSnake::class.java)
                snakeIntent.putExtra("CurrentUser", firebaseAuth.currentUser?.uid)
                this.startActivity(snakeIntent)
            }
            R.id.mcvTetris -> {
                //Placeholder
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
}