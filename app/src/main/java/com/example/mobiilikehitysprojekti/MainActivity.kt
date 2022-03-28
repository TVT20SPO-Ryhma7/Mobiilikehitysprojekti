package com.example.mobiilikehitysprojekti

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import java.lang.Exception

class MainActivity : AppCompatActivity() {

    //Authentication
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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

        //Configuring the google sign-in
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.def_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //Initializing firebase authentication
        firebaseAuth = FirebaseAuth.getInstance()

        //Sign in button in sidebar
        navigationView = findViewById(R.id.navigationViewLoggedOut)
        val headerView: View = navigationView.getHeaderView(0)
        val signInButton: Button = headerView.findViewById(R.id.btnLogIn)
        signInButton.setOnClickListener {
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

        //Listener for clicking sidebar items
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_gamePref -> {
                    //Placeholder
                    Toast.makeText(this, "game preferences", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_settings -> {
                    //Placeholder
                    Toast.makeText(this, "settings", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> {
                    false
                }
            }
        }
    }

    //Beginning google sign in
    private var signInGoogle = registerForActivityResult (
        ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task:Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleResult(task)
        }
    }

    //Handling google sign in result
    private fun handleResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = completedTask.getResult(ApiException::class.java)
            if (account != null) {
                firebaseAuthWithGoogleAccount(account)
            }
        } catch (e: Exception){
            Log.d(TAG, "onActivityResult: ${e.message}")
        }
    }

    //Authenticating user to firebase with google user
    private fun firebaseAuthWithGoogleAccount(account: GoogleSignInAccount){
        val credential= GoogleAuthProvider.getCredential(account.idToken,null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task->
            if(task.isSuccessful) {
                val intent = Intent(this, MainActivityLoggedIn::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    //Signing in automatically when starting the app
    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null) {
            startActivity(Intent(this, MainActivityLoggedIn::class.java))
            finish()
        }
    }

    //Listener for clicking game buttons
    private val gameClick: View.OnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.mcvMatopeli -> {

            }
            R.id.mcvTetris -> {
                //Placeholder
                Toast.makeText(this, "Tetris", Toast.LENGTH_SHORT).show()
            }
            R.id.mcvTrivia -> {
                //Placeholder
                Toast.makeText(this, "Trivia", Toast.LENGTH_SHORT).show()
            }
            R.id.mcvNopeuspeli -> {
                //Placeholder
                Toast.makeText(this, "Nopeuspeli", Toast.LENGTH_SHORT).show()
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
}