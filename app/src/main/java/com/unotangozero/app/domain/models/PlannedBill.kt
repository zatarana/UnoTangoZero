package com.unotangozero.app.domain.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

enum class PlannedBillType(val displayName: String) {
    PAYABLE("A pagar"),
    RECEIVABLE("A receber")
}

data class PlannedBill(
    val id: String = UUID.randomUUID().toString(),
    val type: PlannedBillType,
    val description: String,
    val amountInCents: Long,
    val dueDate: LocalDate,
    val category: String? = null,
    val accountId: String? = null,
    val isPaid: Boolean = false,
    val paidAt: LocalDate? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
