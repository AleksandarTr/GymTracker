package com.example.gym_tracker_app_2

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.gym_tracker_app_2.ui.main.WorkoutDisplayAdapter
import com.example.gym_tracker_app_2.databinding.WorkoutLayoutBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class WorkoutScreen : AppCompatActivity() {

    private lateinit var binding: WorkoutLayoutBinding
    private lateinit var sectionsPagerAdapter: WorkoutDisplayAdapter

    @RequiresApi(Build.VERSION_CODES.O)
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

        val dateFiled : EditText = binding.dateField
        dateFiled.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString())
    }

    fun saveWorkout(view: View) {
        HomeScreen.databaseInterface.updateWorkout(intent.getIntExtra("workoutID", -1),
            binding.nameField.text.toString(), binding.dateField.text.toString())
        sectionsPagerAdapter.save()

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