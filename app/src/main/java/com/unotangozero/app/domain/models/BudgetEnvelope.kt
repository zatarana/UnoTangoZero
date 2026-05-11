package com.unotangozero.app.domain.models

import java.time.YearMonth
import java.util.UUID

data class BudgetEnvelope(
    val id: String = UUID.randomUUID().toString(),
    val yearMonth: String = YearMonth.now().toString(),
    val category: String,
    val allocatedAmountInCents: Long,
    val rolloverEnabled: Boolean = false
)

data class BudgetEnvelopeStatus(
    val envelope: BudgetEnvelope,
    val spentAmountInCents: Long,
    val rolloverAmountInCents: Long = 0L
) {
    val availableAmountInCents: Long = envelope.allocatedAmountInCents + rolloverAmountInCents
    val remainingAmountInCents: Long = availableAmountInCents - spentAmountInCents
    val percentageUsed: Double = if (availableAmountInCents > 0L) {
        spentAmountInCents.toDouble() / availableAmountInCents.toDouble() * 100.0
    } else {
        0.0
    }
    val isOverBudget: Boolean = spentAmountInCents > availableAmountInCents
}

data class MonthlyBudgetSummary(
    val yearMonth: String,
    val totalIncomeInCents: Long,
    val totalAllocatedInCents: Long,
    val totalSpentInCents: Long,
    val totalRolloverInCents: Long = 0L,
    val envelopes: List<BudgetEnvelopeStatus>
) {
    val totalAvailableInCents: Long = totalAllocatedInCents + totalRolloverInCents
    val amountToDistributeInCents: Long = totalIncomeInCents - totalAllocatedInCents
}
