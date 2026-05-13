package com.unotangozero.app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.tasks.FocusSessionRepository
import com.unotangozero.app.domain.models.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FocusSummaryViewModel @Inject constructor(
    focusSessionRepository: FocusSessionRepository
) : ViewModel() {
    val focusSecondsByTask: StateFlow<Map<String, Int>> = focusSessionRepository.focusSecondsByTask
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())
}

@Composable
fun FocusSummaryCard(
    tasks: List<Task>,
    viewModel: FocusSummaryViewModel = hiltViewModel()
) {
    val focusSecondsByTask by viewModel.focusSecondsByTask.collectAsState()
    val existingTaskIds = remember(tasks) { tasks.map { it.id }.toSet() }
    val visibleTotals = remember(focusSecondsByTask, existingTaskIds) {
        focusSecondsByTask.filterKeys { it in existingTaskIds }
    }
    val totalSeconds = visibleTotals.values.sum()
    val topEntry = visibleTotals.maxByOrNull { it.value }
    val topTask = topEntry?.let { entry -> tasks.firstOrNull { it.id == entry.key } }
    val activeTasksWithFocus = visibleTotals.count { it.value > 0 }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Resumo de foco", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                text = formatFocusCompact(totalSeconds),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "$activeTasksWithFocus tarefa(s) com tempo registrado",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            topTask?.let { task ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mais focada", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(task.title, fontWeight = FontWeight.Bold)
                }
                Text("Tempo nela: ${formatFocusCompact(topEntry.value)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                val progress = if (totalSeconds > 0) topEntry.value.toFloat() / totalSeconds.toFloat() else 0f
                LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, modifier = Modifier.fillMaxWidth())
            } ?: Text(
                text = "Use o botão Focar em uma tarefa para começar a registrar tempo.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatFocusCompact(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        totalSeconds > 0 -> "${totalSeconds}s"
        else -> "0min"
    }
}
