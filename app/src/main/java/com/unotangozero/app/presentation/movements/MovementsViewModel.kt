package com.unotangozero.app.presentation.movements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.accounts.FinancialAccountRepository
import com.unotangozero.app.data.categories.FinancialCategoryRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.AccountBalance
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialCategory
import com.unotangozero.app.domain.models.FinancialCategoryType
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject
import kotlin.math.round

data class MovementFormState(
    val type: FinancialMovementType = FinancialMovementType.EXPENSE,
    val description: String = "",
    val amountText: String = "",
    val category: String = "",
    val accountId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val date: LocalDate = LocalDate.now(),
    val isInstallment: Boolean = false,
    val installmentCountText: String = ""
)

@HiltViewModel
class MovementsViewModel @Inject constructor(
    accountRepository: FinancialAccountRepository,
    categoryRepository: FinancialCategoryRepository,
    private val movementRepository: FinancialMovementRepository
) : ViewModel() {
    val accounts: StateFlow<List<FinancialAccount>> = accountRepository.accounts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categories: StateFlow<List<FinancialCategory>> = categoryRepository.categories
        .map { list -> list.filter { !it.isArchived } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val balances: StateFlow<List<AccountBalance>> = movementRepository.accountBalances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val movements: StateFlow<List<FinancialMovement>> = movementRepository.movements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _form = MutableStateFlow(MovementFormState())
    val form: StateFlow<MovementFormState> = _form.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTypeChange(type: FinancialMovementType) {
        if (type != FinancialMovementType.ADJUSTMENT) {
            _form.value = _form.value.copy(
                type = type,
                category = "",
                isInstallment = if (type == FinancialMovementType.TRANSFER) false else _form.value.isInstallment,
                installmentCountText = if (type == FinancialMovementType.TRANSFER) "" else _form.value.installmentCountText
            )
        }
    }
    fun onDescriptionChange(value: String) { _form.value = _form.value.copy(description = value) }
    fun onAmountChange(value: String) { _form.value = _form.value.copy(amountText = value.filter { it.isDigit() || it == ',' || it == '.' }) }
    fun onCategoryChange(value: String) { _form.value = _form.value.copy(category = value) }
    fun onCategorySelected(category: FinancialCategory) { _form.value = _form.value.copy(category = category.displayLabel) }
    fun onAccountChange(id: String?) { _form.value = _form.value.copy(accountId = id) }
    fun onFromAccountChange(id: String?) { _form.value = _form.value.copy(fromAccountId = id) }
    fun onToAccountChange(id: String?) { _form.value = _form.value.copy(toAccountId = id) }
    fun onInstallmentChange(value: Boolean) { _form.value = _form.value.copy(isInstallment = value, installmentCountText = if (value) _form.value.installmentCountText else "") }
    fun onInstallmentCountChange(value: String) { _form.value = _form.value.copy(installmentCountText = value.filter { it.isDigit() }.take(2)) }
    fun previousDay() { _form.value = _form.value.copy(date = _form.value.date.minusDays(1)) }
    fun nextDay() { _form.value = _form.value.copy(date = _form.value.date.plusDays(1)) }
    fun onDateSelected(date: LocalDate) { _form.value = _form.value.copy(date = date) }
    fun today() { _form.value = _form.value.copy(date = LocalDate.now()) }
    fun yesterday() { _form.value = _form.value.copy(date = LocalDate.now().minusDays(1)) }
    fun tomorrow() { _form.value = _form.value.copy(date = LocalDate.now().plusDays(1)) }

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

        val installmentCount = if (state.isInstallment && state.type != FinancialMovementType.TRANSFER) {
            val count = state.installmentCountText.toIntOrNull() ?: 0
            if (count < 2) {
                _message.value = "Digite pelo menos 2 parcelas."
                return
            }
            count.coerceAtMost(99)
        } else {
            1
        }

        val baseMovement = when (state.type) {
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
            FinancialMovementType.ADJUSTMENT -> {
                _message.value = "Ajuste de saldo deve ser feito pela tela Reconciliação."
                return
            }
        }

        viewModelScope.launch {
            val movementsToSave = if (installmentCount > 1) {
                buildInstallments(baseMovement, installmentCount)
            } else {
                listOf(baseMovement)
            }

            var failureMessage: String? = null
            movementsToSave.forEach { movement ->
                movementRepository.addMovement(movement)
                    .onFailure { failureMessage = it.message ?: "Não foi possível salvar." }
                if (failureMessage != null) return@forEach
            }

            if (failureMessage == null) {
                _form.value = MovementFormState(type = state.type)
                _message.value = if (installmentCount > 1) "$installmentCount parcelas salvas a partir do valor total." else "Movimentação salva."
            } else {
                _message.value = failureMessage
            }
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

    fun categoryTypeForCurrentMovement(): FinancialCategoryType? = when (_form.value.type) {
        FinancialMovementType.INCOME -> FinancialCategoryType.INCOME
        FinancialMovementType.EXPENSE -> FinancialCategoryType.EXPENSE
        else -> null
    }

    private fun buildInstallments(baseMovement: FinancialMovement, installmentCount: Int): List<FinancialMovement> {
        val baseInstallmentAmount = baseMovement.amountInCents / installmentCount
        val remainder = baseMovement.amountInCents % installmentCount
        return (1..installmentCount).map { installmentNumber ->
            val installmentAmount = baseInstallmentAmount + if (installmentNumber == 1) remainder else 0L
            baseMovement.copy(
                id = java.util.UUID.randomUUID().toString(),
                amountInCents = installmentAmount,
                date = baseMovement.date.plusMonths((installmentNumber - 1).toLong()),
                description = "${baseMovement.description} ($installmentNumber/$installmentCount)"
            )
        }
    }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
