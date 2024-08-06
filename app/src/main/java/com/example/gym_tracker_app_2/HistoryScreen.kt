package com.example.gym_tracker_app_2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gym_tracker_app_2.ui.main.HistoryDisplayAdapter

class HistoryScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        val workouts = HomeScreen.databaseInterface.getWorkouts()
        workouts.sortByDescending { workout -> workout.date }

        val workoutContainer: RecyclerView = findViewById(R.id.workoutContainer)
        workoutContainer.layoutManager = LinearLayoutManager(this)
        workoutContainer.adapter = HistoryDisplayAdapter(workouts, applicationContext)
    }
}