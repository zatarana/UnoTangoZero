package com.unotangozero.app.domain.usecases.finances

import com.unotangozero.app.domain.models.Transaction
import com.unotangozero.app.domain.repositories.FinanceRepository
import javax.inject.Inject

/**
 * Use case for adding a new financial transaction.
 */
class AddTransactionUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    suspend operator fun invoke(transaction: Transaction): Long {
        require(transaction.amount > java.math.BigDecimal.ZERO) { "Transaction amount must be greater than 0" }
        require(transaction.description.isNotBlank()) { "Transaction description cannot be empty" }
        return financeRepository.createTransaction(transaction)
    }
}
