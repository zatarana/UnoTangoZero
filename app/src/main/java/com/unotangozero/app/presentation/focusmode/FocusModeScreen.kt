package com.unotangozero.app.presentation.focusmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.models.Task
import java.time.format.DateTimeFormatter

@Composable
fun FocusModeRoute(viewModel: FocusModeViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
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
        FocusModeScreen(
            uiState = uiState,
            onDateFilterChange = viewModel::onDateFilterChange,
            onPriorityFilterChange = viewModel::onPriorityFilterChange,
            onTagFilterChange = viewModel::onTagFilterChange,
            onClearFilters = viewModel::clearFilters,
            onCompleteTask = viewModel::completeTask
        )
    }
}

@Composable
fun FocusModeScreen(
    uiState: FocusModeUiState,
    onDateFilterChange: (FocusDateFilter) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onCompleteTask: (Task) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Modo Foco", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Uma visão limpa apenas com as tarefas que você quer atacar agora.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            FilterPanel(
                uiState = uiState,
                onDateFilterChange = onDateFilterChange,
                onPriorityFilterChange = onPriorityFilterChange,
                onTagFilterChange = onTagFilterChange,
                onClearFilters = onClearFilters
            )
        }

        item {
            Text(
                "${uiState.tasks.size} tarefa(s) para focar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.tasks.isEmpty()) {
            item { EmptyFocusCard() }
        } else {
            items(uiState.tasks, key = { it.id }) { task ->
                FocusTaskCard(
                    task = task,
                    tags = uiState.tagsByTask[task.id].orEmpty(),
                    estimatedDurationMinutes = uiState.durationsByTask[task.id] ?: task.estimatedDurationMinutes,
                    onCompleteTask = onCompleteTask
                )
            }
        }
    }
}

@Composable
private fun FilterPanel(
    uiState: FocusModeUiState,
    onDateFilterChange: (FocusDateFilter) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Filtros", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Text("Período", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(FocusDateFilter.entries) { filter ->
                    FilterChip(
                        selected = uiState.filters.dateFilter == filter,
                        onClick = { onDateFilterChange(filter) },
                        label = { Text(filter.displayName) }
                    )
                }
            }

            Text("Prioridade", style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = uiState.filters.selectedPriority == null,
                        onClick = { onPriorityFilterChange(null) },
                        label = { Text("Todas") }
                    )
                }
                items(Priority.entries) { priority ->
                    FilterChip(
                        selected = uiState.filters.selectedPriority == priority,
                        onClick = { onPriorityFilterChange(priority) },
                        label = { Text(priority.displayName) }
                    )
                }
            }

            if (uiState.allTags.isNotEmpty()) {
                Text("Tag", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = uiState.filters.selectedTag == null,
                            onClick = { onTagFilterChange(null) },
                            label = { Text("Todas") }
                        )
                    }
                    items(uiState.allTags) { tag ->
                        FilterChip(
                            selected = uiState.filters.selectedTag == tag,
                            onClick = { onTagFilterChange(tag) },
                            label = { Text("#$tag") }
                        )
                    }
                }
            }

            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onClearFilters) {
                Text("Limpar filtros")
            }
        }
    }
}

@Composable
private fun FocusTaskCard(
    task: Task,
    tags: List<String>,
    estimatedDurationMinutes: Int,
    onCompleteTask: (Task) -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy") }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "${task.priority.displayName} • ${task.dueDate.format(formatter)}${durationText(estimatedDurationMinutes)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag -> AssistChip(onClick = {}, label = { Text("#$tag") }) }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Spacer(Modifier.weight(1f))
                Button(onClick = { onCompleteTask(task) }) { Text("Concluir") }
            }
        }
    }
}

@Composable
private fun EmptyFocusCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma tarefa para este foco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Ajuste os filtros ou cadastre novas tarefas.")
        }
    }
}

private fun durationText(totalMinutes: Int): String {
    if (totalMinutes <= 0) return ""
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val formatted = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        else -> "${minutes}min"
    }
    return " • $formatted"
}
