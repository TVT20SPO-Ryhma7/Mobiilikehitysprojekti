package com.example.mobiilikehitysprojekti

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivityLoggedIn : AppCompatActivity() {

    //Authentication
    private lateinit var googleSignInClient: GoogleSignInClient
    private val firebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    //Things needed for sidebar
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_logged_in)

        //Configuring the google sign-out
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.def_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        //Sign out button in sidebar
        navigationView = findViewById(R.id.navigationViewLoggedIn)
        val headerView: View = navigationView.getHeaderView(0)
        val signOutButton: Button = headerView.findViewById(R.id.btnLogOut)
        signOutButton.setOnClickListener {
            googleSignInClient.signOut().addOnCompleteListener {
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

        //Sidebar button in appbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Initializing sidebar
        drawerLayout = findViewById(R.id.drawerLoggedIn)
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

    //Listener for clicking game buttons
    private val gameClick: View.OnClickListener = View.OnClickListener { view ->
        when (view.id) {
            R.id.mcvMatopeli -> {
                // Creates new intent and loads 'GameSnake' activity
                val snakeIntent: Intent = Intent(this,GameSnake::class.java)
                this.startActivity(snakeIntent)
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