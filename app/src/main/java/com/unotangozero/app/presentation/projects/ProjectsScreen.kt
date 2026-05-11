package com.unotangozero.app.presentation.projects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.Project
import com.unotangozero.app.domain.models.ProjectSection
import com.unotangozero.app.domain.models.ProjectTask
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun ProjectsRoute(viewModel: ProjectsViewModel = hiltViewModel()) {
    val projects by viewModel.projects.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val selectedSectionId by viewModel.selectedSectionId.collectAsState()
    val title by viewModel.title.collectAsState()
    val description by viewModel.description.collectAsState()
    val deadline by viewModel.deadline.collectAsState()
    val sectionTitle by viewModel.sectionTitle.collectAsState()
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
            selectedSectionId = selectedSectionId,
            title = title,
            description = description,
            deadline = deadline,
            sectionTitle = sectionTitle,
            taskTitle = taskTitle,
            onTitleChange = viewModel::onTitleChange,
            onDescriptionChange = viewModel::onDescriptionChange,
            onPreviousDeadlineDay = viewModel::previousDeadlineDay,
            onNextDeadlineDay = viewModel::nextDeadlineDay,
            onClearDeadline = viewModel::clearDeadline,
            onCreateProject = viewModel::createProject,
            onSelectProject = viewModel::selectProject,
            onArchiveProject = viewModel::archiveProject,
            onSectionTitleChange = viewModel::onSectionTitleChange,
            onCreateSection = viewModel::addSectionToSelectedProject,
            onSelectSection = viewModel::selectSection,
            onToggleSection = viewModel::toggleSection,
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
    selectedSectionId: String?,
    title: String,
    description: String,
    deadline: LocalDate?,
    sectionTitle: String,
    taskTitle: String,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDeadlineDay: () -> Unit,
    onNextDeadlineDay: () -> Unit,
    onClearDeadline: () -> Unit,
    onCreateProject: () -> Unit,
    onSelectProject: (String) -> Unit,
    onArchiveProject: (Project) -> Unit,
    onSectionTitleChange: (String) -> Unit,
    onCreateSection: () -> Unit,
    onSelectSection: (String?) -> Unit,
    onToggleSection: (String, String) -> Unit,
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
            Text("Agrupe tarefas por objetivo, seções e progresso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                        Text("Nova seção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        OutlinedTextField(Modifier.fillMaxWidth(), sectionTitle, onSectionTitleChange, label = { Text("Cabeçalho da seção") }, singleLine = true)
                        Button(Modifier.fillMaxWidth(), onClick = onCreateSection) { Text("Criar seção") }
                    }
                }
            }
            item {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Nova tarefa", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        SectionPicker(selectedProject, selectedSectionId, onSelectSection)
                        OutlinedTextField(Modifier.fillMaxWidth(), taskTitle, onTaskTitleChange, label = { Text("Tarefa do projeto") }, singleLine = true)
                        Button(Modifier.fillMaxWidth(), onClick = onAddTask) { Text("Adicionar tarefa") }
                    }
                }
            }

            if (selectedProject.totalTasks == 0 && selectedProject.sections.isEmpty()) {
                item { Text("Nenhuma seção ou tarefa neste projeto ainda.") }
            } else {
                if (selectedProject.tasks.isNotEmpty()) {
                    item { Text("Sem seção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                    items(selectedProject.tasks, key = { it.id }) { task ->
                        ProjectTaskCard(projectId = selectedProject.id, task = task, onToggleTask = onToggleTask)
                    }
                }

                items(selectedProject.sections, key = { it.id }) { section ->
                    ProjectSectionCard(
                        projectId = selectedProject.id,
                        section = section,
                        onToggleSection = onToggleSection,
                        onToggleTask = onToggleTask
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionPicker(project: Project, selectedSectionId: String?, onSelectSection: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
            FilterChip(selected = selectedSectionId == null, onClick = { onSelectSection(null) }, label = { Text("Sem seção") })
        }
        items(project.sections, key = { it.id }) { section ->
            FilterChip(selected = selectedSectionId == section.id, onClick = { onSelectSection(section.id) }, label = { Text(section.title) })
        }
    }
}

@Composable
private fun ProjectSectionCard(projectId: String, section: ProjectSection, onToggleSection: (String, String) -> Unit, onToggleTask: (String, String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${section.completedTasks}/${section.totalTasks} concluídas", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onToggleSection(projectId, section.id) }) {
                    Icon(if (section.isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess, contentDescription = "Recolher seção")
                }
            }
            if (!section.isCollapsed) {
                if (section.tasks.isEmpty()) {
                    Text("Nenhuma tarefa nesta seção.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    section.tasks.forEach { task ->
                        ProjectTaskCard(projectId = projectId, task = task, onToggleTask = onToggleTask)
                    }
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
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(projectId, task.id) })
        Spacer(Modifier.width(8.dp))
        Text(
            task.title,
            style = MaterialTheme.typography.titleMedium,
            textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
        )
    }
}
