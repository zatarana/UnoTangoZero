package com.unotangozero.app.data.repositories

import com.unotangozero.app.data.db.dao.ReminderDao
import com.unotangozero.app.data.db.dao.SubTaskDao
import com.unotangozero.app.data.db.dao.TaskDao
import com.unotangozero.app.data.mappers.toDomain
import com.unotangozero.app.data.mappers.toEntity
import com.unotangozero.app.data.mappers.toEpochMillis
import com.unotangozero.app.domain.models.Reminder
import com.unotangozero.app.domain.models.SubTask
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.ReminderRepository
import com.unotangozero.app.domain.repositories.SubTaskRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val reminderDao: ReminderDao
) : TaskRepository {

    override suspend fun save(task: Task): Result<Unit> = runCatching {
        taskDao.insert(task.toEntity())
        task.subtasks.forEach { subTaskDao.insert(it.toEntity()) }
        task.reminders.forEach { reminderDao.insert(it.toEntity()) }
    }

    override suspend fun delete(taskId: String): Result<Unit> = runCatching {
        taskDao.deleteById(taskId)
    }

    override suspend fun setCompleted(taskId: String, completed: Boolean): Result<Unit> = runCatching {
        taskDao.updateCompleted(taskId, completed)
    }

    override fun observeById(taskId: String): Flow<Task?> {
        return combine(
            taskDao.observeById(taskId),
            subTaskDao.observeByTaskId(taskId),
            reminderDao.observeByTaskId(taskId)
        ) { taskEntity, subTaskEntities, reminderEntities ->
            taskEntity?.toDomain(
                subtasks = subTaskEntities.map { it.toDomain() },
                reminders = reminderEntities.map { it.toDomain() }
            )
        }
    }

    override fun observeToday(): Flow<List<Task>> = observeByDateRange(LocalDate.now(), LocalDate.now())

    override fun observeByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>> {
        val start = startDate.toEpochMillis()
        val end = endDate.plusDays(1).toEpochMillis() - 1
        return taskDao.observeByDateRange(start, end).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observePending(): Flow<List<Task>> {
        return taskDao.observePending().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeAll(): Flow<List<Task>> {
        return taskDao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }
}

class SubTaskRepositoryImpl @Inject constructor(
    private val subTaskDao: SubTaskDao
) : SubTaskRepository {
    override suspend fun save(subTask: SubTask): Result<Unit> = runCatching {
        subTaskDao.insert(subTask.toEntity())
    }

    override suspend fun delete(subTaskId: String): Result<Unit> = runCatching {
        subTaskDao.deleteById(subTaskId)
    }

    override fun observeByTaskId(taskId: String): Flow<List<SubTask>> {
        return subTaskDao.observeByTaskId(taskId).map { entities -> entities.map { it.toDomain() } }
    }
}

class ReminderRepositoryImpl @Inject constructor(
    private val reminderDao: ReminderDao
) : ReminderRepository {
    override suspend fun save(reminder: Reminder): Result<Unit> = runCatching {
        reminderDao.insert(reminder.toEntity())
    }

    override suspend fun delete(reminderId: String): Result<Unit> = runCatching {
        reminderDao.deleteById(reminderId)
    }

    override suspend fun deactivate(reminderId: String): Result<Unit> = runCatching {
        reminderDao.deactivate(reminderId)
    }

    override fun observeActive(): Flow<List<Reminder>> {
        return reminderDao.observeActive().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeByTaskId(taskId: String): Flow<List<Reminder>> {
        return reminderDao.observeByTaskId(taskId).map { entities -> entities.map { it.toDomain() } }
    }
}
