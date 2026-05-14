package com.unotangozero.app.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.bills.PlannedBillRepository
import com.unotangozero.app.data.budget.EnvelopeBudgetRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.FinancialAccount
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.MonthlyBudgetSummary
import com.unotangozero.app.domain.models.PlannedBill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlin.math.round

data class FinanceDashboardUiState(
    val totalBalanceInCents: Long = 0L,
    val accountCount: Int = 0,
    val accounts: List<FinancialAccount> = emptyList(),
    val monthlyIncomeInCents: Long = 0L,
    val monthlyExpenseInCents: Long = 0L,
    val monthlyAdjustmentInCents: Long = 0L,
    val monthlyBalanceInCents: Long = 0L,
    val monthlyMovementCount: Int = 0,
    val budgetSummary: MonthlyBudgetSummary? = null,
    val budgetEnvelopeCategories: List<String> = emptyList(),
    val upcomingBills: List<PlannedBill> = emptyList(),
    val overdueBills: List<PlannedBill> = emptyList(),
    val recentMovements: List<FinancialMovement> = emptyList(),
    val movements: List<FinancialMovement> = emptyList(),
    val next30DaysImpactInCents: Long = 0L
)

@HiltViewModel
class FinanceDashboardViewModel @Inject constructor(
    private val movementRepository: FinancialMovementRepository,
    budgetRepository: EnvelopeBudgetRepository,
    plannedBillRepository: PlannedBillRepository
) : ViewModel() {
    val uiState: StateFlow<FinanceDashboardUiState> = combine(
        movementRepository.accountBalances,
        movementRepository.movements,
        budgetRepository.currentMonthSummary,
        plannedBillRepository.bills
    ) { balances, movements, budgetSummary, bills ->
        val today = LocalDate.now()
        val month = YearMonth.now()
        val monthMovements = movements.filter { YearMonth.from(it.date) == month }
        val income = monthMovements.filter { it.type == FinancialMovementType.INCOME }.sumOf { it.amountInCents }
        val expenses = monthMovements.filter { it.type == FinancialMovementType.EXPENSE }.sumOf { it.amountInCents }
        val adjustments = monthMovements.filter { it.type == FinancialMovementType.ADJUSTMENT }.sumOf { it.amountInCents }
        val openBills = bills.filter { !it.isPaid }
        val upcoming = openBills
            .filter { !it.dueDate.isBefore(today) && !it.dueDate.isAfter(today.plusDays(30)) }
            .sortedBy { it.dueDate }
        val overdue = openBills.filter { it.dueDate.isBefore(today) }.sortedBy { it.dueDate }
        FinanceDashboardUiState(
            totalBalanceInCents = balances.sumOf { it.currentBalanceInCents },
            accountCount = balances.size,
            accounts = balances.map { it.account },
            monthlyIncomeInCents = income,
            monthlyExpenseInCents = expenses,
            monthlyAdjustmentInCents = adjustments,
            monthlyBalanceInCents = income - expenses + adjustments,
            monthlyMovementCount = monthMovements.size,
            budgetSummary = budgetSummary,
            budgetEnvelopeCategories = budgetSummary.envelopes.map { it.envelope.category }.distinctBy { it.trim().lowercase() },
            upcomingBills = upcoming.take(5),
            overdueBills = overdue.take(5),
            recentMovements = movements.sortedByDescending { it.date }.take(5),
            movements = movements.sortedByDescending { it.date },
            next30DaysImpactInCents = upcoming.sumOf { bill ->
                if (bill.type.name == "PAYABLE") -bill.amountInCents else bill.amountInCents
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceDashboardUiState())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun registerQuickTransaction(
        amountText: String,
        type: FinancialMovementType,
        category: String,
        accountId: String?,
        date: LocalDate
    ) {
        val normalizedCategory = category.trim()
        val amountInCents = parseMoneyToCents(amountText)
        if (amountInCents <= 0L) {
            _message.value = "Digite um valor maior que zero."
            return
        }
        if (type != FinancialMovementType.EXPENSE && type != FinancialMovementType.INCOME) {
            _message.value = "Escolha despesa ou receita."
            return
        }
        if (normalizedCategory.isBlank()) {
            _message.value = "Selecione uma categoria."
            return
        }
        if (accountId.isNullOrBlank()) {
            _message.value = "Selecione uma conta."
            return
        }

        val hasMatchingEnvelope = uiState.value.budgetEnvelopeCategories.any { envelopeCategory ->
            envelopeCategory.trim().equals(normalizedCategory, ignoreCase = true)
        }

        viewModelScope.launch {
            val movement = FinancialMovement(
                type = type,
                amountInCents = amountInCents,
                date = date,
                description = normalizedCategory,
                category = normalizedCategory,
                accountId = accountId
            )
            movementRepository.addMovement(movement)
                .onSuccess {
                    _message.value = when {
                        type == FinancialMovementType.EXPENSE && hasMatchingEnvelope ->
                            "Despesa salva e descontada automaticamente do envelope $normalizedCategory."
                        type == FinancialMovementType.EXPENSE ->
                            "Despesa salva. Nenhum envelope com a categoria $normalizedCategory foi encontrado."
                        else -> "Lançamento salvo."
                    }
                }
                .onFailure { _message.value = it.message ?: "Não foi possível salvar o lançamento." }
        }
    }

    fun deleteMovement(movementId: String) {
        viewModelScope.launch {
            movementRepository.deleteMovement(movementId)
                .onSuccess { _message.value = "Lançamento excluído. O orçamento foi recalculado automaticamente." }
                .onFailure { _message.value = it.message ?: "Não foi possível excluir o lançamento." }
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
}
