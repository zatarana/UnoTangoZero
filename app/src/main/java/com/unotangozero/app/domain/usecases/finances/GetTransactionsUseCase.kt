package com.unotangozero.app.domain.usecases.finances

import com.unotangozero.app.domain.models.Transaction
import com.unotangozero.app.domain.repositories.FinanceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching all transactions.
 */
class GetTransactionsUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    operator fun invoke(): Flow<List<Transaction>> {
        return financeRepository.getAllTransactions()
    }
}
