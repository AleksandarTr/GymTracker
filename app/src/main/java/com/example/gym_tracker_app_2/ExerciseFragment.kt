package com.example.gym_tracker_app_2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.gym_tracker_app_2.databinding.ExerciseLayoutBinding
import com.google.android.material.tabs.TabLayout
import kotlin.math.roundToInt

class ExerciseFragment(private val position: Int) : Fragment() {
    private var _binding: ExerciseLayoutBinding? = null
    private val binding get() = _binding!!
    private var name = ""

    private lateinit var exerciseNameField : AutoCompleteTextView
    private lateinit var prDisplay : TextView
    private lateinit var addSetButton : Button
    private lateinit var removeSetButton : Button
    private lateinit var exerciseSets : RecyclerView
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
        prDisplay = binding.prDisplay
        exerciseSets = binding.exerciseSets
        addSetButton = binding.addSetButton
        removeSetButton = binding.removeSetButton
        removeSetButton.isEnabled = true

        exerciseNameField.setText(name)
        exerciseNameField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(newName: CharSequence?, p1: Int, p2: Int, p3: Int) {
                name = newName.toString()
                val exerciseID = HomeScreen.databaseInterface.getExerciseTypeID(name, false)
                if(exerciseID != null) {
                    val unitType = HomeScreen.databaseInterface.getExerciseUnitType(exerciseID)
                    (exerciseSets.adapter as ExerciseDisplayAdapter).updateUnits(unitType)

                    val exercisePRID = HomeScreen.databaseInterface.getExercisePR(exerciseID, Unit.getUnit(0))
                    val lastExerciseID = HomeScreen.databaseInterface.getLastExercise(exerciseID)
                    if(exercisePRID == null || lastExerciseID == null) return

                    val exercisePR = HomeScreen.databaseInterface.getExerciseSets(exercisePRID)
                    val lastExercise = HomeScreen.databaseInterface.getExerciseSets(lastExerciseID)

                    var prDisplayText = "Last: "
                    for(set in lastExercise) prDisplayText +=
                        "${set.count}x${(set.unit.castTo(set.weight, SettingsScreen.getPreferredUnit(set.unit.type)) * 100f).roundToInt() / 100f}${SettingsScreen.getPreferredUnit(set.unit.type).name} "
                    prDisplayText += "\nPR: "
                    for(set in exercisePR) prDisplayText +=
                        "${set.count}x${(set.unit.castTo(set.weight, SettingsScreen.getPreferredUnit(set.unit.type)) * 100f).roundToInt() / 100f}${SettingsScreen.getPreferredUnit(set.unit.type).name} "

                    prDisplay.text = prDisplayText
                }
                else (exerciseSets.adapter as ExerciseDisplayAdapter).updateUnits("")
            }

            override fun afterTextChanged(p0: Editable?) {
                if(container is ViewPager) {
                    val constraintLayout = container.parent as ConstraintLayout
                    val tabLayout = constraintLayout.findViewById<TabLayout>(R.id.exerciseTab)
                    tabLayout.getTabAt(position)?.setText(tabTitle)
                }
            }
        })
        if(this.context != null) {
            val exerciseAdapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(),
                android.R.layout.simple_spinner_item,
                HomeScreen.databaseInterface.getExerciseTypes())
            exerciseNameField.setAdapter(exerciseAdapter)
        }

        exerciseSets.adapter = context?.let { ExerciseDisplayAdapter(sets, requireContext(), exerciseSets) }
        exerciseSets.layoutManager = LinearLayoutManager(context)

        addSetButton.setOnClickListener {
            (exerciseSets.adapter as ExerciseDisplayAdapter?)?.addSet()
            removeSetButton.isEnabled = true
        }

        removeSetButton.setOnClickListener{
            (exerciseSets.adapter as ExerciseDisplayAdapter?)?.removeSet()
            if(sets.isEmpty()) removeSetButton.isEnabled = false
        }
        if(sets.isEmpty()) removeSetButton.isEnabled = false

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun save(workoutID: Int) {
        HomeScreen.databaseInterface.updateExercise(id, name, workoutID)
        for(set in sets) HomeScreen.databaseInterface.updateSet(set.id, set.count, set.weight, id, set.unit, set.warmup)
        (exerciseSets.adapter as ExerciseDisplayAdapter).save()
    }

    fun getExerciseId(): Int {
        return id
    }

    fun removeSets() {
        for (i in 0 until sets.size) (exerciseSets.adapter as ExerciseDisplayAdapter).removeSet()
    }
}