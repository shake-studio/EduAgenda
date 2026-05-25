package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.Task

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleAlarm(context: Context, task: Task) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        // Calculate the exact alarm trigger time
        val triggerTime = task.dateTime - (task.notifyMinutesBefore * 60 * 1000L)
        if (triggerTime <= System.currentTimeMillis()) {
            Log.d(TAG, "Task '${task.title}' trigger time is in the past. Skipping alarm.")
            return // Task deadline has already passed (or warning time already passed)
        }
        
        val intent = Intent(context, DeadlineReceiver::class.java).apply {
            putExtra("taskId", task.id)
            putExtra("taskTitle", task.title)
            putExtra("taskSubject", task.subject)
            putExtra("taskType", task.type)
            putExtra("taskDesc", task.description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id, // Unique request code per task
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        try {
            // Use setAndAllowWhileIdle for reliable triggers even when in Doze mode
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
            Log.d(TAG, "Alarm set for task '${task.title}' at interval: $triggerTime")
        } catch (e: SecurityException) {
            // Under extremely specific modern sandbox rules, this try-catch guarantees zero execution crashes
            try {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to schedule alarm", ex)
            }
        }
    }

    fun cancelAlarm(context: Context, taskId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DeadlineReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Alarm canceled for task ID: $taskId")
    }
}
