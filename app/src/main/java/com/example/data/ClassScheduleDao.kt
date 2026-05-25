package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassScheduleDao {
    @Query("SELECT * FROM class_schedules ORDER BY dayOfWeek ASC, startTime ASC")
    fun getAllSchedules(): Flow<List<ClassSchedule>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ClassSchedule): Long

    @Delete
    suspend fun deleteSchedule(schedule: ClassSchedule)

    @Query("DELETE FROM class_schedules WHERE id = :id")
    suspend fun deleteById(id: Int)
}
