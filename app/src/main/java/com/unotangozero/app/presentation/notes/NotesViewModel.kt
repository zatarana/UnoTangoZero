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
import javax.inject.Inject

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

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _content = MutableStateFlow("")
    val content: StateFlow<String> = _content.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }

    fun onTitleChange(value: String) {
        _title.value = value
    }

    fun onContentChange(value: String) {
        _content.value = value
    }

    fun createNote() {
        val title = _title.value.trim()
        val content = _content.value.trim()

        if (title.isBlank()) {
            _message.value = "Digite um título para a nota."
            return
        }

        if (content.isBlank()) {
            _message.value = "Digite o conteúdo da nota."
            return
        }

        viewModelScope.launch {
            val note = Note(
                title = title,
                content = content
            )

            noteRepository.save(note)
                .onSuccess {
                    _title.value = ""
                    _content.value = ""
                    _message.value = "Nota criada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar a nota."
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
                .onSuccess { _message.value = "Nota excluída." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a nota." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
