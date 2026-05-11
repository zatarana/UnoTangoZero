package com.unotangozero.app.presentation.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.models.Note
import com.unotangozero.app.domain.repositories.NoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

data class NoteEditorUiState(
    val editingNote: Note? = null,
    val title: String = "",
    val content: String = ""
) {
    val isEditing: Boolean = editingNote != null
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val notes: StateFlow<List<Note>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.observeAll()
            } else {
                noteRepository.search(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _editorState = MutableStateFlow(NoteEditorUiState())
    val editorState: StateFlow<NoteEditorUiState> = _editorState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun onTitleChange(value: String) {
        _editorState.value = _editorState.value.copy(title = value)
    }

    fun onContentChange(value: String) {
        _editorState.value = _editorState.value.copy(content = value)
    }

    fun startEditing(note: Note) {
        _editorState.value = NoteEditorUiState(
            editingNote = note,
            title = note.title,
            content = note.content
        )
    }

    fun cancelEditing() {
        _editorState.value = NoteEditorUiState()
    }

    fun saveNoteFromEditor() {
        val state = _editorState.value
        val title = state.title.trim()
        val content = state.content.trim()

        if (title.isBlank()) {
            _message.value = "Digite um título para a nota."
            return
        }

        if (content.isBlank()) {
            _message.value = "Digite o conteúdo da nota."
            return
        }

        viewModelScope.launch {
            val note = state.editingNote?.copy(
                title = title,
                content = content,
                updatedAt = LocalDateTime.now()
            ) ?: Note(
                title = title,
                content = content
            )

            noteRepository.save(note)
                .onSuccess {
                    _editorState.value = NoteEditorUiState()
                    _message.value = if (state.isEditing) "Nota atualizada." else "Nota criada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível salvar a nota."
                }
        }
    }

    fun togglePinned(note: Note) {
        viewModelScope.launch {
            noteRepository.setPinned(note.id, !note.isPinned)
                .onFailure {
                    _message.value = it.message ?: "Não foi possível atualizar a nota."
                }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepository.delete(note.id)
                .onSuccess {
                    if (_editorState.value.editingNote?.id == note.id) cancelEditing()
                    _message.value = "Nota excluída."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a nota." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
