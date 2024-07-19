package com.example.gym_tracker_app_2.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.gym_tracker_app_2.HomeScreen
import com.example.gym_tracker_app_2.R
import com.example.gym_tracker_app_2.Set
import com.example.gym_tracker_app_2.databinding.ExerciseLayoutBinding
import com.google.android.material.tabs.TabLayout

class ExerciseFragment(private val position: Int) : Fragment() {
    private var _binding: ExerciseLayoutBinding? = null
    private val binding get() = _binding!!
    private var name = ""

    private lateinit var exerciseNameField : EditText
    private lateinit var addSetButton : Button
    private lateinit var removeSetButton : Button
    private lateinit var exerciseSets : ListView
    private val sets : ArrayList<Set> = ArrayList()
    private var id : Int = HomeScreen.databaseInterface.getNextExerciseID()

    constructor(position: Int, id: Int) : this(position) {
        this.id = id
        this.name = HomeScreen.databaseInterface.getExerciseName(id) ?: return
        sets.addAll(HomeScreen.databaseInterface.getExerciseSets(id))
    }

    val tabTitle : String
        get() {
            val nameParts = name.split(' ')
            var result = ""
            for(part in nameParts) if(part.isNotEmpty()) result += part[0].uppercase()
            return result
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ExerciseLayoutBinding.inflate(inflater, container, false)
        val root = binding.root

        exerciseNameField = binding.exerciseNameField
        exerciseSets = binding.exerciseSets
        addSetButton = binding.addSetButton
        removeSetButton = binding.removeSetButton
        removeSetButton.isEnabled = true

        exerciseNameField.setText(name)
        exerciseNameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newName: CharSequence?, p1: Int, p2: Int, p3: Int) {
                name = newName.toString()
            }

            override fun afterTextChanged(p0: Editable?) {
                if(container is ViewPager) {
                    val coordinatorLayout = container.parent as CoordinatorLayout
                    val tabLayout = coordinatorLayout.findViewById<TabLayout>(R.id.exerciseTab)
                    tabLayout.getTabAt(position)?.setText(tabTitle)
                }
            }
        })

        exerciseSets.adapter = context?.let { ExerciseDisplayAdapter(it, sets) }

        addSetButton.setOnClickListener {
            (exerciseSets.adapter as ExerciseDisplayAdapter?)?.addSet()
            removeSetButton.isEnabled = true
        }

        removeSetButton.setOnClickListener{
            (exerciseSets.adapter as ExerciseDisplayAdapter?)?.removeSet()
            if(sets.isEmpty()) removeSetButton.isEnabled = false
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}