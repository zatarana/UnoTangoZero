package com.unotangozero.app.presentation.bills

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.data.bills.PlannedBillRepository
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.PlannedBill
import com.unotangozero.app.domain.models.PlannedBillType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.round

data class BillFormState(
    val type: PlannedBillType = PlannedBillType.PAYABLE,
    val description: String = "",
    val amountText: String = "",
    val category: String = "",
    val dueDate: LocalDate = LocalDate.now(),
    val selectedAccountId: String? = null
)

data class BillsUiState(
    val bills: List<PlannedBill> = emptyList(),
    val accounts: List<FinancialAccount> = emptyList(),
    val dueNext7Days: Long = 0L,
    val dueNext30Days: Long = 0L
)

@HiltViewModel
class BillsViewModel @Inject constructor(
    private val plannedBillRepository: PlannedBillRepository,
    accountRepository: FinancialAccountRepository
) : ViewModel() {
    val uiState: StateFlow<BillsUiState> = combine(
        plannedBillRepository.bills,
        accountRepository.accounts
    ) { bills, accounts ->
        val today = LocalDate.now()
        val openBills = bills.filter { !it.isPaid }
        BillsUiState(
            bills = bills,
            accounts = accounts.filter { !it.isArchived },
            dueNext7Days = openBills.filter { !it.dueDate.isBefore(today) && !it.dueDate.isAfter(today.plusDays(7)) }.sumOf { signedAmount(it) },
            dueNext30Days = openBills.filter { !it.dueDate.isBefore(today) && !it.dueDate.isAfter(today.plusDays(30)) }.sumOf { signedAmount(it) }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BillsUiState())

    private val _form = MutableStateFlow(BillFormState())
    val form: StateFlow<BillFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTypeChange(type: PlannedBillType) { _form.value = _form.value.copy(type = type) }
    fun onDescriptionChange(value: String) { _form.value = _form.value.copy(description = value) }
    fun onAmountChange(value: String) { _form.value = _form.value.copy(amountText = value.filter { it.isDigit() || it == ',' || it == '.' }) }
    fun onCategoryChange(value: String) { _form.value = _form.value.copy(category = value) }
    fun onAccountChange(accountId: String?) { _form.value = _form.value.copy(selectedAccountId = accountId) }
    fun previousDay() { _form.value = _form.value.copy(dueDate = _form.value.dueDate.minusDays(1)) }
    fun nextDay() { _form.value = _form.value.copy(dueDate = _form.value.dueDate.plusDays(1)) }

    fun saveBill() {
        val state = _form.value
        val amount = parseMoneyToCents(state.amountText)
        if (state.description.trim().isBlank()) {
            _message.value = "Digite uma descrição."
            return
        }
        if (amount <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }
        viewModelScope.launch {
            plannedBillRepository.saveBill(
                PlannedBill(
                    type = state.type,
                    description = state.description.trim(),
                    amountInCents = amount,
                    dueDate = state.dueDate,
                    category = state.category.trim().ifBlank { null }
                )
            ).onSuccess {
                _form.value = BillFormState(type = state.type)
                _message.value = "Conta planejada salva."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível salvar."
            }
        }
    }

    fun markAsPaid(bill: PlannedBill) {
        val accountId = _form.value.selectedAccountId ?: uiState.value.accounts.firstOrNull()?.id
        if (accountId == null) {
            _message.value = "Cadastre ou selecione uma conta."
            return
        }
        viewModelScope.launch {
            plannedBillRepository.markAsPaid(bill, accountId)
                .onSuccess { _message.value = if (bill.type == PlannedBillType.PAYABLE) "Conta paga e despesa gerada." else "Recebimento registrado." }
                .onFailure { _message.value = it.message ?: "Não foi possível registrar." }
        }
    }

    fun deleteBill(bill: PlannedBill) {
        viewModelScope.launch {
            plannedBillRepository.deleteBill(bill.id)
                .onSuccess { _message.value = "Conta removida." }
                .onFailure { _message.value = it.message ?: "Não foi possível remover." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }

    private fun signedAmount(bill: PlannedBill): Long {
        return if (bill.type == PlannedBillType.PAYABLE) -bill.amountInCents else bill.amountInCents
    }
}
