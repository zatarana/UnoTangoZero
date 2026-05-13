package com.unotangozero.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.unotangozero.app.domain.models.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun schedule(task: Task, reminderDateTime: LocalDateTime) {
        val now = System.currentTimeMillis()
        val requestedTriggerAt = reminderDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val triggerAt = if (requestedTriggerAt <= now && task.dueDate == java.time.LocalDate.now()) {
            now + FALLBACK_DELAY_MILLIS
        } else {
            requestedTriggerAt
        }

        if (triggerAt <= now) return

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TITLE, task.title)
            putExtra(TaskReminderReceiver.EXTRA_DESCRIPTION, task.description ?: "Tarefa marcada para ${task.dueDate}.")
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, task.id.hashCode())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        if (!canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            return
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun canScheduleExactAlarms(): Boolean {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    fun cancel(taskId: String) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode(),
            Intent(context, TaskReminderReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)
    }

    private companion object {
        const val FALLBACK_DELAY_MILLIS = 2 * 60 * 1000L
    }
}
