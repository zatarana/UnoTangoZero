package com.unotangozero.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.unotangozero.app.data.settings.AppSettingsRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {
    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var settingsRepository: AppSettingsRepository
    @Inject lateinit var reminderScheduler: TaskReminderScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val shouldReschedule = intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED
        if (!shouldReschedule) return

        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                val settings = settingsRepository.settings.first()
                if (!settings.notificationsEnabled) return@launch

                val today = LocalDate.now()
                taskRepository.observeAll()
                    .first()
                    .filter { task -> !task.isCompleted && !task.dueDate.isBefore(today) }
                    .forEach { task ->
                        reminderScheduler.schedule(
                            task = task,
                            reminderDateTime = task.dueDate.atTime(
                                settings.defaultReminderHour.coerceIn(0, 23),
                                settings.defaultReminderMinute.coerceIn(0, 59)
                            )
                        )
                    }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
