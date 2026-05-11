package com.unotangozero.app.presentation.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.HabitFrequency
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitLog
import com.unotangozero.app.domain.repositories.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class HabitsViewModel @Inject constructor(
    private val habitRepository: HabitRepository
) : ViewModel() {
    val habits: StateFlow<List<Habit>> = habitRepository
        .observeActive()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _frequency = MutableStateFlow(HabitFrequency.DAILY)
    val frequency: StateFlow<HabitFrequency> = _frequency.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun onFrequencyChange(value: HabitFrequency) {
        _frequency.value = value
    }

    fun createHabit() {
        val name = _name.value.trim()
        val description = _description.value.trim().ifBlank { null }

        if (name.isBlank()) {
            _message.value = "Digite o nome do hábito."
            return
        }

        viewModelScope.launch {
            val habit = Habit(
                name = name,
                description = description,
                frequency = _frequency.value
            )

            habitRepository.save(habit)
                .onSuccess {
                    _name.value = ""
                    _description.value = ""
                    _frequency.value = HabitFrequency.DAILY
                    _message.value = "Hábito criado."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível criar o hábito."
                }
        }
    }

    fun completeToday(habit: Habit) {
        viewModelScope.launch {
            val log = HabitLog(
                habitId = habit.id,
                completedDate = LocalDate.now()
            )

            habitRepository.log(log)
                .onSuccess { _message.value = "Conclusão registrada para hoje." }
                .onFailure { _message.value = it.message ?: "Não foi possível registrar o hábito." }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitRepository.delete(habit.id)
                .onSuccess { _message.value = "Hábito excluído." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir o hábito." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
