package com.unotangozero.app.domain.usecases.finances

import com.unotangozero.app.domain.repositories.FinanceRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import javax.inject.Inject

/**
 * Use case for calculating total expenses in a period.
 */
class CalculateTotalExpensesUseCase @Inject constructor(
    private val financeRepository: FinanceRepository
) {
    suspend operator fun invoke(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): BigDecimal {
        require(startDate.isBefore(endDate)) { "Start date must be before end date" }
        return financeRepository.calculateTotalExpenses(startDate, endDate)
    }
}
