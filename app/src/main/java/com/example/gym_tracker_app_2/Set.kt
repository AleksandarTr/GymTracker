package com.example.gym_tracker_app_2

import java.time.LocalTime

data class Set(val id: Long) {
    var count: Int = 0
    var weight: Float = 0f
    var unit: Unit = Unit.getUnit(0)
    val timeStamp = LocalTime.now()
}