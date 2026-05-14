package com.unotangozero.app.notifications

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class TaskReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParameters: WorkerParameters
) : CoroutineWorker(appContext, workerParameters) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "Tarefa pendente"
        val description = inputData.getString(KEY_DESCRIPTION) ?: "Você tem uma tarefa programada."
        val notificationId = inputData.getInt(KEY_NOTIFICATION_ID, title.hashCode())

        val intent = Intent(applicationContext, TaskReminderReceiver::class.java).apply {
            putExtra(TaskReminderReceiver.EXTRA_TITLE, title)
            putExtra(TaskReminderReceiver.EXTRA_DESCRIPTION, description)
            putExtra(TaskReminderReceiver.EXTRA_NOTIFICATION_ID, notificationId)
        }
        TaskReminderReceiver().onReceive(applicationContext, intent)
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "key_title"
        const val KEY_DESCRIPTION = "key_description"
        const val KEY_NOTIFICATION_ID = "key_notification_id"
    }
}
