package com.example.gym_tracker_app_2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gym_tracker_app_2.ui.main.HistoryDisplayAdapter

class HistoryScreen : ComponentActivity() {
    companion object {
        val workouts: ArrayList<Workout> = ArrayList()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.workout_history_layout)
        workouts.clear()
        workouts.addAll(HomeScreen.databaseInterface.getWorkouts())

        val workoutContainer: RecyclerView = findViewById(R.id.workoutContainer)
        workoutContainer.layoutManager = LinearLayoutManager(this)
        workoutContainer.adapter = HistoryDisplayAdapter(workouts, applicationContext)
    }
}