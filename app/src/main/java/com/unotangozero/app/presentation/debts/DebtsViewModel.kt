package com.unotangozero.app.presentation.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtSummary
import com.unotangozero.app.domain.repositories.DebtRepository
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

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepository: DebtRepository
) : ViewModel() {
    val debts: StateFlow<List<Debt>> = debtRepository
        .observeAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val summary: StateFlow<DebtSummary> = debtRepository
        .observeSummary()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DebtSummary()
        )

    private val _creditor = MutableStateFlow("")
    val creditor: StateFlow<String> = _creditor.asStateFlow()

    private val _amountText = MutableStateFlow("")
    val amountText: StateFlow<String> = _amountText.asStateFlow()

    private val _interestText = MutableStateFlow("")
    val interestText: StateFlow<String> = _interestText.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _dueDate = MutableStateFlow(LocalDate.now().plusMonths(1))
    val dueDate: StateFlow<LocalDate> = _dueDate.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onCreditorChange(value: String) {
        _creditor.value = value
    }

    fun onAmountChange(value: String) {
        _amountText.value = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onInterestChange(value: String) {
        _interestText.value = value.filter { it.isDigit() || it == ',' || it == '.' }
    }

    fun onDescriptionChange(value: String) {
        _description.value = value
    }

    fun previousDueDay() {
        _dueDate.value = _dueDate.value.minusDays(1)
    }

    fun nextDueDay() {
        _dueDate.value = _dueDate.value.plusDays(1)
    }

    fun createDebt() {
        val creditor = _creditor.value.trim()
        val amountInCents = parseMoneyToCents(_amountText.value)
        val interest = parsePercent(_interestText.value)
        val description = _description.value.trim().ifBlank { null }

        if (creditor.isBlank()) {
            _message.value = "Digite o credor da dívida."
            return
        }

        if (amountInCents <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }

        viewModelScope.launch {
            val debt = Debt(
                creditor = creditor,
                originalAmountInCents = amountInCents,
                remainingAmountInCents = amountInCents,
                dueDate = _dueDate.value,
                monthlyInterestRate = interest,
                status = DebtStatus.PENDING,
                description = description
            )

            debtRepository.save(debt)
                .onSuccess {
                    _creditor.value = ""
                    _amountText.value = ""
                    _interestText.value = ""
                    _description.value = ""
                    _dueDate.value = LocalDate.now().plusMonths(1)
                    _message.value = "Dívida cadastrada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível cadastrar a dívida."
                }
        }
    }

    fun markAsPaid(debt: Debt) {
        viewModelScope.launch {
            val paidDebt = debt.copy(
                remainingAmountInCents = 0L,
                interestAccumulatedInCents = 0L,
                status = DebtStatus.PAID,
                updatedAt = java.time.LocalDateTime.now()
            )
            debtRepository.save(paidDebt)
                .onSuccess { _message.value = "Dívida marcada como paga." }
                .onFailure { _message.value = it.message ?: "Não foi possível quitar a dívida." }
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.delete(debt.id)
                .onSuccess { _message.value = "Dívida excluída." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a dívida." }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }

    private fun parsePercent(rawValue: String): Double {
        val normalized = rawValue.trim().replace(",", ".")
        return normalized.toDoubleOrNull() ?: 0.0
    }
}
