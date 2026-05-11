package com.unotangozero.app.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.TaskCategory
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository
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

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNewTaskTitleChange(value: String) {
        _newTaskTitle.value = value
    }

    fun createTodayTask() {
        val title = _newTaskTitle.value.trim()
        if (title.isBlank()) {
            _message.value = "Digite um título para a tarefa."
            return
        }

        viewModelScope.launch {
            val task = Task(
                title = title,
                dueDate = LocalDate.now(),
                category = TaskCategory.PERSONAL,
                priority = Priority.MEDIUM
            )

            taskRepository.save(task)
                .onSuccess {
                    _newTaskTitle.value = ""
                    _message.value = "Tarefa criada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar a tarefa."
                }
        }
    }

    fun toggleCompleted(task: Task) {
        viewModelScope.launch {
            taskRepository.setCompleted(task.id, !task.isCompleted)
                .onFailure {
                    _message.value = it.message ?: "Não foi possível atualizar a tarefa."
                }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            taskRepository.delete(task.id)
                .onSuccess { _message.value = "Tarefa excluída." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a tarefa." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
