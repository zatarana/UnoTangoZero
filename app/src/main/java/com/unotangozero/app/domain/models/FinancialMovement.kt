package com.unotangozero.app.domain.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

enum class FinancialMovementType(val displayName: String) {
    INCOME("Receita"),
    TRANSFER("Transferência")
}

data class FinancialMovement(
    val id: String = UUID.randomUUID().toString(),
    val type: FinancialMovementType,
    val amountInCents: Long,
    val date: LocalDate = LocalDate.now(),
    val description: String,
    val category: String? = null,
    val accountId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class AccountBalance(
    val account: FinancialAccount,
    val currentBalanceInCents: Long
)
