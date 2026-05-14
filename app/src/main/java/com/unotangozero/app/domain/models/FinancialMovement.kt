package com.unotangozero.app.domain.models

import com.unotangozero.app.data.serialization.LocalDateSerializer
import com.unotangozero.app.data.serialization.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Serializable
enum class FinancialMovementType(val displayName: String) {
    INCOME("Receita"),
    EXPENSE("Despesa"),
    TRANSFER("Transferência"),
    ADJUSTMENT("Ajuste de saldo")
}

@Serializable
data class FinancialMovement(
    val id: String = UUID.randomUUID().toString(),
    val type: FinancialMovementType,
    val amountInCents: Long,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate = LocalDate.now(),
    val description: String,
    val category: String? = null,
    val accountId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class AccountBalance(
    val account: FinancialAccount,
    val currentBalanceInCents: Long
)
