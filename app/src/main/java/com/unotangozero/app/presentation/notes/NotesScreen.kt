package com.unotangozero.app.presentation.notes

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unotangozero.app.domain.models.Note
import java.time.format.DateTimeFormatter

@Composable
fun NotesRoute(viewModel: NotesViewModel = hiltViewModel()) {
    val notes by viewModel.notes.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
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
        NotesScreen(
            notes = notes,
            searchQuery = query,
            editorState = editorState,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onTitleChange = viewModel::onTitleChange,
            onContentChange = viewModel::onContentChange,
            onSaveNote = viewModel::saveNoteFromEditor,
            onCancelEdit = viewModel::cancelEditing,
            onStartEdit = viewModel::startEditing,
            onTogglePinned = viewModel::togglePinned,
            onDeleteNote = viewModel::deleteNote
        )
    }
}

@Composable
fun NotesScreen(
    notes: List<Note>,
    searchQuery: String,
    editorState: NoteEditorUiState,
    onSearchQueryChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onCancelEdit: () -> Unit,
    onStartEdit: (Note) -> Unit,
    onTogglePinned: (Note) -> Unit,
    onDeleteNote: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notas", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                Text(
                    text = "Crie, edite, fixe e busque notas locais.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            NoteEditorCard(
                state = editorState,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                onSaveNote = onSaveNote,
                onCancelEdit = onCancelEdit
            )
        }

        item {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Buscar notas") },
                singleLine = true
            )
        }

        if (notes.isEmpty()) {
            item { EmptyNotesCard() }
        } else {
            items(items = notes, key = { it.id }) { note ->
                NoteCard(
                    note = note,
                    onStartEdit = onStartEdit,
                    onTogglePinned = onTogglePinned,
                    onDeleteNote = onDeleteNote
                )
            }
        }
    }
}

@Composable
private fun NoteEditorCard(
    state: NoteEditorUiState,
    onTitleChange: (String) -> Unit,
    onContentChange: (String) -> Unit,
    onSaveNote: () -> Unit,
    onCancelEdit: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(if (state.isEditing) "Editar nota" else "Nova nota", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.title, onValueChange = onTitleChange, label = { Text("Título") }, singleLine = true)
            OutlinedTextField(modifier = Modifier.fillMaxWidth(), value = state.content, onValueChange = onContentChange, label = { Text("Conteúdo") }, minLines = 3)
            Button(modifier = Modifier.fillMaxWidth(), onClick = onSaveNote) { Text(if (state.isEditing) "Salvar alterações" else "Criar nota") }
            if (state.isEditing) {
                OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onCancelEdit) { Text("Cancelar edição") }
            }
        }
    }
}

@Composable
private fun EmptyNotesCard() {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Nenhuma nota encontrada", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text("Crie uma nota acima ou ajuste sua busca.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun NoteCard(note: Note, onStartEdit: (Note) -> Unit, onTogglePinned: (Note) -> Unit, onDeleteNote: (Note) -> Unit) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm") }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = note.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Atualizada em ${note.updatedAt.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { onStartEdit(note) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar nota")
            }

            IconButton(onClick = { onTogglePinned(note) }) {
                Icon(
                    imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                    contentDescription = if (note.isPinned) "Desfixar nota" else "Fixar nota"
                )
            }

            IconButton(onClick = { onDeleteNote(note) }) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir nota")
            }
        }
    }
}
