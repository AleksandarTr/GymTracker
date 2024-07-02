package com.example.gym_tracker_app_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen_layout)
    }

    fun newWorkoutClick(view: View) {
        val intent : Intent = Intent(applicationContext, WorkoutScreen::class.java)
        startActivity(intent)
    }
}