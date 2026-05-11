package com.unotangozero.app.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.FinancialMovement
import com.unotangozero.app.domain.models.FinancialMovementType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import javax.inject.Inject

data class CategoryExpenseReport(
    val category: String,
    val amountInCents: Long,
    val percentage: Double
)

data class MonthlyFinancialReport(
    val yearMonth: YearMonth = YearMonth.now(),
    val incomeInCents: Long = 0L,
    val expenseInCents: Long = 0L,
    val transferInCents: Long = 0L,
    val balanceInCents: Long = 0L,
    val categoryExpenses: List<CategoryExpenseReport> = emptyList(),
    val movements: List<FinancialMovement> = emptyList()
)

@HiltViewModel
class FinancialReportsViewModel @Inject constructor(
    movementRepository: FinancialMovementRepository
) : ViewModel() {
    private val selectedMonth = MutableStateFlow(YearMonth.now())

    val report: StateFlow<MonthlyFinancialReport> = combine(
        movementRepository.movements,
        selectedMonth
    ) { movements, month ->
        val monthMovements = movements.filter { YearMonth.from(it.date) == month }
        val income = monthMovements.filter { it.type == FinancialMovementType.INCOME }.sumOf { it.amountInCents }
        val expenses = monthMovements.filter { it.type == FinancialMovementType.EXPENSE }.sumOf { it.amountInCents }
        val transfers = monthMovements.filter { it.type == FinancialMovementType.TRANSFER }.sumOf { it.amountInCents }
        val categories = monthMovements
            .filter { it.type == FinancialMovementType.EXPENSE }
            .groupBy { it.category?.ifBlank { "sem categoria" } ?: "sem categoria" }
            .map { (category, items) ->
                val amount = items.sumOf { it.amountInCents }
                CategoryExpenseReport(
                    category = category,
                    amountInCents = amount,
                    percentage = if (expenses > 0L) amount.toDouble() / expenses.toDouble() * 100.0 else 0.0
                )
            }
            .sortedByDescending { it.amountInCents }

        MonthlyFinancialReport(
            yearMonth = month,
            incomeInCents = income,
            expenseInCents = expenses,
            transferInCents = transfers,
            balanceInCents = income - expenses,
            categoryExpenses = categories,
            movements = monthMovements.sortedByDescending { it.date }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlyFinancialReport())

    fun previousMonth() {
        selectedMonth.value = selectedMonth.value.minusMonths(1)
    }

    fun nextMonth() {
        selectedMonth.value = selectedMonth.value.plusMonths(1)
    }

    fun currentMonth() {
        selectedMonth.value = YearMonth.now()
    }
}
