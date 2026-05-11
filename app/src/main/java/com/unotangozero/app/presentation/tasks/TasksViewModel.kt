package com.unotangozero.app.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.settings.AppSettingsRepository
import com.unotangozero.app.data.tasks.TaskDurationRepository
import com.unotangozero.app.data.tasks.TaskTagRepository
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

data class TaskEditorUiState(
    val editingTask: Task? = null,
    val title: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val recurrenceType: RecurrenceType = RecurrenceType.NONE,
    val estimatedHoursText: String = "",
    val estimatedMinutesText: String = "",
    val tagsText: String = ""
) {
    val isEditing: Boolean = editingTask != null
}

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val settingsRepository: AppSettingsRepository,
    private val reminderScheduler: TaskReminderScheduler,
    private val taskDurationRepository: TaskDurationRepository,
    private val taskTagRepository: TaskTagRepository
) : ViewModel() {
    val tasks: StateFlow<List<Task>> = taskRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val taskDurations: StateFlow<Map<String, Int>> = taskDurationRepository.durations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    val taskTags: StateFlow<Map<String, List<String>>> = taskTagRepository.tags
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    private val _editorState = MutableStateFlow(TaskEditorUiState())
    val editorState: StateFlow<TaskEditorUiState> = _editorState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTitleChange(value: String) { _editorState.value = _editorState.value.copy(title = value) }
    fun onDueDatePreviousDay() { _editorState.value = _editorState.value.copy(dueDate = _editorState.value.dueDate.minusDays(1)) }
    fun onDueDateNextDay() { _editorState.value = _editorState.value.copy(dueDate = _editorState.value.dueDate.plusDays(1)) }
    fun onCategoryChange(category: TaskCategory) { _editorState.value = _editorState.value.copy(category = category) }
    fun onPriorityChange(priority: Priority) { _editorState.value = _editorState.value.copy(priority = priority) }
    fun onRecurrenceTypeChange(value: RecurrenceType) { _editorState.value = _editorState.value.copy(recurrenceType = value) }
    fun onTagsChange(value: String) { _editorState.value = _editorState.value.copy(tagsText = value) }

    fun onEstimatedHoursChange(value: String) {
        _editorState.value = _editorState.value.copy(estimatedHoursText = value.filter { it.isDigit() }.take(3))
    }

    fun onEstimatedMinutesChange(value: String) {
        _editorState.value = _editorState.value.copy(estimatedMinutesText = value.filter { it.isDigit() }.take(2))
    }

    fun startEditing(task: Task) {
        val duration = taskDurations.value[task.id] ?: task.estimatedDurationMinutes
        _editorState.value = TaskEditorUiState(
            editingTask = task,
            title = task.title,
            dueDate = task.dueDate,
            category = task.category,
            priority = task.priority,
            recurrenceType = task.recurrenceType ?: RecurrenceType.NONE,
            estimatedHoursText = if (duration > 0) (duration / 60).toString() else "",
            estimatedMinutesText = if (duration > 0) (duration % 60).toString() else "",
            tagsText = taskTags.value[task.id].orEmpty().joinToString(", ")
        )
    }

    fun cancelEditing() { _editorState.value = TaskEditorUiState() }

    fun saveTaskFromEditor() {
        val state = _editorState.value
        val title = state.title.trim()
        if (title.isBlank()) {
            _message.value = "Digite um título para a tarefa."
            return
        }

        val estimatedMinutes = parseEstimatedMinutes(state)
        val tags = parseTags(state.tagsText)

        viewModelScope.launch {
            val recurrenceType = state.recurrenceType
            val task = state.editingTask?.copy(
                title = title,
                dueDate = state.dueDate,
                category = state.category,
                priority = state.priority,
                isRecurring = recurrenceType != RecurrenceType.NONE,
                recurrenceType = recurrenceType.takeIf { it != RecurrenceType.NONE },
                estimatedDurationMinutes = estimatedMinutes,
                updatedAt = LocalDateTime.now()
            ) ?: Task(
                title = title,
                dueDate = state.dueDate,
                category = state.category,
                priority = state.priority,
                isRecurring = recurrenceType != RecurrenceType.NONE,
                recurrenceType = recurrenceType.takeIf { it != RecurrenceType.NONE },
                estimatedDurationMinutes = estimatedMinutes
            )

            taskRepository.save(task)
                .onSuccess {
                    taskDurationRepository.setDuration(task.id, estimatedMinutes)
                    taskTagRepository.setTags(task.id, tags)
                    reminderScheduler.cancel(task.id)
                    if (!task.isCompleted) scheduleReminderIfEnabled(task)
                    _editorState.value = TaskEditorUiState()
                    _message.value = if (state.isEditing) "Tarefa atualizada." else "Tarefa criada."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar a tarefa." }
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
                    taskDurationRepository.setDuration(task.id, 0)
                    taskTagRepository.clear(task.id)
                    reminderScheduler.cancel(task.id)
                    if (_editorState.value.editingTask?.id == task.id) cancelEditing()
                    _message.value = "Tarefa excluída."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a tarefa." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseEstimatedMinutes(state: TaskEditorUiState): Int {
        val hours = state.estimatedHoursText.toIntOrNull() ?: 0
        val minutes = (state.estimatedMinutesText.toIntOrNull() ?: 0).coerceIn(0, 59)
        return (hours * 60 + minutes).coerceAtLeast(0)
    }

    private fun parseTags(raw: String): List<String> {
        return raw.split(",")
            .map { it.trim().lowercase() }
            .filter { it.isNotBlank() }
            .distinct()
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

        val duration = taskDurations.value[task.id] ?: task.estimatedDurationMinutes
        val tags = taskTags.value[task.id].orEmpty()
        val nextTask = task.copy(
            id = java.util.UUID.randomUUID().toString(),
            dueDate = nextDate,
            isCompleted = false,
            estimatedDurationMinutes = duration,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            subtasks = emptyList(),
            reminders = emptyList()
        )

        taskRepository.save(nextTask).onSuccess {
            taskDurationRepository.setDuration(nextTask.id, duration)
            taskTagRepository.setTags(nextTask.id, tags)
            scheduleReminderIfEnabled(nextTask)
            _message.value = "Tarefa concluída. Próxima recorrência criada."
        }
    }
}
