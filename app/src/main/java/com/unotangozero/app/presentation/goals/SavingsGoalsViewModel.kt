package com.unotangozero.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.goals.SavingsGoalRepository
import com.unotangozero.app.domain.models.SavingsGoal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.round

data class SavingsGoalFormState(
    val name: String = "",
    val targetAmountText: String = "",
    val category: String = "",
    val hasTargetDate: Boolean = false,
    val targetDate: LocalDate = LocalDate.now().plusMonths(1),
    val depositAmountText: String = "",
    val depositNote: String = ""
)

@HiltViewModel
class SavingsGoalsViewModel @Inject constructor(
    private val repository: SavingsGoalRepository
) : ViewModel() {
    val goals: StateFlow<List<SavingsGoal>> = repository.goals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(SavingsGoalFormState())
    val form: StateFlow<SavingsGoalFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onNameChange(value: String) { _form.value = _form.value.copy(name = value) }
    fun onTargetAmountChange(value: String) { _form.value = _form.value.copy(targetAmountText = moneyChars(value)) }
    fun onCategoryChange(value: String) { _form.value = _form.value.copy(category = value) }
    fun onHasTargetDateChange(value: Boolean) { _form.value = _form.value.copy(hasTargetDate = value) }
    fun previousTargetDate() { _form.value = _form.value.copy(targetDate = _form.value.targetDate.minusDays(1)) }
    fun nextTargetDate() { _form.value = _form.value.copy(targetDate = _form.value.targetDate.plusDays(1)) }
    fun onTargetDateSelected(date: LocalDate) { _form.value = _form.value.copy(targetDate = date, hasTargetDate = true) }
    fun onDepositAmountChange(value: String) { _form.value = _form.value.copy(depositAmountText = moneyChars(value)) }
    fun onDepositNoteChange(value: String) { _form.value = _form.value.copy(depositNote = value) }

    fun saveGoal() {
        val state = _form.value
        val target = parseMoneyToCents(state.targetAmountText)
        if (state.name.trim().isBlank()) {
            _message.value = "Digite o nome da meta."
            return
        }
        if (target <= 0L) {
            _message.value = "Digite um valor alvo maior que zero."
            return
        }
        viewModelScope.launch {
            repository.saveGoal(
                SavingsGoal(
                    name = state.name.trim(),
                    targetAmountInCents = target,
                    targetDate = state.targetDate.takeIf { state.hasTargetDate },
                    category = state.category.trim().ifBlank { null }
                )
            ).onSuccess {
                _form.value = SavingsGoalFormState()
                _message.value = "Meta criada."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível salvar a meta."
            }
        }
    }

    fun addDeposit(goal: SavingsGoal) {
        val state = _form.value
        val amount = parseMoneyToCents(state.depositAmountText)
        if (amount <= 0L) {
            _message.value = "Digite um depósito maior que zero."
            return
        }
        viewModelScope.launch {
            repository.addGoalValue(goal, amount, state.depositNote)
                .onSuccess {
                    _form.value = _form.value.copy(depositAmountText = "", depositNote = "")
                    _message.value = "Depósito registrado."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível registrar." }
        }
    }

    fun deleteGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            repository.deleteGoal(goal.id)
                .onSuccess { _message.value = "Meta removida." }
                .onFailure { _message.value = it.message ?: "Não foi possível remover." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun moneyChars(value: String): String = value.filter { it.isDigit() || it == ',' || it == '.' }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
