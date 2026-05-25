package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val description: String,
    val subject: String,
    val dateTime: Long, // timestamp in ms
    val type: String, // "TRABALHO" or "PROVA" or "ESTUDO" or "OUTRO"
    val isCompleted: Boolean = false,
    val notifyMinutesBefore: Int = 0 // 0 = exact time, 15 = 15m before, 60 = 1h before, 1440 = 1 day before
)
