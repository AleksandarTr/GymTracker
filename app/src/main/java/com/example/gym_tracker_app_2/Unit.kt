package com.example.gym_tracker_app_2

enum class Unit(val type: Type) {
    Kg(Type.WEIGHT),
    Lbs(Type.WEIGHT);

    private var conversionMap: Map<Unit, Float> = mapOf()

    companion object {
        val unitToPosition = mapOf(
            Kg to 0,
            Lbs to 1
        )
        val positionToUnit = unitToPosition.map {(k, v) -> v to k}.toMap()

        init {
            Kg.conversionMap = mapOf(Kg to 1.0f, Lbs to 2.204622f)
            Lbs.conversionMap = mapOf(Kg to 0.4535923f, Lbs to 1.0f)
        }
    }

    enum class Type {
        WEIGHT
    }

    fun castTo(value: Float, unit: Unit): Float {
        if (unit.type != this.type) throw IncompatibleClassChangeError("Incompatible unit types")

        conversionMap[unit]?.let { return value * it }
        throw IncompatibleClassChangeError("No known conversion between these units")
    }
}