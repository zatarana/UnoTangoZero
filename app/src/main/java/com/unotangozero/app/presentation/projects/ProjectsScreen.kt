package com.unotangozero.app.presentation.projects

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

private enum class ProjectSheetMode { PROJECT, SECTION, TASK }

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

@OptIn(ExperimentalMaterial3Api::class)
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
    var sheetMode by remember { mutableStateOf<ProjectSheetMode?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Projetos", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Organize tarefas maiores por projeto, seção e progresso.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (projects.isNotEmpty()) {
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(projects, key = { it.id }) { project ->
                            FilterChip(
                                selected = selectedProjectId == project.id,
                                onClick = { onSelectProject(project.id) },
                                label = { Text(project.title) }
                            )
                        }
                    }
                }
            }

            if (selectedProject == null) {
                item { EmptyProjectCard() }
            } else {
                item { ProjectHeaderCard(selectedProject, onArchiveProject) }
                item {
                    ProjectActionsRow(
                        onNewSection = { sheetMode = ProjectSheetMode.SECTION },
                        onNewTask = { sheetMode = ProjectSheetMode.TASK }
                    )
                }

                if (selectedProject.totalTasks == 0 && selectedProject.sections.isEmpty()) {
                    item { EmptyProjectContentCard() }
                } else {
                    if (selectedProject.tasks.isNotEmpty()) {
                        item { Text("Sem seção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold) }
                        items(selectedProject.tasks, key = { it.id }) { task ->
                            ProjectTaskCard(projectId = selectedProject.id, task = task, onToggleTask = onToggleTask)
                        }
                    }

                    items(selectedProject.sections, key = { it.id }) { section ->
                        ProjectSectionCard(projectId = selectedProject.id, section = section, onToggleSection = onToggleSection, onToggleTask = onToggleTask)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { sheetMode = ProjectSheetMode.PROJECT },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Novo projeto")
        }
    }

    if (sheetMode != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetMode = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            when (sheetMode) {
                ProjectSheetMode.PROJECT -> ProjectFormSheet(
                    title = title,
                    description = description,
                    deadline = deadline,
                    onTitleChange = onTitleChange,
                    onDescriptionChange = onDescriptionChange,
                    onPreviousDeadlineDay = onPreviousDeadlineDay,
                    onNextDeadlineDay = onNextDeadlineDay,
                    onClearDeadline = onClearDeadline,
                    onCreateProject = {
                        onCreateProject()
                        sheetMode = null
                    },
                    onClose = { sheetMode = null }
                )
                ProjectSheetMode.SECTION -> SectionFormSheet(
                    sectionTitle = sectionTitle,
                    onSectionTitleChange = onSectionTitleChange,
                    onCreateSection = {
                        onCreateSection()
                        sheetMode = null
                    },
                    onClose = { sheetMode = null }
                )
                ProjectSheetMode.TASK -> selectedProject?.let { project ->
                    ProjectTaskFormSheet(
                        selectedProject = project,
                        selectedSectionId = selectedSectionId,
                        taskTitle = taskTitle,
                        onSelectSection = onSelectSection,
                        onTaskTitleChange = onTaskTitleChange,
                        onAddTask = {
                            onAddTask()
                            sheetMode = null
                        },
                        onClose = { sheetMode = null }
                    )
                }
                null -> Unit
            }
        }
    }
}

@Composable
private fun ProjectActionsRow(onNewSection: () -> Unit, onNewTask: () -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = false, onClick = onNewSection, label = { Text("Nova seção") }) }
        item { FilterChip(selected = false, onClick = onNewTask, label = { Text("Nova tarefa") }) }
    }
}

@Composable
private fun ProjectFormSheet(
    title: String,
    description: String,
    deadline: LocalDate?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDeadlineDay: () -> Unit,
    onNextDeadlineDay: () -> Unit,
    onClearDeadline: () -> Unit,
    onCreateProject: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Novo projeto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = title, onValueChange = onTitleChange, label = { Text("Nome do projeto") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = description, onValueChange = onDescriptionChange, label = { Text("Descrição opcional") }, minLines = 2)
        DeadlineSelector(deadline, onPreviousDeadlineDay, onNextDeadlineDay, onClearDeadline)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateProject) { Text("Criar projeto") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun SectionFormSheet(sectionTitle: String, onSectionTitleChange: (String) -> Unit, onCreateSection: () -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova seção", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = sectionTitle, onValueChange = onSectionTitleChange, label = { Text("Cabeçalho da seção") }, singleLine = true)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateSection) { Text("Criar seção") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun ProjectTaskFormSheet(
    selectedProject: Project,
    selectedSectionId: String?,
    taskTitle: String,
    onSelectSection: (String?) -> Unit,
    onTaskTitleChange: (String) -> Unit,
    onAddTask: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova tarefa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        SectionPicker(selectedProject, selectedSectionId, onSelectSection)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = taskTitle, onValueChange = onTaskTitleChange, label = { Text("Tarefa do projeto") }, singleLine = true)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onAddTask) { Text("Adicionar tarefa") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun SectionPicker(project: Project, selectedSectionId: String?, onSelectSection: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selectedSectionId == null, onClick = { onSelectSection(null) }, label = { Text("Sem seção") }) }
        items(project.sections, key = { it.id }) { section ->
            FilterChip(selected = selectedSectionId == section.id, onClick = { onSelectSection(section.id) }, label = { Text(section.title) })
        }
    }
}

@Composable
private fun ProjectSectionCard(projectId: String, section: ProjectSection, onToggleSection: (String, String) -> Unit, onToggleTask: (String, String) -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(section.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text("${section.completedTasks}/${section.totalTasks} concluídas", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { onToggleSection(projectId, section.id) }) {
                    Icon(if (section.isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess, contentDescription = "Recolher seção")
                }
            }
            if (!section.isCollapsed) {
                if (section.tasks.isEmpty()) {
                    Text("Nenhuma tarefa nesta seção.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    section.tasks.forEach { task -> ProjectTaskCard(projectId = projectId, task = task, onToggleTask = onToggleTask) }
                }
            }
        }
    }
}

@Composable
private fun DeadlineSelector(deadline: LocalDate?, onPrevious: () -> Unit, onNext: () -> Unit, onClear: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(project.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                IconButton(onClick = { onArchiveProject(project) }) { Icon(Icons.Default.Archive, contentDescription = "Arquivar") }
            }
            project.description?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            project.deadline?.let { Text("Prazo: ${it.format(formatter)}", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            LinearProgressIndicator(progress = { project.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
            Text("${project.completedTasks}/${project.totalTasks} tarefas • ${project.progressPercent}%", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ProjectTaskCard(projectId: String, task: ProjectTask, onToggleTask: (String, String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(projectId, task.id) })
        Spacer(Modifier.width(8.dp))
        Text(task.title, style = MaterialTheme.typography.titleMedium, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
    }
}

@Composable
private fun EmptyProjectCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = "Nenhum projeto selecionado. Toque no + para criar um projeto.",
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyProjectContentCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = "Este projeto ainda não tem seções ou tarefas. Use os atalhos acima para começar.",
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
