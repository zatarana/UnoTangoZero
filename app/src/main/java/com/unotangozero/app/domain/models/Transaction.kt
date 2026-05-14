package com.unotangozero.app.domain.models

import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Domain model representing a financial transaction.
 */
data class Transaction(
    val id: Long = 0,
    val amount: BigDecimal = BigDecimal.ZERO,
    val category: TransactionCategory = TransactionCategory.OTHER,
    val type: TransactionType = TransactionType.EXPENSE,
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val tags: List<String> = emptyList(),
    val attachments: List<String> = emptyList()
)

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

enum class TransactionCategory {
    FOOD, TRANSPORT, UTILITIES, ENTERTAINMENT, HEALTH,
    EDUCATION, SHOPPING, SALARY, BONUS, INVESTMENT,
    REFUND, OTHER
}

enum class PaymentMethod {
    CASH, CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, DIGITAL_WALLET
}
