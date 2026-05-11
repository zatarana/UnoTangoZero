package com.unotangozero.app.domain.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class SavingsGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmountInCents: Long,
    val currentAmountInCents: Long = 0L,
    val targetDate: LocalDate? = null,
    val category: String? = null,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val remainingAmountInCents: Long = (targetAmountInCents - currentAmountInCents).coerceAtLeast(0L)
    val progressPercentage: Double = if (targetAmountInCents > 0L) {
        (currentAmountInCents.toDouble() / targetAmountInCents.toDouble() * 100.0).coerceAtMost(100.0)
    } else {
        0.0
    }
}

data class SavingsGoalDeposit(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val amountInCents: Long,
    val date: LocalDate = LocalDate.now(),
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
