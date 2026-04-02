package com.example.gym_tracker_app_2

import android.content.Context
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.example.gym_tracker_app_2.databinding.ExerciseLayoutBinding
import com.google.android.material.tabs.TabLayout
import kotlin.collections.addAll
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class ExerciseFragment : Fragment() {
    private var _binding: ExerciseLayoutBinding? = null
    private val binding get() = _binding!!
    private var name = ""

    private lateinit var exerciseNameField : AutoCompleteTextView
    private lateinit var prDisplay : TextView
    private lateinit var addSetButton : Button
    private lateinit var removeSetButton : Button
    private var exerciseSets : RecyclerView? = null
    private val sets : ArrayList<Set> = ArrayList()
    private val db: DatabaseInterface? = DatabaseInterface.getInstance()
    private val position by lazy {
        arguments?.getInt("position")
    }

    private val id by lazy {
        arguments?.getInt("id")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("name", name)
        outState.putParcelableArrayList("sets", sets)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            println("Restoring data")
            name = savedInstanceState.getString("name")!!
            val restoredSets = savedInstanceState.getParcelableArrayList("sets", Set::class.java)!!
            sets.addAll(restoredSets)
            println("Sets: " + restoredSets.size)
        }
    }

    private fun initialize() {
        if(id == null) arguments?.putInt("id", db?.getNextExerciseID() ?: 0)
        name = db?.getExerciseName(id!!) ?: ""
        if(db != null) sets.addAll(db.getExerciseSets(id!!))
    }

    companion object {
        fun newInstance(position: Int): ExerciseFragment {
            val fragment = ExerciseFragment()
            val args = Bundle()
            args.putInt("position", position)
            fragment.arguments = args
            fragment.initialize()
            return fragment
        }

        fun newInstance(position: Int, id: Int): ExerciseFragment {
            val fragment = ExerciseFragment()
            val args = Bundle()
            args.putInt("id", id)
            args.putInt("position", position)
            fragment.arguments = args
            fragment.initialize()
            return fragment
        }
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
                val exerciseID = DatabaseInterface.getInstance(requireContext()).getExerciseTypeID(name, false)
                if(exerciseID != null) {
                    val unitType = DatabaseInterface.getInstance(requireContext()).getExerciseUnitType(exerciseID)
                    (exerciseSets?.adapter as ExerciseDisplayAdapter).updateUnits(unitType)

                    val exercisePRID = DatabaseInterface.getInstance(requireContext()).getExercisePR(exerciseID, UnitManager.getUnit(0))
                    val lastExerciseID = DatabaseInterface.getInstance(requireContext()).getLastExercise(exerciseID)
                    if(exercisePRID == null || lastExerciseID == null) return

                    val exercisePR = DatabaseInterface.getInstance(requireContext()).getExerciseSets(exercisePRID)
                    val lastExercise = DatabaseInterface.getInstance(requireContext()).getExerciseSets(lastExerciseID)

                    var prDisplayText = "Last: "
                    for(set in lastExercise) prDisplayText +=
                        "${set.count}x${(set.unit.castTo(set.weight, UnitManager.getPreferredUnit(set.unit.type)) * 100f).roundToInt() / 100f}${UnitManager.getPreferredUnit(set.unit.type).name} "
                    prDisplayText += "\nPR: "
                    for(set in exercisePR) prDisplayText +=
                        "${set.count}x${(set.unit.castTo(set.weight, UnitManager.getPreferredUnit(set.unit.type)) * 100f).roundToInt() / 100f}${UnitManager.getPreferredUnit(set.unit.type).name} "

                    prDisplay.text = prDisplayText
                }
                else {
                    (exerciseSets?.adapter as ExerciseDisplayAdapter).updateUnits("")
                    prDisplay.text = ""
                }
            }

            override fun afterTextChanged(p0: Editable?) {
                if(container is ViewPager) {
                    val constraintLayout = container.parent as ConstraintLayout
                    val tabLayout = constraintLayout.findViewById<TabLayout>(R.id.exerciseTab)
                    tabLayout.getTabAt(position!!)?.setText(tabTitle)
                }
            }
        })
        if(this.context != null) {
            val exerciseAdapter: ArrayAdapter<String> = ArrayAdapter(this.requireContext(),
                android.R.layout.simple_spinner_item,
                DatabaseInterface.getInstance(requireContext()).getExerciseTypes())
            exerciseNameField.setAdapter(exerciseAdapter)
        }

        exerciseSets?.adapter = context?.let {
            if(exerciseSets != null) ExerciseDisplayAdapter(sets, requireContext(), exerciseSets!!)
            else null
        }
        exerciseSets?.layoutManager = LinearLayoutManager(context)

        addSetButton.setOnClickListener {
            (exerciseSets?.adapter as ExerciseDisplayAdapter?)?.addSet()
            removeSetButton.isEnabled = true
        }

        removeSetButton.setOnClickListener{
            (exerciseSets?.adapter as ExerciseDisplayAdapter?)?.removeSet()
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
        DatabaseInterface.getInstance(requireContext()).updateExercise(id!!, name, workoutID)
        for(set in sets) DatabaseInterface.getInstance(requireContext()).updateSet(set.id, set.count, set.weight, id!!, set.unit, set.warmup)
        (exerciseSets?.adapter as ExerciseDisplayAdapter?)?.save()
    }

    fun getExerciseId(): Int {
        return id!!
    }

    fun removeSets() {
        for (i in 0 until sets.size) (exerciseSets?.adapter as ExerciseDisplayAdapter).removeSet()
    }
}