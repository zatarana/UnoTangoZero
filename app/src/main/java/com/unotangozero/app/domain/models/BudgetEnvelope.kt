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
    val spentAmountInCents: Long
) {
    val remainingAmountInCents: Long = envelope.allocatedAmountInCents - spentAmountInCents
    val percentageUsed: Double = if (envelope.allocatedAmountInCents > 0L) {
        spentAmountInCents.toDouble() / envelope.allocatedAmountInCents.toDouble() * 100.0
    } else {
        0.0
    }
    val isOverBudget: Boolean = spentAmountInCents > envelope.allocatedAmountInCents
}

data class MonthlyBudgetSummary(
    val yearMonth: String,
    val totalIncomeInCents: Long,
    val totalAllocatedInCents: Long,
    val totalSpentInCents: Long,
    val envelopes: List<BudgetEnvelopeStatus>
) {
    val amountToDistributeInCents: Long = totalIncomeInCents - totalAllocatedInCents
}
