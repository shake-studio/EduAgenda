package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "class_schedules")
data class ClassSchedule(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val subject: String,
    val dayOfWeek: Int, // 1 (Segunda) to 7 (Domingo)
    val startTime: String, // "HH:mm"
    val endTime: String, // "HH:mm"
    val room: String = "",
    val teacher: String = "",
    val colorHex: String = "#8D4F38" // Default theme color
)
