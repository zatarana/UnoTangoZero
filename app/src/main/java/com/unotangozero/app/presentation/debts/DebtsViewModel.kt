package com.unotangozero.app.presentation.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtSummary
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.repositories.DebtRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import javax.inject.Inject
import kotlin.math.round

data class DebtEditorUiState(
    val editingDebt: Debt? = null,
    val creditor: String = "",
    val amountText: String = "",
    val description: String = "",
    val dueDate: LocalDate = LocalDate.now().plusMonths(1)
) {
    val isEditing: Boolean = editingDebt != null
}

@HiltViewModel
class DebtsViewModel @Inject constructor(
    private val debtRepository: DebtRepository,
    accountRepository: FinancialAccountRepository,
    private val movementRepository: FinancialMovementRepository
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

    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.accounts
        .map { list -> list.filter { !it.isArchived } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editorState = MutableStateFlow(DebtEditorUiState())
    val editorState: StateFlow<DebtEditorUiState> = _editorState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onCreditorChange(value: String) {
        _editorState.value = _editorState.value.copy(creditor = value)
    }

    fun onAmountChange(value: String) {
        _editorState.value = _editorState.value.copy(amountText = value.filterMoneyChars())
    }

    fun onDescriptionChange(value: String) {
        _editorState.value = _editorState.value.copy(description = value)
    }

    fun previousDueDay() {
        _editorState.value = _editorState.value.copy(dueDate = _editorState.value.dueDate.minusDays(1))
    }

    fun nextDueDay() {
        _editorState.value = _editorState.value.copy(dueDate = _editorState.value.dueDate.plusDays(1))
    }

    fun onDueDateSelected(date: LocalDate) {
        _editorState.value = _editorState.value.copy(dueDate = date)
    }

    fun startEditing(debt: Debt) {
        _editorState.value = DebtEditorUiState(
            editingDebt = debt,
            creditor = debt.creditor,
            amountText = centsToMoneyText(if (debt.status == DebtStatus.PAID) debt.originalAmountInCents else debt.remainingAmountInCents),
            description = debt.description.orEmpty(),
            dueDate = debt.dueDate
        )
    }

    fun cancelEditing() {
        _editorState.value = DebtEditorUiState()
    }

    fun saveDebtFromEditor() {
        val state = _editorState.value
        val creditor = state.creditor.trim()
        val amountInCents = parseMoneyToCents(state.amountText)
        val description = state.description.trim().ifBlank { null }

        if (creditor.isBlank()) {
            _message.value = "Digite o credor da dívida."
            return
        }

        if (amountInCents <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }

        viewModelScope.launch {
            val existing = state.editingDebt
            val status = existing?.status ?: DebtStatus.PENDING
            val debt = existing?.copy(
                creditor = creditor,
                originalAmountInCents = amountInCents,
                remainingAmountInCents = if (status == DebtStatus.PAID) 0L else amountInCents,
                dueDate = state.dueDate,
                monthlyInterestRate = 0.0,
                interestAccumulatedInCents = 0L,
                status = status,
                description = description,
                updatedAt = LocalDateTime.now()
            ) ?: Debt(
                creditor = creditor,
                originalAmountInCents = amountInCents,
                remainingAmountInCents = amountInCents,
                dueDate = state.dueDate,
                monthlyInterestRate = 0.0,
                interestAccumulatedInCents = 0L,
                status = DebtStatus.PENDING,
                description = description
            )

            debtRepository.save(debt)
                .onSuccess {
                    _editorState.value = DebtEditorUiState()
                    _message.value = if (state.isEditing) "Dívida atualizada." else "Dívida cadastrada."
                }
                .onFailure {
                    _message.value = it.message ?: "Não foi possível salvar a dívida."
                }
        }
    }

    fun markAsPaid(debt: Debt, finalPaidAmountText: String, accountId: String?) {
        val finalPaidAmountInCents = parseMoneyToCents(finalPaidAmountText)
        if (finalPaidAmountInCents <= 0L) {
            _message.value = "Digite o valor final pago."
            return
        }
        if (accountId.isNullOrBlank()) {
            _message.value = "Selecione a conta usada para pagar."
            return
        }

        viewModelScope.launch {
            val paidDebt = debt.copy(
                remainingAmountInCents = 0L,
                interestAccumulatedInCents = 0L,
                monthlyInterestRate = 0.0,
                status = DebtStatus.PAID,
                updatedAt = LocalDateTime.now()
            )
            val payoffMovement = FinancialMovement(
                type = FinancialMovementType.EXPENSE,
                amountInCents = finalPaidAmountInCents,
                date = LocalDate.now(),
                description = "Quitação de dívida - ${debt.creditor}",
                category = "Dívidas",
                accountId = accountId
            )

            val movementResult = movementRepository.addMovement(payoffMovement)
            if (movementResult.isFailure) {
                _message.value = movementResult.exceptionOrNull()?.message ?: "Não foi possível registrar a despesa da quitação."
                return@launch
            }

            val debtResult = debtRepository.save(paidDebt)
            if (debtResult.isSuccess) {
                _message.value = buildPayoffMessage(debt.remainingAmountInCents, finalPaidAmountInCents) + " Despesa registrada em Finanças."
            } else {
                movementRepository.deleteMovement(payoffMovement.id)
                _message.value = debtResult.exceptionOrNull()?.message ?: "Despesa revertida. Não foi possível quitar a dívida."
            }
        }
    }

    fun deleteDebt(debt: Debt) {
        viewModelScope.launch {
            debtRepository.delete(debt.id)
                .onSuccess {
                    if (_editorState.value.editingDebt?.id == debt.id) cancelEditing()
                    _message.value = "Dívida excluída."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir a dívida." }
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

    private fun buildPayoffMessage(expectedAmountInCents: Long, finalPaidAmountInCents: Long): String {
        val difference = expectedAmountInCents - finalPaidAmountInCents
        return when {
            difference > 0L -> "Dívida quitada. Economia de ${formatMoney(difference)}."
            difference < 0L -> "Dívida quitada. Juros/acréscimo de ${formatMoney(-difference)}."
            else -> "Dívida quitada pelo valor previsto."
        }
    }

    private fun centsToMoneyText(amountInCents: Long): String {
        return "%.2f".format(amountInCents / 100.0).replace('.', ',')
    }

    private fun formatMoney(amountInCents: Long): String {
        return NumberFormat.getCurrencyInstance(Locale("pt", "BR")).format(amountInCents / 100.0)
    }
}
