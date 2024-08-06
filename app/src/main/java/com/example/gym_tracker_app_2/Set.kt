package com.example.gym_tracker_app_2

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.LocalTime

data class Set(val id: Long) {
    var count: Int = 0
    var weight: Float = 0f
    var unit: Byte = 0
    val timeStamp = LocalTime.now()
}