package com.example.gym_tracker_app_2

import android.content.Context
import java.io.File

class Unit constructor(val name: String, val type: String) {
    val conversionMap = HashMap<Unit, Float>()

    fun castTo(value: Float, unit: Unit): Float {
        if (unit.type != this.type) throw IncompatibleClassChangeError("Incompatible unit types")

        conversionMap[unit]?.let { return value * it }
        throw IncompatibleClassChangeError("No known conversion between these units")
    }
}

object UnitManager {
    private val units = ArrayList<Unit>()
    private lateinit var positions: Map<Unit, Int>
    val preferredUnits: HashMap<String, Unit> = HashMap()

    fun getPreferredUnit(type: String) : Unit {
        return preferredUnits[type]!!
    }

    // This is where you "inject" the dependency
    fun loadUnits(dbHelper: DatabaseInterface) {
        if (units.isNotEmpty()) return // Already loaded

        val db = dbHelper.readableDatabase

        // 1. Load Units
        val cursor = db.rawQuery("SELECT name, type FROM Unit", null)
        while (cursor.moveToNext()) {
            units.add(Unit(cursor.getString(0), cursor.getString(1)))
        }
        cursor.close()

        // 2. Load Conversions
        units.forEachIndexed { index, unit ->
            val convCursor = db.rawQuery(
                "SELECT unit2, ratio FROM UnitConversion WHERE unit1 = ?",
                arrayOf(index.toString())
            )
            while (convCursor.moveToNext()) {
                val targetUnit = units[convCursor.getInt(0)]
                unit.conversionMap[targetUnit] = convCursor.getFloat(1)
            }
            convCursor.close()
        }

        positions = units.mapIndexed { i, v -> v to i }.toMap()

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
                preferredUnits[setting[0]] = UnitManager.getUnit(setting[1]) as Unit
            }
        }

        val unitTypes = HashSet<String>()
        for(i in 0 until UnitManager.getUnitCount()) unitTypes.add(UnitManager.getUnit(i).type)

        if(preferredUnits.size != unitTypes.size) {
            if(!preferredUnits.containsKey("weight")) preferredUnits["weight"] = UnitManager.getUnit("kg") as Unit
            if(!preferredUnits.containsKey("time")) preferredUnits["time"] = UnitManager.getUnit("s") as Unit
            if(!preferredUnits.containsKey("rep")) preferredUnits["rep"] = UnitManager.getUnit("rep") as Unit

            if(!settingsFile.exists()) {
                directory.mkdirs()
                settingsFile.createNewFile()
            }
            settingsFile.appendText("\n#UNITS")
            for((type, unit) in preferredUnits) settingsFile.appendText("\n$type=${unit.name}")
            settingsFile.appendText("\n#!UNITS")
        }
    }

    fun getUnit(name: String) = units.find { it.name == name }
    fun getUnit(position: Int) = units[position]
    fun getPosition(unit: Unit): Int = positions[unit]!!
    fun getUnitCount(): Int = units.size
}