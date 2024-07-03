package com.example.gym_tracker_app_2.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class SectionsPagerAdapter(private val context: Context, fm: FragmentManager) :
    FragmentPagerAdapter(fm) {

        private val exerciseFragments = ArrayList<ExerciseFragment>()

    init {
        exerciseFragments.add(ExerciseFragment(0))
        exerciseFragments.add(ExerciseFragment(1))
    }

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment.
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
}