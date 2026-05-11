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
fun TasksRoute(viewModel: TasksViewModel = hiltViewModel()) {
    val tasks by viewModel.tasks.collectAsState()
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
            editorState = editorState,
            onTitleChange = viewModel::onTitleChange,
            onDueDatePreviousDay = viewModel::onDueDatePreviousDay,
            onDueDateNextDay = viewModel::onDueDateNextDay,
            onCategoryChange = viewModel::onCategoryChange,
            onPriorityChange = viewModel::onPriorityChange,
            onRecurrenceTypeChange = viewModel::onRecurrenceTypeChange,
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
    editorState: TaskEditorUiState,
    onTitleChange: (String) -> Unit,
    onDueDatePreviousDay: () -> Unit,
    onDueDateNextDay: () -> Unit,
    onCategoryChange: (TaskCategory) -> Unit,
    onPriorityChange: (Priority) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Task) -> Unit,
    onToggleTask: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tarefas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Crie, edite e acompanhe tarefas com recorrência e lembrete padrão.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            TaskEditorCard(
                state = editorState,
                onTitleChange = onTitleChange,
                onDueDatePreviousDay = onDueDatePreviousDay,
                onDueDateNextDay = onDueDateNextDay,
                onCategoryChange = onCategoryChange,
                onPriorityChange = onPriorityChange,
                onRecurrenceTypeChange = onRecurrenceTypeChange,
                onSaveClick = onSaveClick,
                onCancelEdit = onCancelEdit
            )
        }

        if (tasks.isEmpty()) {
            item { EmptyTasksCard() }
        } else {
            items(items = tasks, key = { it.id }) { task ->
                TaskCard(task = task, onStartEdit = onStartEdit, onToggleTask = onToggleTask, onDeleteTask = onDeleteTask)
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
    onSaveClick: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.isEditing) "Editar tarefa" else "Nova tarefa", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.title, onValueChange = onTitleChange, label = { Text("Título") }, singleLine = true)
            DateSelector(date = state.dueDate, onPrevious = onDueDatePreviousDay, onNext = onDueDateNextDay)
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
private fun EmptyTasksCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma tarefa cadastrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Adicione uma tarefa acima para começar seu planejamento.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun TaskCard(task: Task, onStartEdit: (Task) -> Unit, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(task.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null)
                val recurrenceText = task.recurrenceType?.let { " • ${it.displayName}" } ?: ""
                Text("${task.category.displayName} • ${task.priority.displayName} • ${task.dueDate.format(dateFormatter)}$recurrenceText", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { onStartEdit(task) }) { Icon(Icons.Default.Edit, contentDescription = "Editar tarefa") }
            IconButton(onClick = { onDeleteTask(task) }) { Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa") }
        }
    }
}
