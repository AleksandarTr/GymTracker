package com.example.gym_tracker_app_2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity

class HomeScreen : ComponentActivity() {
    companion object {
        lateinit var databaseInterface: DatabaseInterface
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_screen_layout)
        databaseInterface = DatabaseInterface(applicationContext)

//        databaseInterface.writableDatabase.execSQL("Delete from Workout")
//        databaseInterface.writableDatabase.execSQL("Delete from Exercise")
//        databaseInterface.writableDatabase.execSQL("Delete from ExerciseType")
//        databaseInterface.writableDatabase.execSQL("Delete from ExerciseSet")
    }

    fun newWorkoutClick(view: View) {
        val intent = Intent(applicationContext, WorkoutScreen::class.java)
        intent.putExtra("workoutID", databaseInterface.getNextWorkoutID())
        startActivity(intent)
    }

    fun workoutHistoryClick(view: View) {
        val intent = Intent(applicationContext, HistoryScreen::class.java)
        startActivity(intent)
    }
}