package com.unotangozero.app.presentation.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.unotangozero.app.backup.BackupFile
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupRoute(viewModel: BackupViewModel = hiltViewModel()) {
    val backups by viewModel.backups.collectAsState()
    val message by viewModel.message.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
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
        BackupScreen(
            backups = backups,
            isLoading = isLoading,
            onExport = viewModel::exportBackup,
            onRestoreLatest = viewModel::restoreLatestBackup
        )
    }
}

@Composable
fun BackupScreen(
    backups: List<BackupFile>,
    isLoading: Boolean,
    onExport: () -> Unit,
    onRestoreLatest: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Backup", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    "Exporte uma cópia local do banco de dados e restaure o backup mais recente quando necessário.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(modifier = Modifier.fillMaxWidth(), enabled = !isLoading, onClick = onExport) {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Text(" Criar backup local")
                    }
                    OutlinedButton(modifier = Modifier.fillMaxWidth(), enabled = !isLoading && backups.isNotEmpty(), onClick = onRestoreLatest) {
                        Icon(Icons.Default.Restore, contentDescription = null)
                        Text(" Restaurar backup mais recente")
                    }
                    Text(
                        "Os backups ficam na pasta local de documentos do app. Após restaurar, reinicie o app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text("Backups encontrados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }

        if (backups.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Nenhum backup ainda", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text("Crie um backup para proteger seus dados locais.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(backups, key = { it.path }) { backup -> BackupCard(backup) }
        }
    }
}

@Composable
private fun BackupCard(backup: BackupFile) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()) }
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(backup.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(formatter.format(Date(backup.lastModified)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatSize(backup.sizeBytes), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

private fun formatSize(sizeBytes: Long): String {
    return when {
        sizeBytes >= 1024 * 1024 -> "%.2f MB".format(sizeBytes / 1024.0 / 1024.0)
        sizeBytes >= 1024 -> "%.2f KB".format(sizeBytes / 1024.0)
        else -> "$sizeBytes B"
    }
}
