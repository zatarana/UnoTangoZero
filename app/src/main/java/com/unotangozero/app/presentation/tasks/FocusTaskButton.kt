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
import com.unotangozero.app.domain.models.Task
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun FocusTaskButton(task: Task, modifier: Modifier = Modifier) {
    var isOpen by remember { mutableStateOf(false) }

    OutlinedButton(
        modifier = modifier,
        onClick = { isOpen = true }
    ) {
        Text("Focar")
    }

    if (isOpen) {
        FocusTaskDialog(
            task = task,
            onDismiss = { isOpen = false }
        )
    }
}

@Composable
private fun FocusTaskDialog(task: Task, onDismiss: () -> Unit) {
    var elapsedSeconds by remember(task.id) { mutableIntStateOf(0) }
    var isRunning by remember(task.id) { mutableStateOf(false) }

    LaunchedEffect(task.id, isRunning) {
        while (isRunning) {
            delay(1_000)
            elapsedSeconds += 1
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Foco em ${task.title}") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = formatFocusSeconds(elapsedSeconds),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (isRunning) "Sessão em andamento" else "Cronômetro pausado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { isRunning = !isRunning }
                ) {
                    Text(if (isRunning) "Pausar" else "Iniciar")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Encerrar") }
        },
        dismissButton = {
            TextButton(onClick = { isRunning = false }) { Text("Pausar") }
        }
    )
}

private fun formatFocusSeconds(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(Locale("pt", "BR"), hours, minutes, seconds)
}
