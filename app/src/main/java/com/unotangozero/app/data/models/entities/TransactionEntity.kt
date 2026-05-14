package com.unotangozero.app.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Room entity representing a Financial Transaction.
 */
@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: String = "0", // Using String to preserve BigDecimal precision
    val category: String = "OTHER",
    val type: String = "EXPENSE",
    val description: String = "",
    val date: LocalDateTime = LocalDateTime.now(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val paymentMethod: String = "CASH",
    val tags: String = "", // JSON string
    val attachments: String = "" // JSON string
)
