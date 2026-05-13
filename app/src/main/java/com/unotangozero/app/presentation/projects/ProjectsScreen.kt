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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.rememberDatePickerState
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
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
            onDeadlineSelected = viewModel::onDeadlineSelected,
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
    onDeadlineSelected: (LocalDate) -> Unit,
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
                    Text("Abra um projeto e organize suas entregas em um Kanban simples.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (projects.isNotEmpty()) {
                item { ProjectOverviewRow(projects, selectedProjectId, onSelectProject) }
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
                    item { ProjectKanbanBoard(selectedProject, onToggleSection, onToggleTask) }
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
                    onDeadlineSelected = onDeadlineSelected,
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
private fun ProjectOverviewRow(projects: List<Project>, selectedProjectId: String?, onSelectProject: (String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(projects, key = { it.id }) { project ->
            ProjectOverviewCard(
                project = project,
                isSelected = selectedProjectId == project.id,
                onClick = { onSelectProject(project.id) }
            )
        }
    }
}

@Composable
private fun ProjectOverviewCard(project: Project, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    Card(
        onClick = onClick,
        modifier = Modifier.width(260.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(project.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${project.completedTasks}/${project.totalTasks} tarefas • ${project.progressPercent}%", color = MaterialTheme.colorScheme.onSurfaceVariant)
            LinearProgressIndicator(progress = { project.progressPercent / 100f }, modifier = Modifier.fillMaxWidth())
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
private fun ProjectKanbanBoard(project: Project, onToggleSection: (String, String) -> Unit, onToggleTask: (String, String) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        if (project.tasks.isNotEmpty()) {
            item(key = "backlog-${project.id}") {
                ProjectBacklogColumn(projectId = project.id, tasks = project.tasks, onToggleTask = onToggleTask)
            }
        }
        items(project.sections, key = { it.id }) { section ->
            ProjectSectionColumn(projectId = project.id, section = section, onToggleSection = onToggleSection, onToggleTask = onToggleTask)
        }
    }
}

@Composable
private fun ProjectBacklogColumn(projectId: String, tasks: List<ProjectTask>, onToggleTask: (String, String) -> Unit) {
    Card(modifier = Modifier.width(300.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Sem seção", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("${tasks.count { it.isCompleted }}/${tasks.size} concluídas", color = MaterialTheme.colorScheme.onSurfaceVariant)
            tasks.forEach { task -> ProjectTaskCard(projectId = projectId, task = task, onToggleTask = onToggleTask) }
        }
    }
}

@Composable
private fun ProjectSectionColumn(projectId: String, section: ProjectSection, onToggleSection: (String, String) -> Unit, onToggleTask: (String, String) -> Unit) {
    Card(modifier = Modifier.width(300.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
private fun ProjectFormSheet(
    title: String,
    description: String,
    deadline: LocalDate?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onPreviousDeadlineDay: () -> Unit,
    onNextDeadlineDay: () -> Unit,
    onDeadlineSelected: (LocalDate) -> Unit,
    onClearDeadline: () -> Unit,
    onCreateProject: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Novo projeto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = title, onValueChange = onTitleChange, label = { Text("Nome do projeto") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = description, onValueChange = onDescriptionChange, label = { Text("Descrição opcional") }, minLines = 2)
        DeadlineSelector(deadline, onPreviousDeadlineDay, onNextDeadlineDay, onDeadlineSelected, onClearDeadline)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateProject) { Text("Criar projeto") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClose) { Text("Cancelar") }
    }
}

@Composable
private fun SectionFormSheet(sectionTitle: String, onSectionTitleChange: (String) -> Unit, onCreateSection: () -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nova seção", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = sectionTitle, onValueChange = onSectionTitleChange, label = { Text("Nome da coluna do Kanban") }, placeholder = { Text("Ex: A fazer, Em andamento, Concluído") }, singleLine = true)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeadlineSelector(
    deadline: LocalDate?,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onDeadlineSelected: (LocalDate) -> Unit,
    onClear: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    var isCalendarOpen by remember { mutableStateOf(false) }
    val dateForPicker = deadline ?: LocalDate.now()
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateForPicker.toEpochMillis())

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = null) }
                Button(onClick = { isCalendarOpen = true }) {
                    Text(deadline?.format(formatter) ?: "Selecionar prazo")
                }
                IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = null) }
            }
            TextButton(onClick = onClear) { Text("Remover prazo") }
        }
    }

    if (isCalendarOpen) {
        DatePickerDialog(
            onDismissRequest = { isCalendarOpen = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis -> onDeadlineSelected(millis.toLocalDate()) }
                        isCalendarOpen = false
                    }
                ) { Text("Selecionar") }
            },
            dismissButton = { TextButton(onClick = { isCalendarOpen = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
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
            text = "Nenhum projeto selecionado. Toque no + para criar um projeto ou escolha um card acima.",
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyProjectContentCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Text(
            text = "Este projeto ainda não tem colunas ou tarefas. Crie uma seção para montar o Kanban ou adicione uma tarefa solta.",
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
