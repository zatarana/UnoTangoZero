package com.unotangozero.app.presentation.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.Project
import com.unotangozero.app.domain.models.ProjectTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProjectsRoute(viewModel: ProjectsViewModel = hiltViewModel()) {
    val projects by viewModel.projects.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val deadline by viewModel.deadline.collectAsState()
    val taskTitle by viewModel.taskTitle.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(Modifier.fillMaxSize()) {
        SnackbarHost(snackbarHostState)
        ProjectsScreen(
            projects = projects,
            selectedProjectId = selectedProjectId,
            title = title,
            description = description,
            deadline = deadline,
            taskTitle = taskTitle,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onPreviousDeadlineDay = viewModel::previousDeadlineDay,
            onNextDeadlineDay = viewModel::nextDeadlineDay,
            onClearDeadline = viewModel::clearDeadline,
            onCreateProject = viewModel::createProject,
            onSelectProject = viewModel::selectProject,
            onArchiveProject = viewModel::archiveProject,
            onTaskTitleChange = viewModel::onTaskTitleChange,
            onAddTask = viewModel::addTaskToSelectedProject,
            onToggleTask = viewModel::toggleProjectTask
        )
    }
}

@Composable
fun ProjectsScreen(
    projects: List<Project>,
    selectedProjectId: String?,
    title: String,
    description: String,
    deadline: LocalDate?,
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDeadlineDay: () -> Unit,
    onNextDeadlineDay: () -> Unit,
    onClearDeadline: () -> Unit,
    onCreateProject: () -> Unit,
    onSelectProject: (String) -> Unit,
    onArchiveProject: (Project) -> Unit,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onToggleTask: (String, String) -> Unit
) {
    val selectedProject = projects.firstOrNull { it.id == selectedProjectId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Projetos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Agrupe tarefas por objetivo e acompanhe o progresso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(Modifier.fillMaxWidth(), title, onTitleChange, label = { Text("Nome do projeto") }, singleLine = true)
                    OutlinedTextField(Modifier.fillMaxWidth(), description, onDescriptionChange, label = { Text("Descrição opcional") }, minLines = 2)
                    DeadlineSelector(deadline, onPreviousDeadlineDay, onNextDeadlineDay, onClearDeadline)
                    Button(Modifier.fillMaxWidth(), onClick = onCreateProject) { Text("Criar projeto") }
                }
            }
        }

        if (projects.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(projects, key = { it.id }) { project ->
                        FilterChip(selected = selectedProjectId == project.id, onClick = { onSelectProject(project.id) }, label = { Text(project.title) })
                    }
                }
            }
        }

        if (selectedProject == null) {
            item { Text("Crie ou selecione um projeto para ver detalhes.") }
        } else {
            item { ProjectHeaderCard(selectedProject, onArchiveProject) }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(Modifier.fillMaxWidth(), taskTitle, onTaskTitleChange, label = { Text("Nova tarefa do projeto") }, singleLine = true)
                        Button(Modifier.fillMaxWidth(), onClick = onAddTask) { Text("Adicionar tarefa") }
                    }
                }
            }
            if (selectedProject.tasks.isEmpty()) {
                item { Text("Nenhuma tarefa neste projeto ainda.") }
            } else {
                items(selectedProject.tasks, key = { it.id }) { task ->
                    ProjectTaskCard(projectId = selectedProject.id, task = task, onToggleTask = onToggleTask)
                }
            }
        }
    }
}

@Composable
private fun DeadlineSelector(deadline: LocalDate?, onPrevious: () -> Unit, onNext: () -> Unit, onClear: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = null) }
                Text(deadline?.format(formatter) ?: "Sem prazo", fontWeight = FontWeight.Bold)
                IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = null) }
            }
            TextButton(onClick = onClear) { Text("Remover prazo") }
        }
    }
}

@Composable
private fun ProjectHeaderCard(project: Project, onArchiveProject: (Project) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(project.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { onArchiveProject(project) }) { Icon(Icons.Default.Archive, contentDescription = "Arquivar") }
            }
            project.description?.let { Text(it) }
            project.deadline?.let { Text("Prazo: ${it.format(formatter)}") }
            LinearProgressIndicator(progress = project.progressPercent / 100f, modifier = Modifier.fillMaxWidth())
            Text("${project.completedTasks}/${project.totalTasks} tarefas • ${project.progressPercent}%")
        }
    }
}

@Composable
private fun ProjectTaskCard(projectId: String, task: ProjectTask, onToggleTask: (String, String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(projectId, task.id) })
            Spacer(Modifier.width(8.dp))
            Text(
                task.title,
                style = MaterialTheme.typography.titleMedium,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
        }
    }
}
