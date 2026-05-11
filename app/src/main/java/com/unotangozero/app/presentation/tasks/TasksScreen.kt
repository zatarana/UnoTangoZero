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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.models.Task
import java.time.format.DateTimeFormatter

@Composable
fun TasksRoute(
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsState()
    val title by viewModel.newTaskTitle.collectAsState()
    val recurrenceType by viewModel.selectedRecurrenceType.collectAsState()
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
            newTaskTitle = title,
            recurrenceType = recurrenceType,
            onTitleChange = viewModel::onNewTaskTitleChange,
            onRecurrenceTypeChange = viewModel::onRecurrenceTypeChange,
            onCreateClick = viewModel::createTodayTask,
            onToggleTask = viewModel::toggleCompleted,
            onDeleteTask = viewModel::deleteTask
        )
    }
}

@Composable
fun TasksScreen(
    tasks: List<Task>,
    newTaskTitle: String,
    recurrenceType: RecurrenceType,
    onTitleChange: (String) -> Unit,
    onRecurrenceTypeChange: (RecurrenceType) -> Unit,
    onCreateClick: () -> Unit,
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
                Text("Crie tarefas rápidas, recorrentes e com lembrete padrão.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = newTaskTitle, onValueChange = onTitleChange, label = { Text("Nova tarefa") }, singleLine = true)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val options = listOf(RecurrenceType.NONE, RecurrenceType.DAILY, RecurrenceType.WEEKLY, RecurrenceType.MONTHLY, RecurrenceType.YEARLY)
                        items(options) { option ->
                            FilterChip(
                                selected = recurrenceType == option,
                                onClick = { onRecurrenceTypeChange(option) },
                                label = { Text(option.displayName) }
                            )
                        }
                    }
                    Button(modifier = Modifier.fillMaxWidth(), onClick = onCreateClick) {
                        Text("Adicionar para hoje")
                    }
                }
            }
        }

        if (tasks.isEmpty()) {
            item { EmptyTasksCard() }
        } else {
            items(items = tasks, key = { it.id }) { task ->
                TaskCard(task = task, onToggleTask = onToggleTask, onDeleteTask = onDeleteTask)
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
private fun TaskCard(task: Task, onToggleTask: (Task) -> Unit, onDeleteTask: (Task) -> Unit) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.isCompleted, onCheckedChange = { onToggleTask(task) })
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
                )
                val recurrenceText = task.recurrenceType?.let { " • ${it.displayName}" } ?: ""
                Text(
                    text = "${task.category.displayName} • ${task.priority.displayName} • ${task.dueDate.format(dateFormatter)}$recurrenceText",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { onDeleteTask(task) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir tarefa")
            }
        }
    }
}
