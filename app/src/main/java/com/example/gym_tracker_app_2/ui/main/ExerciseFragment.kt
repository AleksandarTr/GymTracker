package com.example.gym_tracker_app_2.ui.main

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.gym_tracker_app_2.R
import com.example.gym_tracker_app_2.databinding.ExerciseLayoutBinding
import com.google.android.material.tabs.TabLayout

class ExerciseFragment(private val position: Int) : Fragment() {
    private var _binding: ExerciseLayoutBinding? = null
    private val binding get() = _binding!!
    private var name = ""

    private var exerciseNameField : EditText? = null
    private var addSetButton : Button? = null
    private var removeSetButton : Button? = null
    private var parent : ViewPager? = null

    val tabTitle : String
        get() {
            val nameParts = name.split(' ');
            var result = "";
            for(part in nameParts) if(part.isNotEmpty()) result += part[0].uppercase()
            return result
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = ExerciseLayoutBinding.inflate(inflater, container, false)
        val root = binding.root

        exerciseNameField = root.findViewById(R.id.exerciseNameField)
        addSetButton = root.findViewById(R.id.addSetButton)
        removeSetButton = root.findViewById(R.id.removeSetButton)

        exerciseNameField?.setText(name)
        exerciseNameField?.addTextChangedListener(object : TextWatcher {
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

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}