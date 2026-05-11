package com.unotangozero.app.presentation.projects

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.projects.ProjectRepository
import com.unotangozero.app.domain.models.Project
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
class ProjectsViewModel @Inject constructor(
    private val projectRepository: ProjectRepository
) : ViewModel() {
    val projects: StateFlow<List<Project>> = projectRepository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _deadline = MutableStateFlow<LocalDate?>(null)
    val deadline: StateFlow<LocalDate?> = _deadline.asStateFlow()

    private val _taskTitle = MutableStateFlow("")
    val taskTitle: StateFlow<String> = _taskTitle.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTitleChange(value: String) { _title.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }
    fun onTaskTitleChange(value: String) { _taskTitle.value = value }

    fun previousDeadlineDay() {
        _deadline.value = (_deadline.value ?: LocalDate.now()).minusDays(1)
    }

    fun nextDeadlineDay() {
        _deadline.value = (_deadline.value ?: LocalDate.now()).plusDays(1)
    }

    fun clearDeadline() {
        _deadline.value = null
    }

    fun selectProject(projectId: String) {
        _selectedProjectId.value = projectId
    }

    fun createProject() {
        val title = _title.value.trim()
        if (title.isBlank()) {
            _message.value = "Digite o nome do projeto."
            return
        }
        viewModelScope.launch {
            val project = Project(
                title = title,
                description = _description.value.trim().ifBlank { null },
                deadline = _deadline.value
            )
            projectRepository.save(project)
                .onSuccess {
                    _selectedProjectId.value = project.id
                    _title.value = ""
                    _description.value = ""
                    _deadline.value = null
                    _message.value = "Projeto criado."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível criar o projeto." }
        }
    }

    fun archiveProject(project: Project) {
        viewModelScope.launch {
            projectRepository.archive(project.id)
                .onSuccess {
                    if (_selectedProjectId.value == project.id) _selectedProjectId.value = null
                    _message.value = "Projeto arquivado."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível arquivar o projeto." }
        }
    }

    fun addTaskToSelectedProject() {
        val projectId = _selectedProjectId.value
        val taskTitle = _taskTitle.value.trim()
        if (projectId == null) {
            _message.value = "Selecione um projeto."
            return
        }
        if (taskTitle.isBlank()) {
            _message.value = "Digite o nome da tarefa do projeto."
            return
        }
        viewModelScope.launch {
            projectRepository.addTask(projectId, taskTitle)
                .onSuccess {
                    _taskTitle.value = ""
                    _message.value = "Tarefa adicionada ao projeto."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível adicionar a tarefa." }
        }
    }

    fun toggleProjectTask(projectId: String, taskId: String) {
        viewModelScope.launch {
            projectRepository.toggleTask(projectId, taskId)
                .onFailure { _message.value = it.message ?: "Não foi possível atualizar a tarefa." }
        }
    }

    fun clearMessage() { _message.value = null }
}
