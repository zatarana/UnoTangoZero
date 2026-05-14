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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.round

data class BudgetUiState(
    val yearMonth: String = YearMonth.now().toString(),
    val summary: MonthlyBudgetSummary? = null,
    val currentMonthEnvelopes: List<BudgetEnvelope> = emptyList()
)

data class BudgetEnvelopeFormUiState(
    val editingEnvelope: BudgetEnvelope? = null,
    val category: String = "",
    val plannedAmountText: String = "",
    val rolloverEnabled: Boolean = false
) {
    val isEditing: Boolean = editingEnvelope != null
}

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val envelopeBudgetRepository: EnvelopeBudgetRepository
) : ViewModel() {
    val uiState: StateFlow<BudgetUiState> = combine(
        envelopeBudgetRepository.envelopes,
        envelopeBudgetRepository.currentMonthSummary
    ) { envelopes, summary ->
        val currentMonth = YearMonth.now().toString()
        BudgetUiState(
            yearMonth = currentMonth,
            summary = summary,
            currentMonthEnvelopes = envelopes.filter { it.yearMonth == currentMonth }.sortedBy { it.category }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BudgetUiState()
    )

    private val _formState = MutableStateFlow(BudgetEnvelopeFormUiState())
    val formState: StateFlow<BudgetEnvelopeFormUiState> = _formState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onCategoryChange(value: String) {
        _formState.value = _formState.value.copy(category = value)
    }

    fun onPlannedAmountChange(value: String) {
        _formState.value = _formState.value.copy(plannedAmountText = value.filterMoneyChars())
    }

    fun onRolloverChange(value: Boolean) {
        _formState.value = _formState.value.copy(rolloverEnabled = value)
    }

    fun startEditing(envelope: BudgetEnvelope) {
        _formState.value = BudgetEnvelopeFormUiState(
            editingEnvelope = envelope,
            category = envelope.category,
            plannedAmountText = centsToMoneyText(envelope.allocatedAmountInCents),
            rolloverEnabled = envelope.rolloverEnabled
        )
    }

    fun cancelEditing() {
        _formState.value = BudgetEnvelopeFormUiState()
    }

    fun saveEnvelope() {
        val state = _formState.value
        val category = state.category.trim()
        val amountInCents = parseMoneyToCents(state.plannedAmountText)

        if (category.isBlank()) {
            _message.value = "Digite a categoria do envelope."
            return
        }

        if (amountInCents <= 0L) {
            _message.value = "Digite um valor planejado maior que zero."
            return
        }

        val currentMonth = YearMonth.now().toString()
        val existingSameCategory = uiState.value.currentMonthEnvelopes.firstOrNull {
            it.category.trim().equals(category, ignoreCase = true)
        }
        val editingEnvelope = state.editingEnvelope
        val envelopeId = editingEnvelope?.id ?: existingSameCategory?.id

        val envelope = BudgetEnvelope(
            id = envelopeId ?: java.util.UUID.randomUUID().toString(),
            yearMonth = editingEnvelope?.yearMonth ?: existingSameCategory?.yearMonth ?: currentMonth,
            category = category,
            allocatedAmountInCents = amountInCents,
            rolloverEnabled = state.rolloverEnabled
        )

        viewModelScope.launch {
            envelopeBudgetRepository.saveEnvelope(envelope)
                .onSuccess {
                    _formState.value = BudgetEnvelopeFormUiState()
                    _message.value = if (editingEnvelope != null || existingSameCategory != null) {
                        "Envelope atualizado."
                    } else {
                        "Envelope criado."
                    }
                }
                .onFailure { error ->
                    _message.value = error.message ?: "Não foi possível salvar o envelope."
                }
        }
    }

    fun deleteEnvelope(envelope: BudgetEnvelope) {
        viewModelScope.launch {
            envelopeBudgetRepository.deleteEnvelope(envelope.id)
                .onSuccess {
                    if (_formState.value.editingEnvelope?.id == envelope.id) cancelEditing()
                    _message.value = "Envelope excluído."
                }
                .onFailure { error ->
                    _message.value = error.message ?: "Não foi possível excluir o envelope."
                }
        }
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

    private fun centsToMoneyText(amountInCents: Long): String {
        return "%.2f".format(amountInCents / 100.0).replace('.', ',')
    }
}
