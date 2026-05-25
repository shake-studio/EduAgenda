package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.Task
import com.example.data.ClassSchedule
import com.example.data.TaskRepository
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    
    val searchQuery = MutableStateFlow("")
    val selectedFilter = MutableStateFlow("TODAS") // "TODAS", "TRABALHOS", "PROVAS", "ESTUDOS", "PENDENTES", "CONCLUIDAS"

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao(), database.classScheduleDao())
    }

    // Reactive flow combining tasks flow from room with active search query and selected filter
    val tasksState: StateFlow<List<Task>> = combine(
        repository.allTasks,
        searchQuery,
        selectedFilter
    ) { tasks, query, filter ->
        tasks.filter { task ->
            val matchesQuery = task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true) ||
                    task.subject.contains(query, ignoreCase = true)
            
            val matchesFilter = when (filter) {
                "TODAS" -> true
                "TRABALHOS" -> task.type.uppercase() == "TRABALHO"
                "PROVAS" -> task.type.uppercase() == "PROVA"
                "ESTUDOS" -> task.type.uppercase() == "ESTUDO"
                "PENDENTES" -> !task.isCompleted
                "CONCLUIDAS" -> task.isCompleted
                else -> true
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive flow of weekly class schedules
    val schedulesState: StateFlow<List<ClassSchedule>> = repository.allSchedules
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(
        title: String,
        description: String,
        subject: String,
        dateTime: Long,
        type: String,
        notifyMinutesBefore: Int
    ) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                subject = subject,
                dateTime = dateTime,
                type = type.uppercase(),
                notifyMinutesBefore = notifyMinutesBefore,
                isCompleted = false
            )
            // Save to room, returning the auto-generated rowId/id
            val generatedId = repository.insert(task)
            
            // Re-schedule alarm using the active primary key rowId
            val scheduledTask = task.copy(id = generatedId.toInt())
            AlarmScheduler.scheduleAlarm(getApplication(), scheduledTask)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.insert(task)
            if (task.isCompleted) {
                AlarmScheduler.cancelAlarm(getApplication(), task.id)
            } else {
                // Reschedule to ensure dates or settings updates are pushed
                AlarmScheduler.scheduleAlarm(getApplication(), task)
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
            AlarmScheduler.cancelAlarm(getApplication(), task.id)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(isCompleted = !task.isCompleted)
        updateTask(updatedTask)
    }

    // Class Schedule Actions
    fun addSchedule(
        subject: String,
        dayOfWeek: Int,
        startTime: String,
        endTime: String,
        room: String,
        teacher: String,
        colorHex: String
    ) {
        viewModelScope.launch {
            val schedule = ClassSchedule(
                subject = subject,
                dayOfWeek = dayOfWeek,
                startTime = startTime,
                endTime = endTime,
                room = room,
                teacher = teacher,
                colorHex = colorHex
            )
            repository.insertSchedule(schedule)
        }
    }

    fun deleteSchedule(schedule: ClassSchedule) {
        viewModelScope.launch {
            repository.deleteSchedule(schedule)
        }
    }
}
