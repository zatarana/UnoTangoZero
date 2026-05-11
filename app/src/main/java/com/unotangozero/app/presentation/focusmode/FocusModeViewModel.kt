package com.unotangozero.app.presentation.focusmode

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.tasks.TaskDurationRepository
import com.unotangozero.app.data.tasks.TaskTagRepository
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class FocusDateFilter(val displayName: String) {
    TODAY("Hoje"),
    TOMORROW("Amanhã"),
    THIS_WEEK("Esta semana"),
    OVERDUE("Atrasadas"),
    ALL("Todas")
}

data class FocusModeFilters(
    val selectedTag: String? = null,
    val selectedPriority: Priority? = null,
    val dateFilter: FocusDateFilter = FocusDateFilter.TODAY
)

data class FocusModeUiState(
    val tasks: List<Task> = emptyList(),
    val allTags: List<String> = emptyList(),
    val tagsByTask: Map<String, List<String>> = emptyMap(),
    val durationsByTask: Map<String, Int> = emptyMap(),
    val filters: FocusModeFilters = FocusModeFilters()
)

@HiltViewModel
class FocusModeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    taskTagRepository: TaskTagRepository,
    taskDurationRepository: TaskDurationRepository
) : ViewModel() {
    private val _selectedTag = MutableStateFlow<String?>(null)
    val selectedTag: StateFlow<String?> = _selectedTag.asStateFlow()

    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    val selectedPriority: StateFlow<Priority?> = _selectedPriority.asStateFlow()

    private val _dateFilter = MutableStateFlow(FocusDateFilter.TODAY)
    val dateFilter: StateFlow<FocusDateFilter> = _dateFilter.asStateFlow()

    private val filters = combine(_selectedTag, _selectedPriority, _dateFilter) { tag, priority, dateFilter ->
        FocusModeFilters(selectedTag = tag, selectedPriority = priority, dateFilter = dateFilter)
    }

    val uiState: StateFlow<FocusModeUiState> = combine(
        taskRepository.observeAll(),
        taskTagRepository.tags,
        taskDurationRepository.durations,
        filters
    ) { tasks, tagsByTask, durationsByTask, filters ->
        val today = LocalDate.now()
        val filteredTasks = tasks
            .asSequence()
            .filter { !it.isCompleted }
            .filter { task -> matchesDate(task, filters.dateFilter, today) }
            .filter { task -> filters.selectedPriority == null || task.priority == filters.selectedPriority }
            .filter { task -> filters.selectedTag == null || tagsByTask[task.id].orEmpty().contains(filters.selectedTag) }
            .sortedWith(compareBy<Task> { it.dueDate }.thenByDescending { it.priority.level })
            .toList()

        FocusModeUiState(
            tasks = filteredTasks,
            allTags = tagsByTask.values.flatten().distinct().sorted(),
            tagsByTask = tagsByTask,
            durationsByTask = durationsByTask,
            filters = filters
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FocusModeUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTagFilterChange(tag: String?) {
        _selectedTag.value = tag
    }

    fun onPriorityFilterChange(priority: Priority?) {
        _selectedPriority.value = priority
    }

    fun onDateFilterChange(filter: FocusDateFilter) {
        _dateFilter.value = filter
    }

    fun clearFilters() {
        _selectedTag.value = null
        _selectedPriority.value = null
        _dateFilter.value = FocusDateFilter.TODAY
    }

    fun completeTask(task: Task) {
        viewModelScope.launch {
            taskRepository.setCompleted(task.id, true)
                .onSuccess { _message.value = "Tarefa concluída." }
                .onFailure { _message.value = it.message ?: "Não foi possível concluir a tarefa." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun matchesDate(task: Task, filter: FocusDateFilter, today: LocalDate): Boolean {
        return when (filter) {
            FocusDateFilter.TODAY -> task.dueDate == today
            FocusDateFilter.TOMORROW -> task.dueDate == today.plusDays(1)
            FocusDateFilter.THIS_WEEK -> !task.dueDate.isBefore(today) && !task.dueDate.isAfter(today.plusDays(6))
            FocusDateFilter.OVERDUE -> task.dueDate.isBefore(today)
            FocusDateFilter.ALL -> true
        }
    }
}
