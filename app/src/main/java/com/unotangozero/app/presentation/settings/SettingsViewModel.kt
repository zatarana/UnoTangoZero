package com.unotangozero.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.settings.AppSettings
import com.unotangozero.app.data.settings.AppSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: AppSettingsRepository
) : ViewModel() {
    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
        }
    }

    fun incrementReminderHour() {
        val current = settings.value
        viewModelScope.launch {
            settingsRepository.setDefaultReminderTime((current.defaultReminderHour + 1) % 24, current.defaultReminderMinute)
        }
    }

    fun decrementReminderHour() {
        val current = settings.value
        viewModelScope.launch {
            settingsRepository.setDefaultReminderTime((current.defaultReminderHour + 23) % 24, current.defaultReminderMinute)
        }
    }

    fun incrementReminderMinute() {
        val current = settings.value
        val next = (current.defaultReminderMinute + 5) % 60
        viewModelScope.launch {
            settingsRepository.setDefaultReminderTime(current.defaultReminderHour, next)
        }
    }

    fun decrementReminderMinute() {
        val current = settings.value
        val next = (current.defaultReminderMinute + 55) % 60
        viewModelScope.launch {
            settingsRepository.setDefaultReminderTime(current.defaultReminderHour, next)
        }
    }
}
