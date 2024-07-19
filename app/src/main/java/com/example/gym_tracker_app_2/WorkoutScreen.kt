package com.example.gym_tracker_app_2

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.gym_tracker_app_2.ui.main.WorkoutDisplayAdapter
import com.example.gym_tracker_app_2.databinding.WorkoutLayoutBinding

class WorkoutScreen : AppCompatActivity() {

    private lateinit var binding: WorkoutLayoutBinding
    private lateinit var sectionsPagerAdapter: WorkoutDisplayAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        HomeScreen.databaseInterface.writableDatabase.beginTransaction()

        binding = WorkoutLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = WorkoutDisplayAdapter(supportFragmentManager, intent.getIntExtra("workoutID", -1))
        val viewPager: ViewPager = binding.exerciseDisplay
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.exerciseTab
        tabs.setupWithViewPager(viewPager)

        val removeExerciseButton : Button = binding.removeExercise
        removeExerciseButton.setOnClickListener {
            sectionsPagerAdapter.removeExercise()
            if(sectionsPagerAdapter.count == 0) removeExerciseButton.isEnabled = false
        }
        removeExerciseButton.isEnabled = false

        val addExerciseButton : Button = binding.addExercise
        addExerciseButton.setOnClickListener {
            sectionsPagerAdapter.addExercise()
            removeExerciseButton.isEnabled = true
        }
    }

    fun saveWorkout(view: View) {
        val db = HomeScreen.databaseInterface.writableDatabase
        db.setTransactionSuccessful()
        db.endTransaction()
        db.beginTransaction()

        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.workout_saved)
        dialog.setPositiveButton("Ok") { _, _ ->}
        dialog.create().show()
    }

    override fun onDestroy() {
        super.onDestroy()
        HomeScreen.databaseInterface.writableDatabase.endTransaction()
    }
}