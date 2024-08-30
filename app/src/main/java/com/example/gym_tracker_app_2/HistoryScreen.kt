package com.example.gym_tracker_app_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HistoryScreen : ComponentActivity() {
    private lateinit var workoutContainer: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        val workouts = HomeScreen.databaseInterface.getWorkouts()
        workouts.sortByDescending { workout -> workout.date }

        workoutContainer = findViewById(R.id.workoutContainer)
        workoutContainer.layoutManager = LinearLayoutManager(this)
        workoutContainer.adapter = HistoryDisplayAdapter(workouts, applicationContext)
    }

    override fun onResume() {
        super.onResume()
        (workoutContainer.adapter as HistoryDisplayAdapter).checkIfWorkoutExists()
    }
}