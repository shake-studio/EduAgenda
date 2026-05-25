package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val classScheduleDao: ClassScheduleDao
) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val allSchedules: Flow<List<ClassSchedule>> = classScheduleDao.getAllSchedules()

    suspend fun getTaskById(id: Int): Task? {
        return taskDao.getTaskById(id)
    }

    suspend fun insert(task: Task): Long {
        return taskDao.insertTask(task)
    }

    suspend fun delete(task: Task) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteById(id: Int) {
        taskDao.deleteById(id)
    }

    suspend fun insertSchedule(schedule: ClassSchedule): Long {
        return classScheduleDao.insertSchedule(schedule)
    }

    suspend fun deleteSchedule(schedule: ClassSchedule) {
        classScheduleDao.deleteSchedule(schedule)
    }

    suspend fun deleteScheduleById(id: Int) {
        classScheduleDao.deleteById(id)
    }
}
