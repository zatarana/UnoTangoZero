package com.unotangozero.app.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitFrequency
import com.unotangozero.app.domain.repositories.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.Locale
import javax.inject.Inject

enum class HabitTrackingType(val displayName: String) {
    YES_NO("Sim/não"),
    NUMERIC("Numérico")
}

enum class HabitScheduleType(val displayName: String) {
    DAILY("Diário"),
    WEEKLY("Semanal"),
    SPECIFIC_DAYS("Dias específicos")
}

data class HabitFormUiState(
    val name: String = "",
    val description: String = "",
    val trackingType: HabitTrackingType = HabitTrackingType.YES_NO,
    val dailyGoalText: String = "1",
    val scheduleType: HabitScheduleType = HabitScheduleType.DAILY,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val icon: String = "⭐",
    val colorHex: String = "#6750A4",
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0
) {
    val frequency: HabitFrequency
        get() = when (scheduleType) {
            HabitScheduleType.DAILY -> HabitFrequency.DAILY
            HabitScheduleType.WEEKLY,
            HabitScheduleType.SPECIFIC_DAYS -> HabitFrequency.WEEKLY
        }
}

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    val habits: StateFlow<List<Habit>> = habitRepository
        .getAllHabits()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _formState = MutableStateFlow(HabitFormUiState())
    val formState: StateFlow<HabitFormUiState> = _formState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNameChange(value: String) {
        _formState.value = _formState.value.copy(name = value)
    }

    fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun onTrackingTypeChange(value: HabitTrackingType) {
        _formState.value = _formState.value.copy(trackingType = value)
    }

    fun onDailyGoalChange(value: String) {
        _formState.value = _formState.value.copy(dailyGoalText = value.filter { it.isDigit() }.take(4))
    }

    fun onScheduleTypeChange(value: HabitScheduleType) {
        _formState.value = _formState.value.copy(scheduleType = value)
    }

    fun onSelectedDayToggle(dayOfWeek: DayOfWeek) {
        val current = _formState.value.selectedDays
        _formState.value = _formState.value.copy(
            selectedDays = if (dayOfWeek in current) current - dayOfWeek else current + dayOfWeek
        )
    }

    fun onIconChange(value: String) {
        _formState.value = _formState.value.copy(icon = value)
    }

    fun onColorChange(value: String) {
        _formState.value = _formState.value.copy(colorHex = value)
    }

    fun onReminderEnabledChange(value: Boolean) {
        _formState.value = _formState.value.copy(reminderEnabled = value)
    }

    fun onReminderTimeSelected(hour: Int, minute: Int) {
        _formState.value = _formState.value.copy(
            reminderHour = hour.coerceIn(0, 23),
            reminderMinute = minute.coerceIn(0, 59),
            reminderEnabled = true
        )
    }

    fun createHabit() {
        val state = _formState.value
        val name = state.name.trim()
        val dailyGoal = state.dailyGoalText.toIntOrNull() ?: 0

        if (name.isBlank()) {
            _message.value = "Digite o nome do hábito."
            return
        }

        if (state.trackingType == HabitTrackingType.NUMERIC && dailyGoal <= 0) {
            _message.value = "Informe uma meta diária maior que zero."
            return
        }

        if (state.scheduleType == HabitScheduleType.SPECIFIC_DAYS && state.selectedDays.isEmpty()) {
            _message.value = "Escolha pelo menos um dia específico."
            return
        }

        viewModelScope.launch {
            runCatching {
                habitRepository.createHabit(
                    Habit(
                        name = name,
                        description = buildHabitDescription(state),
                        frequency = state.frequency,
                        goal = if (state.trackingType == HabitTrackingType.NUMERIC) dailyGoal else 1,
                        color = state.colorHex,
                        icon = state.icon
                    )
                )
            }.onSuccess {
                _formState.value = HabitFormUiState()
                _message.value = "Hábito criado."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível criar o hábito."
            }
        }
    }

    fun completeToday(habit: Habit) {
        viewModelScope.launch {
            runCatching {
                habitRepository.checkInHabit(habit.id)
            }.onSuccess {
                _message.value = "Hábito concluído. +10 XP!"
            }.onFailure {
                _message.value = it.message ?: "Não foi possível registrar o hábito."
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            runCatching {
                habitRepository.deleteHabit(habit.id)
            }.onSuccess {
                _message.value = "Hábito excluído."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível excluir o hábito."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun buildHabitDescription(state: HabitFormUiState): String {
        val userDescription = state.description.trim()
        val metadata = buildList {
            add("Tipo: ${state.trackingType.displayName}")
            if (state.trackingType == HabitTrackingType.NUMERIC) add("Meta diária: ${state.dailyGoalText.ifBlank { "1" }}")
            add("Frequência: ${state.scheduleType.displayName}")
            if (state.scheduleType == HabitScheduleType.SPECIFIC_DAYS) {
                add("Dias: ${state.selectedDays.sortedBy { it.value }.joinToString(", ") { it.shortPtBr() }}")
            }
            add("Ícone: ${state.icon}")
            add("Cor: ${state.colorHex}")
            if (state.reminderEnabled) {
                add("Lembrete: %02d:%02d".format(Locale("pt", "BR"), state.reminderHour, state.reminderMinute))
            }
        }.joinToString("\n")

        return if (userDescription.isBlank()) metadata else "$userDescription\n\n$metadata"
    }
}

fun DayOfWeek.shortPtBr(): String = when (this) {
    DayOfWeek.MONDAY -> "Seg"
    DayOfWeek.TUESDAY -> "Ter"
    DayOfWeek.WEDNESDAY -> "Qua"
    DayOfWeek.THURSDAY -> "Qui"
    DayOfWeek.FRIDAY -> "Sex"
    DayOfWeek.SATURDAY -> "Sáb"
    DayOfWeek.SUNDAY -> "Dom"
}
