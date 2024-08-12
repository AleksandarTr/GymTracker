package com.example.gym_tracker_app_2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ExerciseScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.history_layout)
        val exercises = HomeScreen.databaseInterface.getExerciseTypes()
        exercises.sort()

        val exerciseContainer: RecyclerView = findViewById(R.id.workoutContainer)
        exerciseContainer.layoutManager = LinearLayoutManager(this)
        exerciseContainer.adapter = ExerciseListDisplayAdapter(exercises, applicationContext)
    }
}