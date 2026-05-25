package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class DeadlineReceiver : BroadcastReceiver() {
    private val TAG = "DeadlineReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", 0)
        val title = intent.getStringExtra("taskTitle") ?: "Lembrete de Prazo"
        val subject = intent.getStringExtra("taskSubject") ?: ""
        val type = intent.getStringExtra("taskType") ?: "OUTRO"
        val desc = intent.getStringExtra("taskDesc") ?: ""

        Log.d(TAG, "Notification trigger received: Task ID = $taskId, Title = $title, Type = $type")

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        val channelId = "eduagenda_deadlines"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Prazos de Entrega e Provas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações para prazos de entrega de trabalhos e provas dos estudantes"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Action when the user taps on the notification: Open MainActivity
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val clickPendingIntent = PendingIntent.getActivity(
            context,
            taskId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Choose appropriate icon representation & prefix in Portuguese
        val categoryLabel = when (type.uppercase()) {
            "PROVA" -> "🚨 Prova"
            "TRABALHO" -> "📝 Entrega de Trabalho"
            "ESTUDO" -> "📚 Sessão de Estudos"
            else -> "📅 Lembrete de Tarefa"
        }

        val formattedTitle = "$categoryLabel: $title"
        val formattedText = if (subject.isNotBlank()) "Matéria: $subject | $desc" else desc

        val notification = NotificationCompat.Builder(context, channelId)
            // Using standard warning/reminder system icon for perfect layout compatibility
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(formattedTitle)
            .setContentText(formattedText)
            .setStyle(NotificationCompat.BigTextStyle().bigText("$formattedText\n\nNão se esqueça do seu prazo! Estude e prepare-se."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(clickPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }
}
