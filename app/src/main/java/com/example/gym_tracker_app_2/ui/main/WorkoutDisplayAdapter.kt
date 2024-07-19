package com.example.gym_tracker_app_2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WorkoutDisplayAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

    private val exerciseFragments = ArrayList<ExerciseFragment>()

    override fun getItem(position: Int): Fragment {
        return exerciseFragments[position]
    }

    override fun getPageTitle(position: Int): CharSequence {
        return exerciseFragments[position].tabTitle
    }

    override fun getItemPosition(item: Any): Int {
        return POSITION_NONE
    }

    override fun getCount(): Int {
        return exerciseFragments.size
    }

    fun addExercise() {
        exerciseFragments.add(ExerciseFragment(exerciseFragments.size))
        notifyDataSetChanged()
    }

    fun removeExercise() {
        exerciseFragments.removeLast()
        notifyDataSetChanged()
    }
}