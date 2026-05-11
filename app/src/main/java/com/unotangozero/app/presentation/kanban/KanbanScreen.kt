package com.unotangozero.app.presentation.kanban

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.data.tasks.TaskKanbanColumn
import com.unotangozero.app.domain.models.Task
import java.time.format.DateTimeFormatter

@Composable
fun KanbanRoute(viewModel: KanbanViewModel = hiltViewModel()) {
    val columns by viewModel.columns.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(message) {
        message?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        KanbanScreen(columns = columns, onMoveTask = viewModel::moveTask)
    }
}

@Composable
fun KanbanScreen(
    columns: List<KanbanColumnUiState>,
    onMoveTask: (Task, TaskKanbanColumn) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Kanban", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Visualize tarefas por estágio e mova entre A fazer, Em andamento e Concluído.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            columns.forEach { column ->
                KanbanColumnCard(column = column, onMoveTask = onMoveTask)
            }
        }
    }
}

@Composable
private fun KanbanColumnCard(
    column: KanbanColumnUiState,
    onMoveTask: (Task, TaskKanbanColumn) -> Unit
) {
    Card(
        modifier = Modifier.width(300.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "${column.column.displayName} (${column.tasks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (column.tasks.isEmpty()) {
                Text("Nenhuma tarefa aqui.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                column.tasks.forEach { task ->
                    KanbanTaskCard(task = task, currentColumn = column.column, onMoveTask = onMoveTask)
                }
            }
        }
    }
}

@Composable
private fun KanbanTaskCard(
    task: Task,
    currentColumn: TaskKanbanColumn,
    onMoveTask: (Task, TaskKanbanColumn) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
            )
            Text(
                text = "${task.priority.displayName} • ${task.dueDate.format(formatter)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = { onMoveTask(task, previousColumn(currentColumn)) },
                    enabled = currentColumn != TaskKanbanColumn.TODO
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Mover para coluna anterior")
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onMoveTask(task, nextColumn(currentColumn)) },
                    enabled = currentColumn != TaskKanbanColumn.DONE
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "Mover para próxima coluna")
                }
            }
        }
    }
}

private fun previousColumn(column: TaskKanbanColumn): TaskKanbanColumn = when (column) {
    TaskKanbanColumn.TODO -> TaskKanbanColumn.TODO
    TaskKanbanColumn.IN_PROGRESS -> TaskKanbanColumn.TODO
    TaskKanbanColumn.DONE -> TaskKanbanColumn.IN_PROGRESS
}

private fun nextColumn(column: TaskKanbanColumn): TaskKanbanColumn = when (column) {
    TaskKanbanColumn.TODO -> TaskKanbanColumn.IN_PROGRESS
    TaskKanbanColumn.IN_PROGRESS -> TaskKanbanColumn.DONE
    TaskKanbanColumn.DONE -> TaskKanbanColumn.DONE
}
