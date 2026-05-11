package com.unotangozero.app.presentation.kanban

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.tasks.TaskKanbanColumn
import com.unotangozero.app.data.tasks.TaskKanbanRepository
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
import javax.inject.Inject

data class KanbanColumnUiState(
    val column: TaskKanbanColumn,
    val tasks: List<Task>
)

@HiltViewModel
class KanbanViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val kanbanRepository: TaskKanbanRepository
) : ViewModel() {
    val columns: StateFlow<List<KanbanColumnUiState>> = combine(
        taskRepository.observeAll(),
        kanbanRepository.statuses
    ) { tasks, statuses ->
        val visibleTasks = tasks.sortedBy { it.dueDate }
        TaskKanbanColumn.entries.map { column ->
            KanbanColumnUiState(
                column = column,
                tasks = visibleTasks.filter { task -> resolveColumn(task, statuses[task.id]) == column }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun moveTask(task: Task, targetColumn: TaskKanbanColumn) {
        viewModelScope.launch {
            kanbanRepository.setColumn(task.id, targetColumn)
                .onSuccess {
                    if (targetColumn == TaskKanbanColumn.DONE && !task.isCompleted) {
                        taskRepository.setCompleted(task.id, true)
                    }
                    if (targetColumn != TaskKanbanColumn.DONE && task.isCompleted) {
                        taskRepository.setCompleted(task.id, false)
                    }
                }
                .onFailure { _message.value = it.message ?: "Não foi possível mover a tarefa." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun resolveColumn(task: Task, storedColumn: TaskKanbanColumn?): TaskKanbanColumn {
        if (task.isCompleted) return TaskKanbanColumn.DONE
        return storedColumn ?: TaskKanbanColumn.TODO
    }
}
