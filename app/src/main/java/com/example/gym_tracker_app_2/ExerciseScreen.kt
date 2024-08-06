package com.example.gym_tracker_app_2

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gym_tracker_app_2.ui.main.ExerciseListDisplayAdapter

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