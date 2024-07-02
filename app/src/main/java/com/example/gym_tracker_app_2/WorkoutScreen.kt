package com.example.gym_tracker_app_2

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import com.example.gym_tracker_app_2.ui.main.SectionsPagerAdapter
import com.example.gym_tracker_app_2.databinding.WorkoutLayoutBinding

class WorkoutScreen : AppCompatActivity() {

    private lateinit var binding: WorkoutLayoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = WorkoutLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = binding.exerciseDisplay
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = binding.exerciseTab
        tabs.setupWithViewPager(viewPager)
    }
}