package com.unotangozero.app.presentation.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
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

data class MovementFormState(
    val type: FinancialMovementType = FinancialMovementType.INCOME,
    val description: String = "",
    val amountText: String = "",
    val category: String = "",
    val accountId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val date: LocalDate = LocalDate.now()
)

@HiltViewModel
class MovementsViewModel @Inject constructor(
    accountRepository: FinancialAccountRepository,
    private val movementRepository: FinancialMovementRepository
) : ViewModel() {
    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balances: StateFlow<List<AccountBalance>> = movementRepository.accountBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val movements: StateFlow<List<FinancialMovement>> = movementRepository.movements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(MovementFormState())
    val form: StateFlow<MovementFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTypeChange(type: FinancialMovementType) { _form.value = _form.value.copy(type = type) }
    fun onDescriptionChange(value: String) { _form.value = _form.value.copy(description = value) }
    fun onAmountChange(value: String) { _form.value = _form.value.copy(amountText = value.filter { it.isDigit() || it == ',' || it == '.' }) }
    fun onCategoryChange(value: String) { _form.value = _form.value.copy(category = value) }
    fun onAccountChange(id: String?) { _form.value = _form.value.copy(accountId = id) }
    fun onFromAccountChange(id: String?) { _form.value = _form.value.copy(fromAccountId = id) }
    fun onToAccountChange(id: String?) { _form.value = _form.value.copy(toAccountId = id) }
    fun previousDay() { _form.value = _form.value.copy(date = _form.value.date.minusDays(1)) }
    fun nextDay() { _form.value = _form.value.copy(date = _form.value.date.plusDays(1)) }

    fun save() {
        val state = _form.value
        val amount = parseMoneyToCents(state.amountText)
        if (amount <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }
        if (state.description.trim().isBlank()) {
            _message.value = "Digite uma descrição."
            return
        }

        val movement = when (state.type) {
            FinancialMovementType.INCOME -> {
                if (state.accountId == null) {
                    _message.value = "Selecione a conta de destino."
                    return
                }
                FinancialMovement(
                    type = FinancialMovementType.INCOME,
                    amountInCents = amount,
                    date = state.date,
                    description = state.description.trim(),
                    category = state.category.trim().ifBlank { null },
                    accountId = state.accountId
                )
            }
            FinancialMovementType.EXPENSE -> {
                if (state.accountId == null) {
                    _message.value = "Selecione a conta de saída."
                    return
                }
                FinancialMovement(
                    type = FinancialMovementType.EXPENSE,
                    amountInCents = amount,
                    date = state.date,
                    description = state.description.trim(),
                    category = state.category.trim().ifBlank { null },
                    accountId = state.accountId
                )
            }
            FinancialMovementType.TRANSFER -> {
                if (state.fromAccountId == null || state.toAccountId == null) {
                    _message.value = "Selecione origem e destino."
                    return
                }
                if (state.fromAccountId == state.toAccountId) {
                    _message.value = "Origem e destino precisam ser diferentes."
                    return
                }
                FinancialMovement(
                    type = FinancialMovementType.TRANSFER,
                    amountInCents = amount,
                    date = state.date,
                    description = state.description.trim(),
                    fromAccountId = state.fromAccountId,
                    toAccountId = state.toAccountId
                )
            }
        }

        viewModelScope.launch {
            movementRepository.addMovement(movement)
                .onSuccess {
                    _form.value = MovementFormState(type = state.type)
                    _message.value = "Movimentação salva."
                }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar." }
        }
    }

    fun deleteMovement(movement: FinancialMovement) {
        viewModelScope.launch {
            movementRepository.deleteMovement(movement.id)
                .onSuccess { _message.value = "Movimentação excluída." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir." }
        }
    }

    fun clearMessage() { _message.value = null }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
