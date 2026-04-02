package com.example.gym_tracker_app_2

import java.time.LocalTime
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Set(val id: Long) : Parcelable {
    var count: Int = 0
    var weight: Float = 0f
    var unit: Unit = UnitManager.getUnit(0)
    var warmup: Boolean = false
    val timeStamp = LocalTime.now()
}