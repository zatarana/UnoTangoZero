package com.unotangozero.app.presentation.focusmode

import androidx.compose.foundation.layout.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusModeScreen(
    uiState: FocusModeUiState,
    onDateFilterChange: (FocusDateFilter) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onCompleteTask: (Task) -> Unit
) {
    var isFilterOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, top = 22.dp, end = 20.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Modo Foco", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Uma visão limpa apenas com as tarefas que você quer atacar agora.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            item { FocusSummaryCard(uiState) }

            item {
                Text(
                    "${uiState.tasks.size} tarefa(s) para focar",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
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

        FloatingActionButton(
            onClick = { isFilterOpen = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.MoreHoriz, contentDescription = "Filtros")
        }
    }

    if (isFilterOpen) {
        ModalBottomSheet(
            onDismissRequest = { isFilterOpen = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background
        ) {
            FilterPanel(
                uiState = uiState,
                onDateFilterChange = onDateFilterChange,
                onPriorityFilterChange = onPriorityFilterChange,
                onTagFilterChange = onTagFilterChange,
                onClearFilters = onClearFilters,
                onClose = { isFilterOpen = false }
            )
        }
    }
}

@Composable
private fun FocusSummaryCard(uiState: FocusModeUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Filtro atual", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                "${uiState.filters.dateFilter.displayName} • ${uiState.filters.selectedPriority?.displayName ?: "Todas as prioridades"}${uiState.filters.selectedTag?.let { " • #$it" } ?: ""}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("Use o botão no canto para ajustar o foco.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FilterPanel(
    uiState: FocusModeUiState,
    onDateFilterChange: (FocusDateFilter) -> Unit,
    onPriorityFilterChange: (Priority?) -> Unit,
    onTagFilterChange: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onClose: () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Filtros do foco", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)

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
        Button(modifier = Modifier.fillMaxWidth(), onClick = onClose) {
            Text("Aplicar")
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
            Text(task.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
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
            Text("Nenhuma tarefa para este foco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text("Ajuste os filtros ou cadastre novas tarefas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
