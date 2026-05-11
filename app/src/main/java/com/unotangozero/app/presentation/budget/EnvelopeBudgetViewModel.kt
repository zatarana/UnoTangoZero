package com.unotangozero.app.presentation.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.budget.EnvelopeBudgetRepository
import com.unotangozero.app.domain.models.BudgetEnvelope
import com.unotangozero.app.domain.models.MonthlyBudgetSummary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.round

data class EnvelopeBudgetFormState(
    val category: String = "",
    val amountText: String = "",
    val rolloverEnabled: Boolean = false
)

@HiltViewModel
class EnvelopeBudgetViewModel @Inject constructor(
    private val repository: EnvelopeBudgetRepository
) : ViewModel() {
    val summary: StateFlow<MonthlyBudgetSummary> = repository.currentMonthSummary
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MonthlyBudgetSummary(
                yearMonth = YearMonth.now().toString(),
                totalIncomeInCents = 0L,
                totalAllocatedInCents = 0L,
                totalSpentInCents = 0L,
                envelopes = emptyList()
            )
        )

    private val _form = MutableStateFlow(EnvelopeBudgetFormState())
    val form: StateFlow<EnvelopeBudgetFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onCategoryChange(value: String) { _form.value = _form.value.copy(category = value) }
    fun onAmountChange(value: String) { _form.value = _form.value.copy(amountText = value.filter { it.isDigit() || it == ',' || it == '.' }) }
    fun onRolloverChange(value: Boolean) { _form.value = _form.value.copy(rolloverEnabled = value) }

    fun saveEnvelope() {
        val state = _form.value
        val amount = parseMoneyToCents(state.amountText)
        if (state.category.trim().isBlank()) {
            _message.value = "Digite uma categoria."
            return
        }
        if (amount <= 0L) {
            _message.value = "Digite um valor orçado maior que zero."
            return
        }
        viewModelScope.launch {
            repository.saveEnvelope(
                BudgetEnvelope(
                    yearMonth = YearMonth.now().toString(),
                    category = state.category.trim().lowercase(),
                    allocatedAmountInCents = amount,
                    rolloverEnabled = state.rolloverEnabled
                )
            ).onSuccess {
                _form.value = EnvelopeBudgetFormState()
                _message.value = "Envelope criado."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível salvar."
            }
        }
    }

    fun deleteEnvelope(envelopeId: String) {
        viewModelScope.launch {
            repository.deleteEnvelope(envelopeId)
                .onSuccess { _message.value = "Envelope removido." }
                .onFailure { _message.value = it.message ?: "Não foi possível remover." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
