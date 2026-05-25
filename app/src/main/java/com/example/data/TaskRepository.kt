package com.example.data

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()

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
}
