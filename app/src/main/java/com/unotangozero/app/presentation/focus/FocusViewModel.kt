package com.unotangozero.app.presentation.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.focus.FocusRepository
import com.unotangozero.app.data.projects.ProjectRepository
import com.unotangozero.app.domain.models.FocusPhase
import com.unotangozero.app.domain.models.FocusProfile
import com.unotangozero.app.domain.models.FocusProjectSummary
import com.unotangozero.app.domain.models.FocusSessionLog
import com.unotangozero.app.domain.models.Project
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.max

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val focusRepository: FocusRepository,
    projectRepository: ProjectRepository
) : ViewModel() {
    val profiles: StateFlow<List<FocusProfile>> = focusRepository.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val logs: StateFlow<List<FocusSessionLog>> = focusRepository.logs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projects: StateFlow<List<Project>> = projectRepository.projects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val projectSummaries: StateFlow<List<FocusProjectSummary>> = focusRepository.logs
        .map { logsList ->
            logsList
                .groupBy { it.projectId to (it.projectTitle ?: "Sem projeto") }
                .map { (key, items) ->
                    FocusProjectSummary(
                        projectId = key.first,
                        projectTitle = key.second,
                        totalMinutes = items.sumOf { it.focusedMinutes },
                        sessionCount = items.size
                    )
                }
                .sortedByDescending { it.totalMinutes }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedProfileId = MutableStateFlow("classic")
    val selectedProfileId: StateFlow<String> = _selectedProfileId.asStateFlow()

    private val _selectedProjectId = MutableStateFlow<String?>(null)
    val selectedProjectId: StateFlow<String?> = _selectedProjectId.asStateFlow()

    private val _taskName = MutableStateFlow("")
    val taskName: StateFlow<String> = _taskName.asStateFlow()

    private val _phase = MutableStateFlow(FocusPhase.IDLE)
    val phase: StateFlow<FocusPhase> = _phase.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

    private val _currentCycle = MutableStateFlow(0)
    val currentCycle: StateFlow<Int> = _currentCycle.asStateFlow()

    private val _focusedSeconds = MutableStateFlow(0)
    val focusedSeconds: StateFlow<Int> = _focusedSeconds.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private var timerJob: Job? = null

    fun selectProfile(profileId: String) {
        if (_isRunning.value) return
        _selectedProfileId.value = profileId
    }

    fun selectProject(projectId: String?) {
        if (_isRunning.value) return
        _selectedProjectId.value = projectId
    }

    fun onTaskNameChange(value: String) {
        if (_isRunning.value) return
        _taskName.value = value
    }

    fun start(profile: FocusProfile?) {
        val selected = profile ?: return
        if (_isRunning.value) return
        if (_taskName.value.trim().isBlank()) {
            _message.value = "Digite o nome da tarefa em foco."
            return
        }
        _phase.value = FocusPhase.FOCUS
        _currentCycle.value = 1
        _focusedSeconds.value = 0
        _remainingSeconds.value = selected.focusMinutes * 60
        _isRunning.value = true
        runTimer(selected)
    }

    fun pause() {
        timerJob?.cancel()
        _isRunning.value = false
    }

    fun resume(profile: FocusProfile?) {
        val selected = profile ?: return
        if (_phase.value == FocusPhase.IDLE || _phase.value == FocusPhase.FINISHED) return
        if (_isRunning.value) return
        _isRunning.value = true
        runTimer(selected)
    }

    fun stop(profile: FocusProfile?) {
        timerJob?.cancel()
        viewModelScope.launch { saveCurrentLog(profile) }
        _isRunning.value = false
        _phase.value = FocusPhase.IDLE
        _remainingSeconds.value = 0
        _currentCycle.value = 0
    }

    fun skipBreak(profile: FocusProfile?) {
        val selected = profile ?: return
        if (_phase.value == FocusPhase.SHORT_BREAK || _phase.value == FocusPhase.LONG_BREAK) {
            timerJob?.cancel()
            startNextFocusOrFinish(selected)
        }
    }

    fun extendFocus(minutes: Int = 5) {
        if (_phase.value == FocusPhase.FOCUS) {
            _remainingSeconds.value += minutes * 60
        }
    }

    fun clearMessage() { _message.value = null }

    private fun runTimer(profile: FocusProfile) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value && _remainingSeconds.value > 0) {
                delay(1_000)
                _remainingSeconds.value = max(0, _remainingSeconds.value - 1)
                if (_phase.value == FocusPhase.FOCUS) _focusedSeconds.value += 1
            }
            if (_isRunning.value) handlePhaseFinished(profile)
        }
    }

    private fun handlePhaseFinished(profile: FocusProfile) {
        when (_phase.value) {
            FocusPhase.FOCUS -> {
                if (_currentCycle.value >= profile.totalCycles) {
                    finishSession(profile)
                } else {
                    val shouldLongBreak = profile.longBreakMinutes > 0 && _currentCycle.value % profile.cyclesUntilLongBreak == 0
                    _phase.value = if (shouldLongBreak) FocusPhase.LONG_BREAK else FocusPhase.SHORT_BREAK
                    _remainingSeconds.value = if (shouldLongBreak) profile.longBreakMinutes * 60 else profile.shortBreakMinutes * 60
                    runTimer(profile)
                }
            }
            FocusPhase.SHORT_BREAK, FocusPhase.LONG_BREAK -> startNextFocusOrFinish(profile)
            else -> Unit
        }
    }

    private fun startNextFocusOrFinish(profile: FocusProfile) {
        if (_currentCycle.value >= profile.totalCycles) {
            finishSession(profile)
            return
        }
        _currentCycle.value += 1
        _phase.value = FocusPhase.FOCUS
        _remainingSeconds.value = profile.focusMinutes * 60
        _isRunning.value = true
        runTimer(profile)
    }

    private fun finishSession(profile: FocusProfile) {
        timerJob?.cancel()
        viewModelScope.launch { saveCurrentLog(profile) }
        _isRunning.value = false
        _phase.value = FocusPhase.FINISHED
        _remainingSeconds.value = 0
        _message.value = "Sessão de foco finalizada."
    }

    private suspend fun saveCurrentLog(profile: FocusProfile?) {
        val selected = profile ?: return
        val minutes = _focusedSeconds.value / 60
        if (minutes <= 0) return
        val project = projects.value.firstOrNull { it.id == _selectedProjectId.value }
        focusRepository.addLog(
            FocusSessionLog(
                taskName = _taskName.value.trim(),
                profileName = selected.name,
                focusedMinutes = minutes,
                completedCycles = _currentCycle.value,
                projectId = project?.id,
                projectTitle = project?.title
            )
        )
    }
}
