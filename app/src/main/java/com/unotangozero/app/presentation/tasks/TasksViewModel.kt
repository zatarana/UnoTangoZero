package com.unotangozero.app.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.settings.AppSettingsRepository
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.enums.TaskCategory
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.TaskRepository
import com.unotangozero.app.notifications.TaskReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsRepository: AppSettingsRepository,
    private val reminderScheduler: TaskReminderScheduler
) : ViewModel() {
    val tasks: StateFlow<List<Task>> = taskRepository
        .observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _newTaskTitle = MutableStateFlow("")
    val newTaskTitle: StateFlow<String> = _newTaskTitle.asStateFlow()

    private val _selectedRecurrenceType = MutableStateFlow(RecurrenceType.NONE)
    val selectedRecurrenceType: StateFlow<RecurrenceType> = _selectedRecurrenceType.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNewTaskTitleChange(value: String) {
        _newTaskTitle.value = value
    }

    fun onRecurrenceTypeChange(value: RecurrenceType) {
        _selectedRecurrenceType.value = value
    }

    fun createTodayTask() {
        val title = _newTaskTitle.value.trim()
        if (title.isBlank()) {
            _message.value = "Digite um título para a tarefa."
            return
        }

        viewModelScope.launch {
            val recurrenceType = _selectedRecurrenceType.value
            val task = Task(
                title = title,
                dueDate = LocalDate.now(),
                category = TaskCategory.PERSONAL,
                priority = Priority.MEDIUM,
                isRecurring = recurrenceType != RecurrenceType.NONE,
                recurrenceType = recurrenceType.takeIf { it != RecurrenceType.NONE }
            )

            taskRepository.save(task)
                .onSuccess {
                    scheduleReminderIfEnabled(task)
                    _newTaskTitle.value = ""
                    _selectedRecurrenceType.value = RecurrenceType.NONE
                    _message.value = "Tarefa criada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar a tarefa."
                }
        }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            if (!task.isCompleted) {
                taskRepository.setCompleted(task.id, true)
                    .onSuccess {
                        reminderScheduler.cancel(task.id)
                        createNextRecurringTaskIfNeeded(task)
                    }
                    .onFailure { _message.value = it.message ?: "Não foi possível atualizar a tarefa." }
            } else {
                taskRepository.setCompleted(task.id, false)
                    .onSuccess { scheduleReminderIfEnabled(task.copy(isCompleted = false)) }
                    .onFailure { _message.value = it.message ?: "Não foi possível atualizar a tarefa." }
            }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task.id)
                .onSuccess {
                    reminderScheduler.cancel(task.id)
                    _message.value = "Tarefa excluída."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a tarefa." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun scheduleReminderIfEnabled(task: Task) {
        val settings = settingsRepository.settings.first()
        if (!settings.notificationsEnabled) return
        val reminderDateTime = task.dueDate.atTime(settings.defaultReminderHour, settings.defaultReminderMinute)
        reminderScheduler.schedule(task, reminderDateTime)
    }

    private suspend fun createNextRecurringTaskIfNeeded(task: Task) {
        val nextDate = when (task.recurrenceType) {
            RecurrenceType.DAILY -> task.dueDate.plusDays(1)
            RecurrenceType.WEEKLY -> task.dueDate.plusWeeks(1)
            RecurrenceType.MONTHLY -> task.dueDate.plusMonths(1)
            RecurrenceType.YEARLY -> task.dueDate.plusYears(1)
            else -> null
        } ?: return

        val endDate = task.recurrenceEndDate
        if (endDate != null && nextDate.isAfter(endDate)) return

        val nextTask = task.copy(
            id = java.util.UUID.randomUUID().toString(),
            dueDate = nextDate,
            isCompleted = false,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            subtasks = emptyList(),
            reminders = emptyList()
        )

        taskRepository.save(nextTask).onSuccess {
            scheduleReminderIfEnabled(nextTask)
            _message.value = "Tarefa concluída. Próxima recorrência criada."
        }
    }
}
