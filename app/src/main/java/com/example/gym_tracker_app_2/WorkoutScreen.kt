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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val workout = HomeScreen.databaseInterface.getWorkout(intent.getIntExtra("workoutID", -1))

        val nameField : EditText = binding.nameField
        nameField.setText(workout?.name)

        val dateField : EditText = binding.dateField
        if(workout == null) dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString())
        else dateField.setText(workout.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")))
    }

    fun saveWorkout(view: View) {
        val db = HomeScreen.databaseInterface.writableDatabase
        db.beginTransaction()

        HomeScreen.databaseInterface.updateWorkout(intent.getIntExtra("workoutID", -1),
            binding.nameField.text.toString(), LocalDate.parse(binding.dateField.text.toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy")))
        sectionsPagerAdapter.save()

        db.setTransactionSuccessful()
        db.endTransaction()

        val dialog = AlertDialog.Builder(this)
        dialog.setMessage(R.string.workout_saved)
        dialog.setPositiveButton("Ok") { _, _ ->}
        dialog.create().show()
    }
}