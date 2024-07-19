package com.example.gym_tracker_app_2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge

class HomeScreen : ComponentActivity() {
    companion object {
        lateinit var databaseInterface: DatabaseInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen_layout)
        databaseInterface = DatabaseInterface(applicationContext)
    }

    fun newWorkoutClick(view: View) {
        val intent = Intent(applicationContext, WorkoutScreen::class.java)
        intent.putExtra("workoutID", databaseInterface.getNextWorkoutID())
        startActivity(intent)
    }
}