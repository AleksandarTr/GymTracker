package com.example.gym_tracker_app_2

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WorkoutDisplayAdapter(fm: FragmentManager, val workoutID: Int) :
    FragmentPagerAdapter(fm) {

    private val exerciseFragments = ArrayList<ExerciseFragment>()

    init {
        val exerciseIDs = HomeScreen.databaseInterface.getWorkoutExercises(workoutID)
        exerciseIDs.sort()
        for(id in exerciseIDs) exerciseFragments.add(ExerciseFragment(exerciseFragments.size, id))
    }

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

    fun save() {
        for(exercise in exerciseFragments) exercise.save(workoutID)
    }
}