package com.unotangozero.app.presentation.reconciliation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.round

data class ReconciliationFormState(
    val selectedAccountId: String? = null,
    val realBalanceText: String = ""
)

@HiltViewModel
class ReconciliationViewModel @Inject constructor(
    private val movementRepository: FinancialMovementRepository
) : ViewModel() {
    val balances: StateFlow<List<AccountBalance>> = movementRepository.accountBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(ReconciliationFormState())
    val form: StateFlow<ReconciliationFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onAccountChange(accountId: String?) { _form.value = _form.value.copy(selectedAccountId = accountId) }
    fun onRealBalanceChange(value: String) { _form.value = _form.value.copy(realBalanceText = value.filter { it.isDigit() || it == ',' || it == '.' || it == '-' }) }

    fun reconcile() {
        val state = _form.value
        val accountBalance = balances.value.firstOrNull { it.account.id == state.selectedAccountId }
        if (accountBalance == null) {
            _message.value = "Selecione uma conta."
            return
        }
        val realBalance = parseMoneyToCents(state.realBalanceText)
        val diff = realBalance - accountBalance.currentBalanceInCents
        if (diff == 0L) {
            _message.value = "Saldo já está reconciliado."
            return
        }
        viewModelScope.launch {
            movementRepository.addMovement(
                FinancialMovement(
                    type = FinancialMovementType.ADJUSTMENT,
                    amountInCents = diff,
                    description = "Ajuste de saldo - ${accountBalance.account.name}",
                    category = "ajuste",
                    accountId = accountBalance.account.id
                )
            ).onSuccess {
                _form.value = ReconciliationFormState()
                _message.value = "Saldo reconciliado com ajuste de ${formatSigned(diff)}."
            }.onFailure {
                _message.value = it.message ?: "Não foi possível reconciliar."
            }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }

    private fun formatSigned(cents: Long): String = if (cents >= 0) "+${cents / 100.0}" else "${cents / 100.0}"
}
