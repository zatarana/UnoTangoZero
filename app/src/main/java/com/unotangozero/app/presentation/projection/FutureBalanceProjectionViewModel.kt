package com.unotangozero.app.presentation.projection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.bills.PlannedBillRepository
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.PlannedBill
import com.unotangozero.app.domain.models.PlannedBillType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

data class ProjectionHorizon(
    val days: Int,
    val projectedBalanceInCents: Long,
    val plannedImpactInCents: Long,
    val bills: List<PlannedBill>
)

data class FutureBalanceProjectionUiState(
    val currentBalanceInCents: Long = 0L,
    val horizons: List<ProjectionHorizon> = emptyList(),
    val upcomingBills: List<PlannedBill> = emptyList()
)

@HiltViewModel
class FutureBalanceProjectionViewModel @Inject constructor(
    movementRepository: FinancialMovementRepository,
    plannedBillRepository: PlannedBillRepository
) : ViewModel() {
    val uiState: StateFlow<FutureBalanceProjectionUiState> = combine(
        movementRepository.accountBalances,
        plannedBillRepository.bills
    ) { balances, bills ->
        val today = LocalDate.now()
        val openBills = bills.filter { !it.isPaid && !it.dueDate.isBefore(today) }.sortedBy { it.dueDate }
        val currentBalance = balances.sumOf { it.currentBalanceInCents }
        val horizons = listOf(30, 60, 90).map { days ->
            val limit = today.plusDays(days.toLong())
            val billsInRange = openBills.filter { !it.dueDate.isAfter(limit) }
            val impact = billsInRange.sumOf { signedImpact(it) }
            ProjectionHorizon(
                days = days,
                plannedImpactInCents = impact,
                projectedBalanceInCents = currentBalance + impact,
                bills = billsInRange
            )
        }
        FutureBalanceProjectionUiState(
            currentBalanceInCents = currentBalance,
            horizons = horizons,
            upcomingBills = openBills.take(20)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FutureBalanceProjectionUiState())

    private fun signedImpact(bill: PlannedBill): Long {
        return when (bill.type) {
            PlannedBillType.PAYABLE -> -bill.amountInCents
            PlannedBillType.RECEIVABLE -> bill.amountInCents
        }
    }
}
