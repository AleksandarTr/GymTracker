package com.example.gym_tracker_app_2

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.gym_tracker_app_2.databinding.SettingsLayoutBinding

class SettingsScreen: ComponentActivity() {
    private fun addUnitType(type: String, unitRow: View?, units: ArrayList<String>) {
        if(unitRow != null) {
            val unitType = unitRow.findViewById<TextView>(R.id.unitType)
            val selectedUnit = unitRow.findViewById<Spinner>(R.id.SelectedUnit)
            unitType.text = type

            val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units.toArray())
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectedUnit.adapter = arrayAdapter

            units.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var type = ""
        var unitRow: View? = null
        val units: ArrayList<String> = ArrayList()

        for(i in 0 until Unit.getUnitCount())  {
            val unit = Unit.convertPositionToUnit(i)
            if(type != unit.type) {
                addUnitType(type, unitRow, units)
                type = unit.type
                unitRow = layoutInflater.inflate(R.layout.preferred_unit_layout, binding.preferredUnits)
            }

            units.add(unit.name)
        }

        if(units.isNotEmpty()) addUnitType(type, unitRow, units)
    }
}