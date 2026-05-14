package com.unotangozero.app.presentation.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.tasks.FocusSessionRepository
import com.unotangozero.app.domain.models.Task
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class FocusTaskViewModel @Inject constructor(
    private val focusSessionRepository: FocusSessionRepository
) : ViewModel() {
    val focusSecondsByTask: StateFlow<Map<String, Int>> = focusSessionRepository.focusSecondsByTask
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyMap())

    fun saveFocusSession(taskId: String, seconds: Int) {
        if (seconds <= 0) return
        viewModelScope.launch {
            focusSessionRepository.addFocusedSeconds(taskId, seconds)
        }
    }
}

@Composable
fun FocusTaskButton(
    task: Task,
    modifier: Modifier = Modifier,
    viewModel: FocusTaskViewModel = hiltViewModel()
) {
    var isOpen by remember { mutableStateOf(false) }
    val focusSecondsByTask by viewModel.focusSecondsByTask.collectAsState()
    val savedSeconds = focusSecondsByTask[task.id] ?: 0

    OutlinedButton(
        modifier = modifier,
        onClick = { isOpen = true }
    ) {
        Text(if (savedSeconds > 0) "Foco • ${formatFocusCompact(savedSeconds)}" else "Foco")
    }

    if (isOpen) {
        FocusTaskDialog(
            task = task,
            savedSeconds = savedSeconds,
            onSave = { seconds -> viewModel.saveFocusSession(task.id, seconds) },
            onDismiss = { isOpen = false }
        )
    }
}

@Composable
private fun FocusTaskDialog(
    task: Task,
    savedSeconds: Int,
    onSave: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var remainingSeconds by remember(task.id) { mutableIntStateOf(POMODORO_SECONDS) }
    var isRunning by remember(task.id) { mutableStateOf(false) }
    var wasSaved by remember(task.id) { mutableStateOf(false) }

    fun saveAndClose() {
        val elapsedSeconds = (POMODORO_SECONDS - remainingSeconds).coerceIn(0, POMODORO_SECONDS)
        if (!wasSaved && elapsedSeconds > 0) {
            onSave(elapsedSeconds)
            wasSaved = true
        }
        isRunning = false
        onDismiss()
    }

    LaunchedEffect(task.id, isRunning, remainingSeconds) {
        while (isRunning && remainingSeconds > 0) {
            delay(1_000)
            remainingSeconds -= 1
        }
        if (isRunning && remainingSeconds == 0 && !wasSaved) {
            onSave(POMODORO_SECONDS)
            wasSaved = true
            isRunning = false
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = { saveAndClose() },
        title = { Text("Foco em ${task.title}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (savedSeconds > 0) {
                    Text(
                        text = "Total salvo: ${formatFocusCompact(savedSeconds)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = formatFocusSeconds(remainingSeconds),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = when {
                        remainingSeconds == POMODORO_SECONDS && !isRunning -> "Pomodoro de 25 minutos pronto"
                        isRunning -> "Pomodoro em andamento"
                        else -> "Pomodoro pausado"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isRunning = !isRunning }
                ) {
                    Text(if (isRunning) "Pausar" else "Iniciar Pomodoro")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { saveAndClose() }) { Text("Encerrar e salvar") }
        },
        dismissButton = {
            TextButton(onClick = { remainingSeconds = POMODORO_SECONDS; isRunning = false }) { Text("Reiniciar") }
        }
    )
}

private const val POMODORO_SECONDS = 25 * 60

private fun formatFocusSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(Locale("pt", "BR"), minutes, seconds)
}

private fun formatFocusCompact(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}min"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}min"
        else -> "${totalSeconds}s"
    }
}
