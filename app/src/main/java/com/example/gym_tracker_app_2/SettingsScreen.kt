package com.example.gym_tracker_app_2

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.example.gym_tracker_app_2.databinding.SettingsLayoutBinding
import java.io.File

class SettingsScreen: ComponentActivity() {
    private lateinit var binding: SettingsLayoutBinding
    private val preferredUnitSpinners: HashMap<String, Spinner> = HashMap()

    companion object {
        private val preferredUnits: HashMap<String, Unit> = HashMap()

        fun getPreferredUnit(type: String) : Unit {
            return preferredUnits[type]!!
        }

        init {
            val directory = File(HomeScreen.appDir)
            val settingsFile = File(directory, "settings.config")
            if(settingsFile.exists()) {
                var foundUnits = false
                settingsFile.forEachLine {
                    if(it == "#UNITS") {
                        foundUnits = true
                        return@forEachLine
                    }
                    if (!foundUnits) return@forEachLine
                    if (it == "#!UNITS") {
                        foundUnits = false
                        return@forEachLine
                    }

                    val setting = it.split("=")
                    preferredUnits[setting[0]] = Unit.getUnit(setting[1])
                }
            }

            val unitTypes = HashSet<String>()
            for(i in 0 until Unit.getUnitCount()) unitTypes.add(Unit.getUnit(i).type)

            if(preferredUnits.size != unitTypes.size) {
                if(!preferredUnits.containsKey("weight")) preferredUnits["weight"] = Unit.getUnit("kg")
                if(!preferredUnits.containsKey("time")) preferredUnits["time"] = Unit.getUnit("s")
                if(!preferredUnits.containsKey("rep")) preferredUnits["rep"] = Unit.getUnit("rep")

                if(!settingsFile.exists()) {
                    directory.mkdirs()
                    settingsFile.createNewFile()
                }
                settingsFile.appendText("\n#UNITS")
                for((type, unit) in preferredUnits) settingsFile.appendText("\n$type=${unit.name}")
                settingsFile.appendText("\n#!UNITS")
            }
        }
    }

    fun save(view: View) {
        for((type, unit) in preferredUnitSpinners)
            preferredUnits[type] = Unit.getUnit(unit.selectedItem.toString())

        val directory = File(applicationInfo.dataDir)
        val settingsFile = File(directory, "settings.config")
        settingsFile.writeText("")

        settingsFile.appendText("\n#UNITS")
        for((type, unit) in preferredUnits)
            settingsFile.appendText("\n$type=${unit.name}")
        settingsFile.appendText("\n#!UNITS")
    }

    private fun addUnitType(type: String, unitRow: View?, units: ArrayList<String>) {
        if(unitRow != null) {
            val unitType = unitRow.findViewById<TextView>(R.id.unitType)
            val selectedUnit = unitRow.findViewById<Spinner>(R.id.SelectedUnit)

            val localizedType = when(type) {
                "weight" -> getString(R.string.weight)
                "time" -> getString(R.string.time)
                "rep" -> getString(R.string.rep)
                else -> type
            }
            unitType.text = localizedType

            val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, units.toArray())
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            selectedUnit.adapter = arrayAdapter
            selectedUnit.setSelection(units.indexOf(preferredUnits[type]!!.name))

            preferredUnitSpinners[type] = selectedUnit
            units.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var type = ""
        var unitRow: View? = null
        val units: ArrayList<String> = ArrayList()

        for(i in 0 until Unit.getUnitCount())  {
            val unit = Unit.getUnit(i)
            if(type != unit.type) {
                addUnitType(type, unitRow, units)
                type = unit.type
                unitRow = layoutInflater.inflate(R.layout.preferred_unit_layout, null)
                binding.preferredUnits.addView(unitRow)
            }

            units.add(unit.name)
        }

        if(units.isNotEmpty()) addUnitType(type, unitRow, units)
    }
}