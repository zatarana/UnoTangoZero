package com.unotangozero.app.presentation.goals

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.round

data class GoalUi(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetValueInCents: Long?,
    val deadline: LocalDate,
    val colorHex: String,
    val createdAt: LocalDate = LocalDate.now()
)

data class GoalFormUiState(
    val title: String = "",
    val description: String = "",
    val targetValueText: String = "",
    val deadline: LocalDate = LocalDate.now().plusMonths(1),
    val colorHex: String = "#6750A4"
)

@HiltViewModel
class GoalsViewModel @Inject constructor() : ViewModel() {
    private val _goals = MutableStateFlow<List<GoalUi>>(emptyList())
    val goals: StateFlow<List<GoalUi>> = _goals.asStateFlow()

    private val _formState = MutableStateFlow(GoalFormUiState())
    val formState: StateFlow<GoalFormUiState> = _formState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTitleChange(value: String) {
        _formState.value = _formState.value.copy(title = value)
    }

    fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun onTargetValueChange(value: String) {
        _formState.value = _formState.value.copy(targetValueText = value.filterMoneyChars())
    }

    fun onDeadlineChange(value: LocalDate) {
        _formState.value = _formState.value.copy(deadline = value)
    }

    fun onColorChange(value: String) {
        _formState.value = _formState.value.copy(colorHex = value)
    }

    fun createGoal() {
        val state = _formState.value
        val title = state.title.trim()
        val description = state.description.trim()
        val targetValue = state.targetValueText.takeIf { it.isNotBlank() }?.let { parseMoneyToCents(it) }

        if (title.isBlank()) {
            _message.value = "Digite o título da meta."
            return
        }

        if (state.targetValueText.isNotBlank() && (targetValue == null || targetValue <= 0L)) {
            _message.value = "Digite um valor alvo válido ou deixe em branco."
            return
        }

        _goals.value = _goals.value + GoalUi(
            title = title,
            description = description,
            targetValueInCents = targetValue,
            deadline = state.deadline,
            colorHex = state.colorHex
        )
        _formState.value = GoalFormUiState()
        _message.value = "Meta criada."
    }

    fun deleteGoal(goalId: String) {
        _goals.value = _goals.value.filterNot { it.id == goalId }
        _message.value = "Meta excluída."
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun String.filterMoneyChars(): String = filter { it.isDigit() || it == ',' || it == '.' }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
