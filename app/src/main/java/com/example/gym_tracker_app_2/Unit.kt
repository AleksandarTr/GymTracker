package com.example.gym_tracker_app_2

class Unit private constructor(val name: String, val type: String) {
    private val conversionMap = HashMap<Unit, Float>()

    companion object {
        private val units = ArrayList<Unit>()
        private val positions : HashMap<Unit, Int>

        init {
            val db = HomeScreen.databaseInterface.readableDatabase
            val cursor = db.rawQuery("Select name, type from Unit", null)
            while (cursor.moveToNext()) units.add(Unit(cursor.getString(0), cursor.getString(1)))
            cursor.close()

            for(i in 0 until units.size) {
                val conversionCursor = db.rawQuery("Select unit2, ratio from UnitConversion where unit1 = ?", arrayOf(i.toString()))
                while(conversionCursor.moveToNext()) units[i].conversionMap[units[conversionCursor.getInt(0)]] = conversionCursor.getFloat(1)
                conversionCursor.close()
            }

            positions = units.mapIndexed { i: Int, v: Unit -> v to i}.toMap() as HashMap<Unit, Int>
        }

        fun getPosition(unit: Unit): Int {
            return positions[unit]!!
        }

        fun getUnit(position: Int): Unit {
            return units[position]
        }

        fun getUnit(name: String): Unit {
            return units.find {unit -> unit.name == name}!!
        }

        fun getUnitCount(): Int {
            return units.size
        }
    }

    fun castTo(value: Float, unit: Unit): Float {
        if (unit.type != this.type) throw IncompatibleClassChangeError("Incompatible unit types")

        conversionMap[unit]?.let { return value * it }
        throw IncompatibleClassChangeError("No known conversion between these units")
    }
}