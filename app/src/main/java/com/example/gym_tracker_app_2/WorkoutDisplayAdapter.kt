package com.example.gym_tracker_app_2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WorkoutDisplayAdapter(fm: FragmentManager, val workoutID: Int, val context: Context) :
    FragmentPagerAdapter(fm) {

    private val exerciseFragments = ArrayList<ExerciseFragment>()
    private val forDeletion = ArrayList<ExerciseFragment>()

    init {
        val exerciseIDs = DatabaseInterface.getInstance(context).getWorkoutExercises(workoutID)
        exerciseIDs.sort()
        for(id in exerciseIDs) exerciseFragments.add(ExerciseFragment.newInstance(exerciseFragments.size, id))
    }

    fun reloadExercises(count: Int) {
        for(i in exerciseFragments.size until count) exerciseFragments.add(ExerciseFragment.newInstance(exerciseFragments.size))
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
        println("Fragment count: " + exerciseFragments.size)
        return exerciseFragments.size
    }

    fun addExercise() {
        exerciseFragments.add(ExerciseFragment.newInstance(exerciseFragments.size))
        notifyDataSetChanged()
    }

    fun removeExercise() {
        val exercise = exerciseFragments.removeAt(exerciseFragments.lastIndex)
        exercise.removeSets()
        forDeletion.add(exercise)
        notifyDataSetChanged()
    }

    fun save() {
        for(exercise in exerciseFragments) exercise.save(workoutID)
        for(exercise in forDeletion) exercise.save(workoutID)
        for(exercise in forDeletion) DatabaseInterface.getInstance(context).deleteExercise(exercise.getExerciseId())
        forDeletion.clear()
    }
}