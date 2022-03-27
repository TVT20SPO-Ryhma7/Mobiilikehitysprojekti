package com.example.mobiilikehitysprojekti

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    //Declaring stuff needed for sidedrawer
    private lateinit var actionBarToggle: ActionBarDrawerToggle
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initializing game cardviews
        val matopeliCard: View = findViewById(R.id.mcvMatopeli)
        matopeliCard.setOnClickListener(gameClick)
        val tetrisCard: View = findViewById(R.id.mcvTetris)
        tetrisCard.setOnClickListener(gameClick)
        val triviaCard: View = findViewById(R.id.mcvTrivia)
        triviaCard.setOnClickListener(gameClick)
        val nopeusPeliCard: View = findViewById(R.id.mcvNopeuspeli)
        nopeusPeliCard.setOnClickListener(gameClick)

        //Menu button in appbar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        drawerLayout = findViewById(R.id.drawer)
        navigationView = findViewById(R.id.navigationView)
        actionBarToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close)

        //Listener for opening sidedrawer
        drawerLayout.addDrawerListener(actionBarToggle)
        actionBarToggle.syncState()

        //Listener for clicking menu items
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

    //Listener for clicking games
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
                val triviaIntent = Intent(this, GameTrivia::class.java)
                startActivity(triviaIntent)
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