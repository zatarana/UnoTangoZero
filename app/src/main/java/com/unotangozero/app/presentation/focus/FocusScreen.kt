package com.unotangozero.app.presentation.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.FocusPhase
import com.unotangozero.app.domain.models.FocusProfile
import com.unotangozero.app.domain.models.FocusProjectSummary
import com.unotangozero.app.domain.models.FocusSessionLog
import com.unotangozero.app.domain.models.Project
import java.time.format.DateTimeFormatter

@Composable
fun FocusRoute(viewModel: FocusViewModel = hiltViewModel()) {
    val profiles by viewModel.profiles.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val logs by viewModel.logs.collectAsState()
    val projectSummaries by viewModel.projectSummaries.collectAsState()
    val selectedProfileId by viewModel.selectedProfileId.collectAsState()
    val selectedProjectId by viewModel.selectedProjectId.collectAsState()
    val taskName by viewModel.taskName.collectAsState()
    val phase by viewModel.phase.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    val currentCycle by viewModel.currentCycle.collectAsState()
    val focusedSeconds by viewModel.focusedSeconds.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val message by viewModel.message.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val selectedProfile = profiles.firstOrNull { it.id == selectedProfileId } ?: profiles.firstOrNull()

    LaunchedEffect(message) {
        val currentMessage = message
        if (currentMessage != null) {
            snackbarHostState.showSnackbar(currentMessage)
            viewModel.clearMessage()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbarHostState)
        FocusScreen(
            profiles = profiles,
            projects = projects,
            logs = logs,
            projectSummaries = projectSummaries,
            selectedProfile = selectedProfile,
            selectedProfileId = selectedProfileId,
            selectedProjectId = selectedProjectId,
            taskName = taskName,
            phase = phase,
            remainingSeconds = remainingSeconds,
            currentCycle = currentCycle,
            focusedSeconds = focusedSeconds,
            isRunning = isRunning,
            onSelectProfile = viewModel::selectProfile,
            onSelectProject = viewModel::selectProject,
            onTaskNameChange = viewModel::onTaskNameChange,
            onStart = { viewModel.start(selectedProfile) },
            onPause = viewModel::pause,
            onResume = { viewModel.resume(selectedProfile) },
            onStop = { viewModel.stop(selectedProfile) },
            onSkipBreak = { viewModel.skipBreak(selectedProfile) },
            onExtendFocus = { viewModel.extendFocus() }
        )
    }
}

@Composable
fun FocusScreen(
    profiles: List<FocusProfile>,
    projects: List<Project>,
    logs: List<FocusSessionLog>,
    projectSummaries: List<FocusProjectSummary>,
    selectedProfile: FocusProfile?,
    selectedProfileId: String,
    selectedProjectId: String?,
    taskName: String,
    phase: FocusPhase,
    remainingSeconds: Int,
    currentCycle: Int,
    focusedSeconds: Int,
    isRunning: Boolean,
    onSelectProfile: (String) -> Unit,
    onSelectProject: (String?) -> Unit,
    onTaskNameChange: (String) -> Unit,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkipBreak: () -> Unit,
    onExtendFocus: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Foco", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text("Temporizador Pomodoro com registro de esforço por projeto.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = taskName,
                onValueChange = onTaskNameChange,
                label = { Text("Tarefa em foco") },
                singleLine = true,
                enabled = !isRunning && phase != FocusPhase.FOCUS && phase != FocusPhase.SHORT_BREAK && phase != FocusPhase.LONG_BREAK
            )
        }

        item { ChipTitle("Perfil") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(profiles, key = { it.id }) { profile ->
                    FilterChip(
                        selected = selectedProfileId == profile.id,
                        onClick = { onSelectProfile(profile.id) },
                        label = { Text(profile.name) }
                    )
                }
            }
        }

        item { ChipTitle("Projeto") }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedProjectId == null,
                        onClick = { onSelectProject(null) },
                        label = { Text("Sem projeto") }
                    )
                }
                items(projects, key = { it.id }) { project ->
                    FilterChip(
                        selected = selectedProjectId == project.id,
                        onClick = { onSelectProject(project.id) },
                        label = { Text(project.title) }
                    )
                }
            }
        }

        item {
            TimerCard(
                selectedProfile = selectedProfile,
                phase = phase,
                remainingSeconds = remainingSeconds,
                currentCycle = currentCycle,
                focusedSeconds = focusedSeconds,
                isRunning = isRunning,
                onStart = onStart,
                onPause = onPause,
                onResume = onResume,
                onStop = onStop,
                onSkipBreak = onSkipBreak,
                onExtendFocus = onExtendFocus
            )
        }

        if (projectSummaries.isNotEmpty()) {
            item { Text("Tempo por projeto", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
            items(projectSummaries, key = { it.projectId ?: "none" }) { summary -> ProjectSummaryCard(summary) }
        }

        item { Text("Histórico recente", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }

        if (logs.isEmpty()) {
            item { Text("Nenhuma sessão registrada ainda.") }
        } else {
            items(logs.take(10), key = { it.id }) { log -> FocusLogCard(log) }
        }
    }
}

@Composable
private fun ChipTitle(text: String) {
    Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun TimerCard(
    selectedProfile: FocusProfile?,
    phase: FocusPhase,
    remainingSeconds: Int,
    currentCycle: Int,
    focusedSeconds: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    onSkipBreak: () -> Unit,
    onExtendFocus: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(phase.displayName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(formatTime(remainingSeconds), style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
            selectedProfile?.let {
                Text("Ciclo $currentCycle de ${it.totalCycles}")
                Text("Perfil: ${it.focusMinutes} min foco • ${it.shortBreakMinutes} min pausa • ${it.totalCycles} ciclos")
            }
            Text("Tempo focado registrado: ${focusedSeconds / 60} min")

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (phase == FocusPhase.IDLE || phase == FocusPhase.FINISHED) {
                    Button(onClick = onStart) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Iniciar")
                    }
                } else if (isRunning) {
                    Button(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = null)
                        Text(" Pausar")
                    }
                } else {
                    Button(onClick = onResume) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text(" Retomar")
                    }
                }

                OutlinedButton(onClick = onStop, enabled = phase != FocusPhase.IDLE) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Text(" Parar")
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onExtendFocus, enabled = phase == FocusPhase.FOCUS) { Text("+5 min foco") }
                OutlinedButton(onClick = onSkipBreak, enabled = phase == FocusPhase.SHORT_BREAK || phase == FocusPhase.LONG_BREAK) { Text("Pular pausa") }
            }
        }
    }
}

@Composable
private fun ProjectSummaryCard(summary: FocusProjectSummary) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(summary.projectTitle, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${formatMinutes(summary.totalMinutes)} • ${summary.sessionCount} sessão(ões)")
        }
    }
}

@Composable
private fun FocusLogCard(log: FocusSessionLog) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(log.taskName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("${log.focusedMinutes} min • ${log.completedCycles} ciclo(s) • ${log.profileName}")
            Text("Projeto: ${log.projectTitle ?: "Sem projeto"}", style = MaterialTheme.typography.bodySmall)
            Text(log.createdAt.format(formatter), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(minutes, secs)
}

private fun formatMinutes(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}min" else "${minutes}min"
}
