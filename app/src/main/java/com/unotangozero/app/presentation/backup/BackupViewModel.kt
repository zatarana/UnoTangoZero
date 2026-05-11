package com.unotangozero.app.presentation.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.backup.BackupFile
import com.unotangozero.app.backup.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BackupViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {
    private val _backups = MutableStateFlow<List<BackupFile>>(emptyList())
    val backups: StateFlow<List<BackupFile>> = _backups.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        refreshBackups()
    }

    fun refreshBackups() {
        viewModelScope.launch {
            _backups.value = backupManager.listBackups()
        }
    }

    fun exportBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            backupManager.exportBackup()
                .onSuccess {
                    _message.value = "Backup criado: ${it.name}"
                    _backups.value = backupManager.listBackups()
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar o backup."
                }
            _isLoading.value = false
        }
    }

    fun restoreLatestBackup() {
        viewModelScope.launch {
            _isLoading.value = true
            backupManager.restoreLatestBackup()
                .onSuccess {
                    _message.value = "Backup restaurado. Reinicie o app para recarregar os dados."
                    _backups.value = backupManager.listBackups()
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível restaurar o backup."
                }
            _isLoading.value = false
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
