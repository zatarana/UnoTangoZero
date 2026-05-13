package com.unotangozero.app.presentation.finance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.bills.PlannedBillRepository
import com.unotangozero.app.data.budget.EnvelopeBudgetRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.MonthlyBudgetSummary
import com.unotangozero.app.domain.models.PlannedBill
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class FinanceDashboardUiState(
    val totalBalanceInCents: Long = 0L,
    val accountCount: Int = 0,
    val monthlyIncomeInCents: Long = 0L,
    val monthlyExpenseInCents: Long = 0L,
    val monthlyAdjustmentInCents: Long = 0L,
    val monthlyBalanceInCents: Long = 0L,
    val monthlyMovementCount: Int = 0,
    val budgetSummary: MonthlyBudgetSummary? = null,
    val upcomingBills: List<PlannedBill> = emptyList(),
    val overdueBills: List<PlannedBill> = emptyList(),
    val recentMovements: List<FinancialMovement> = emptyList(),
    val next30DaysImpactInCents: Long = 0L
)

@HiltViewModel
class FinanceDashboardViewModel @Inject constructor(
    movementRepository: FinancialMovementRepository,
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
            monthlyIncomeInCents = income,
            monthlyExpenseInCents = expenses,
            monthlyAdjustmentInCents = adjustments,
            monthlyBalanceInCents = income - expenses + adjustments,
            monthlyMovementCount = monthMovements.size,
            budgetSummary = budgetSummary,
            upcomingBills = upcoming.take(5),
            overdueBills = overdue.take(5),
            recentMovements = movements.sortedByDescending { it.date }.take(5),
            next30DaysImpactInCents = upcoming.sumOf { bill ->
                if (bill.type.name == "PAYABLE") -bill.amountInCents else bill.amountInCents
            }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FinanceDashboardUiState())
}
