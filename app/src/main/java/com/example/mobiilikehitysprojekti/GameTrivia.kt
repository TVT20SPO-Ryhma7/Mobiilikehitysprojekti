package com.example.mobiilikehitysprojekti

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.mobiilikehitysprojekti.databinding.ActivityGameTriviaBinding


class GameTrivia : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("UNUSED_VARIABLE")
        val binding = DataBindingUtil.setContentView<ActivityGameTriviaBinding>(this, R.layout.activity_game_trivia)
    }
}