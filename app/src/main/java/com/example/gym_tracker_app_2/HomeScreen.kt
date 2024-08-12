package com.example.gym_tracker_app_2

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import com.example.gym_tracker_app_2.databinding.HomeScreenLayoutBinding

class HomeScreen : ComponentActivity() {
    companion object {
        lateinit var databaseInterface: DatabaseInterface
        lateinit var appDir: String
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = HomeScreenLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        databaseInterface = DatabaseInterface(applicationContext)
        appDir = applicationInfo.dataDir
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

    fun statsClick(view: View) {
        val intent = Intent(applicationContext, ExerciseScreen::class.java)
        startActivity(intent)
    }

    fun openSettings(view: View) {
        val intent = Intent(applicationContext, SettingsScreen::class.java)
        startActivity(intent)
    }
}