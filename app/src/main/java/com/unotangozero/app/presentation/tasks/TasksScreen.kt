package com.unotangozero.app.presentation.tasks

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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.enums.TaskCategory
import com.unotangozero.app.domain.models.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun TasksRoute(
    onOpenProjects: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenFocusMode: () -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val taskDurations by viewModel.taskDurations.collectAsState()
    val taskTags by viewModel.taskTags.collectAsState()
    val selectedTag by viewModel.selectedTag.collectAsState()
    val editorState by viewModel.editorState.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        TasksScreen(
            tasks = tasks,
            taskDurations = taskDurations,
            taskTags = taskTags,
            selectedTag = selectedTag,
            editorState = editorState,
            onOpenProjects = onOpenProjects,
            onOpenKanban = onOpenKanban,
            onOpenFocus = onOpenFocus,
            onOpenFocusMode = onOpenFocusMode,
            onTitleChange = viewModel::onTitleChange,
            onDueDatePreviousDay = viewModel::onDueDatePreviousDay,
            onDueDateNextDay = viewModel::onDueDateNextDay,
            onDueDateToday = viewModel::onDueDateToday,
            onDueDateTomorrow = viewModel::onDueDateTomorrow,
            onDueDateNextWeek = viewModel::onDueDateNextWeek,
            onCategoryChange = viewModel::onCategoryChange,
            onPriorityChange = viewModel::onPriorityChange,
            onRecurrenceTypeChange = viewModel::onRecurrenceTypeChange,
            onEstimatedHoursChange = viewModel::onEstimatedHoursChange,
            onEstimatedMinutesChange = viewModel::onEstimatedMinutesChange,
            onTagsChange = viewModel::onTagsChange,
            onTagFilterChange = viewModel::onTagFilterChange,
            onSaveClick = viewModel::saveTaskFromEditor,
            onCancelEdit = viewModel::cancelEditing,
            onStartEdit = viewModel::startEditing,
            onToggleTask = viewModel::toggleCompleted,
            onDeleteTask = viewModel::deleteTask
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<Task>,
    taskDurations: Map<String, Int>,
    taskTags: Map<String, List<String>>,
    selectedTag: String?,
    editorState: TaskEditorUiState,
    onOpenProjects: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenFocusMode: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onDueDateToday: () -> Unit,
    onDueDateTomorrow: () -> Unit,
    onDueDateNextWeek: () -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Task) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    var isEditorOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(editorState.isEditing) {
        if (editorState.isEditing) isEditorOpen = true
    }

    val allTags = remember(taskTags) { taskTags.values.flatten().distinct().sorted() }
    val filteredTasks = remember(tasks, taskTags, selectedTag) {
        selectedTag?.let { tag -> tasks.filter { task -> taskTags[task.id].orEmpty().contains(tag) } } ?: tasks
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Lista de tarefas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text("Organize o dia e use o botão + para adicionar rapidamente.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            item { TaskQuickActionsCard(onOpenProjects, onOpenKanban, onOpenFocus, onOpenFocusMode) }

            if (allTags.isNotEmpty()) {
                item { TagFilterRow(tags = allTags, selectedTag = selectedTag, onTagFilterChange = onTagFilterChange) }
            }

            if (filteredTasks.isEmpty()) {
                item { EmptyTasksCard(hasFilter = selectedTag != null) }
            } else {
                items(items = filteredTasks, key = { it.id }) { task ->
                    TaskCard(
                        task = task,
                        estimatedDurationMinutes = taskDurations[task.id] ?: task.estimatedDurationMinutes,
                        tags = taskTags[task.id].orEmpty(),
                        onStartEdit = onStartEdit,
                        onToggleTask = onToggleTask,
                        onDeleteTask = onDeleteTask
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { isEditorOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Nova tarefa")
        }
    }

    if (isEditorOpen) {
        ModalBottomSheet(
            onDismissRequest = {
                onCancelEdit()
                isEditorOpen = false
            },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            TaskEditorSheet(
                state = editorState,
                onTitleChange = onTitleChange,
                onDueDatePreviousDay = onDueDatePreviousDay,
                onDueDateNextDay = onDueDateNextDay,
                onDueDateToday = onDueDateToday,
                onDueDateTomorrow = onDueDateTomorrow,
                onDueDateNextWeek = onDueDateNextWeek,
                onCategoryChange = onCategoryChange,
                onPriorityChange = onPriorityChange,
                onRecurrenceTypeChange = onRecurrenceTypeChange,
                onEstimatedHoursChange = onEstimatedHoursChange,
                onEstimatedMinutesChange = onEstimatedMinutesChange,
                onTagsChange = onTagsChange,
                onSaveClick = {
                    onSaveClick()
                    isEditorOpen = false
                },
                onCancelEdit = {
                    onCancelEdit()
                    isEditorOpen = false
                }
            )
        }
    }
}

@Composable
private fun TaskQuickActionsCard(onOpenProjects: () -> Unit, onOpenKanban: () -> Unit, onOpenFocus: () -> Unit, onOpenFocusMode: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        LazyRow(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = false, onClick = onOpenProjects, label = { Text("Projetos") }) }
            item { FilterChip(selected = false, onClick = onOpenKanban, label = { Text("Kanban") }) }
            item { FilterChip(selected = false, onClick = onOpenFocus, label = { Text("Foco") }) }
            item { FilterChip(selected = false, onClick = onOpenFocusMode, label = { Text("Modo Foco") }) }
        }
    }
}

@Composable
private fun TagFilterRow(tags: List<String>, selectedTag: String?, onTagFilterChange: (String?) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { FilterChip(selected = selectedTag == null, onClick = { onTagFilterChange(null) }, label = { Text("Todas") }) }
        items(tags) { tag -> FilterChip(selected = selectedTag == tag, onClick = { onTagFilterChange(tag) }, label = { Text("#$tag") }) }
    }
}

@Composable
private fun TaskEditorSheet(
    state: TaskEditorUiState,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onDueDateToday: () -> Unit,
    onDueDateTomorrow: () -> Unit,
    onDueDateNextWeek: () -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(if (state.isEditing) "Editar tarefa" else "Nova tarefa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.title, onValueChange = onTitleChange, label = { Text("Digite uma tarefa...") }, singleLine = true)
        DateSelector(state.dueDate, onDueDatePreviousDay, onDueDateNextDay, onDueDateToday, onDueDateTomorrow, onDueDateNextWeek)
        DurationFields(state, onEstimatedHoursChange, onEstimatedMinutesChange)
        OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.tagsText, onValueChange = onTagsChange, label = { Text("Tags") }, placeholder = { Text("Ex: trabalho, casa, estudo") }, singleLine = true)
        ChipSelector("Categoria", TaskCategory.entries, state.category, { it.displayName }, onCategoryChange)
        ChipSelector("Prioridade", Priority.entries, state.priority, { it.displayName }, onPriorityChange)
        val recurrenceOptions = listOf(RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.MONTHLY, RecurrenceType.YEARLY)
        ChipSelector("Recorrência", recurrenceOptions, state.recurrenceType, { it.displayName }, onRecurrenceTypeChange)
        Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveClick) { Text(if (state.isEditing) "Salvar alterações" else "Adicionar") }
        OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar") }
    }
}

@Composable
private fun DurationFields(state: TaskEditorUiState, onEstimatedHoursChange: (String) -> Unit, onEstimatedMinutesChange: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedHoursText, onValueChange = onEstimatedHoursChange, label = { Text("Horas") }, singleLine = true)
        OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedMinutesText, onValueChange = onEstimatedMinutesChange, label = { Text("Min") }, singleLine = true)
    }
}

@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit, onToday: () -> Unit, onTomorrow: () -> Unit, onNextWeek: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior") }
            Text(date.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia") }
        }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item { FilterChip(selected = false, onClick = onToday, label = { Text("Hoje") }) }
            item { FilterChip(selected = false, onClick = onTomorrow, label = { Text("Amanhã") }) }
            item { FilterChip(selected = false, onClick = onNextWeek, label = { Text("Próx. semana") }) }
        }
    }
}

@Composable
private fun <T> ChipSelector(title: String, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option -> FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(label(option)) }) }
        }
    }
}

@Composable
private fun EmptyTasksCard(hasFilter: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (hasFilter) "Nenhuma tarefa com essa tag" else "Nenhuma tarefa cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(if (hasFilter) "Limpe o filtro ou escolha outra tag." else "Toque no + para criar sua primeira tarefa.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TaskCard(task: Task, estimatedDurationMinutes: Int, tags: List<String>, onStartEdit: (Task) -> Unit, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                val recurrenceText = task.recurrenceType?.let { " • ${it.displayName}" } ?: ""
                val durationText = if (estimatedDurationMinutes > 0) " • ${formatDuration(estimatedDurationMinutes)}" else ""
                Text("${task.category.displayName} • ${task.priority.displayName} • ${task.dueDate.format(dateFormatter)}$recurrenceText$durationText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (tags.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(tags) { tag -> AssistChip(onClick = {}, label = { Text("#$tag") }) } }
                }
            }
            IconButton(onClick = { onStartEdit(task) }) { Icon(Icons.Default.Edit, contentDescription = "Editar tarefa") }
            IconButton(onClick = { onDeleteTask(task) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa") }
        }
    }
}

private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
}
