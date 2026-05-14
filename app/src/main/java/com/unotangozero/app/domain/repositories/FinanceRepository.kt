package com.unotangozero.app.domain.repositories

import com.unotangozero.app.domain.models.Transaction
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Repository interface for Financial Transaction operations.
 */
interface FinanceRepository {
    /**
     * Get all transactions as a flow for real-time updates.
     */
    fun getAllTransactions(): Flow<List<Transaction>>

    /**
     * Get transactions within a date range.
     */
    fun getTransactionsByDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<Transaction>>

    /**
     * Get a specific transaction by ID.
     */
    suspend fun getTransactionById(id: Long): Transaction?

    /**
     * Create a new transaction.
     */
    suspend fun createTransaction(transaction: Transaction): Long

    /**
     * Update an existing transaction.
     */
    suspend fun updateTransaction(transaction: Transaction)

    /**
     * Delete a transaction.
     */
    suspend fun deleteTransaction(id: Long)

    /**
     * Calculate total expenses for a period.
     */
    suspend fun calculateTotalExpenses(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): BigDecimal

    /**
     * Calculate total income for a period.
     */
    suspend fun calculateTotalIncome(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): BigDecimal
}
