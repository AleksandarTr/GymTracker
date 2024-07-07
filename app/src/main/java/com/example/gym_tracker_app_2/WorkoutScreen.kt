package com.example.gym_tracker_app_2

import android.os.Bundle
import android.widget.Button
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

        binding = WorkoutLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sectionsPagerAdapter = WorkoutDisplayAdapter(this, supportFragmentManager)
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
}