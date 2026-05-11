package com.unotangozero.app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
    val allTags = remember(taskTags) {
        taskTags.values.flatten().distinct().sorted()
    }
    val filteredTasks = remember(tasks, taskTags, selectedTag) {
        selectedTag?.let { tag -> tasks.filter { task -> taskTags[task.id].orEmpty().contains(tag) } } ?: tasks
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tarefas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Crie a tarefa agora ou abra projetos, Kanban e foco em um toque.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item { TaskQuickActionsCard(onOpenProjects, onOpenKanban, onOpenFocus, onOpenFocusMode) }

        item {
            TaskEditorCard(
                state = editorState,
                onTitleChange = onTitleChange,
                onDueDatePreviousDay = onDueDatePreviousDay,
                onDueDateNextDay = onDueDateNextDay,
                onCategoryChange = onCategoryChange,
                onPriorityChange = onPriorityChange,
                onRecurrenceTypeChange = onRecurrenceTypeChange,
                onEstimatedHoursChange = onEstimatedHoursChange,
                onEstimatedMinutesChange = onEstimatedMinutesChange,
                onTagsChange = onTagsChange,
                onSaveClick = onSaveClick,
                onCancelEdit = onCancelEdit
            )
        }

        if (allTags.isNotEmpty()) {
            item {
                TagFilterRow(tags = allTags, selectedTag = selectedTag, onTagFilterChange = onTagFilterChange)
            }
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
}

@Composable
private fun TaskQuickActionsCard(
    onOpenProjects: () -> Unit,
    onOpenKanban: () -> Unit,
    onOpenFocus: () -> Unit,
    onOpenFocusMode: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Ações rápidas", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(modifier = Modifier.weight(1f), onClick = onOpenProjects) { Text("Projetos") }
                Button(modifier = Modifier.weight(1f), onClick = onOpenKanban) { Text("Kanban") }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(modifier = Modifier.weight(1f), onClick = onOpenFocus) { Text("Foco") }
                Button(modifier = Modifier.weight(1f), onClick = onOpenFocusMode) { Text("Modo Foco") }
            }
        }
    }
}

@Composable
private fun TagFilterRow(tags: List<String>, selectedTag: String?, onTagFilterChange: (String?) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Filtrar por tag", style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                FilterChip(
                    selected = selectedTag == null,
                    onClick = { onTagFilterChange(null) },
                    label = { Text("Todas") }
                )
            }
            items(tags) { tag ->
                FilterChip(
                    selected = selectedTag == tag,
                    onClick = { onTagFilterChange(tag) },
                    label = { Text("#$tag") }
                )
            }
        }
    }
}

@Composable
private fun TaskEditorCard(
    state: TaskEditorUiState,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit,
    onTagsChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.isEditing) "Editar tarefa" else "Nova tarefa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.title, onValueChange = onTitleChange, label = { Text("Título") }, singleLine = true)
            DateSelector(date = state.dueDate, onPrevious = onDueDatePreviousDay, onNext = onDueDateNextDay)
            DurationFields(state = state, onEstimatedHoursChange = onEstimatedHoursChange, onEstimatedMinutesChange = onEstimatedMinutesChange)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = state.tagsText,
                onValueChange = onTagsChange,
                label = { Text("Tags") },
                placeholder = { Text("Ex: trabalho, casa, estudo") },
                singleLine = true
            )
            ChipSelector(title = "Categoria", options = TaskCategory.entries, selected = state.category, label = { it.displayName }, onSelect = onCategoryChange)
            ChipSelector(title = "Prioridade", options = Priority.entries, selected = state.priority, label = { it.displayName }, onSelect = onPriorityChange)
            val recurrenceOptions = listOf(RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.MONTHLY, RecurrenceType.YEARLY)
            ChipSelector(title = "Recorrência", options = recurrenceOptions, selected = state.recurrenceType, label = { it.displayName }, onSelect = onRecurrenceTypeChange)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveClick) { Text(if (state.isEditing) "Salvar alterações" else "Criar tarefa") }
            if (state.isEditing) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar edição") }
            }
        }
    }
}

@Composable
private fun DurationFields(
    state: TaskEditorUiState,
    onEstimatedHoursChange: (String) -> Unit,
    onEstimatedMinutesChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Duração estimada", style = MaterialTheme.typography.labelLarge)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedHoursText, onValueChange = onEstimatedHoursChange, label = { Text("Horas") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.weight(1f), value = state.estimatedMinutesText, onValueChange = onEstimatedMinutesChange, label = { Text("Minutos") }, singleLine = true)
        }
    }
}

@Composable
private fun DateSelector(date: LocalDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Row(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious) { Icon(Icons.Default.ChevronLeft, contentDescription = "Dia anterior") }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Data", style = MaterialTheme.typography.labelMedium)
                Text(date.format(formatter), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            IconButton(onClick = onNext) { Icon(Icons.Default.ChevronRight, contentDescription = "Próximo dia") }
        }
    }
}

@Composable
private fun <T> ChipSelector(title: String, options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(options) { option ->
                FilterChip(selected = selected == option, onClick = { onSelect(option) }, label = { Text(label(option)) })
            }
        }
    }
}

@Composable
private fun EmptyTasksCard(hasFilter: Boolean) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (hasFilter) "Nenhuma tarefa com essa tag" else "Nenhuma tarefa cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(if (hasFilter) "Limpe o filtro ou escolha outra tag." else "Adicione uma tarefa acima para começar seu planejamento.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TaskCard(
    task: Task,
    estimatedDurationMinutes: Int,
    tags: List<String>,
    onStartEdit: (Task) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                val recurrenceText = task.recurrenceType?.let { " • ${it.displayName}" } ?: ""
                val durationText = if (estimatedDurationMinutes > 0) " • ${formatDuration(estimatedDurationMinutes)}" else ""
                Text("${task.category.displayName} • ${task.priority.displayName} • ${task.dueDate.format(dateFormatter)}$recurrenceText$durationText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (tags.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(tags) { tag -> AssistChip(onClick = {}, label = { Text("#$tag") }) }
                    }
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
