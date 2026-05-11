package com.unotangozero.app.data.repositories

import com.unotangozero.app.data.db.dao.BudgetDao
import com.unotangozero.app.data.db.dao.DebtDao
import com.unotangozero.app.data.db.dao.DebtPaymentDao
import com.unotangozero.app.data.db.dao.ExpenseDao
import com.unotangozero.app.data.mappers.toDomain
import com.unotangozero.app.data.mappers.toEntity
import com.unotangozero.app.data.mappers.toEpochMillis
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.models.Budget
import com.unotangozero.app.domain.models.BudgetStatus
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtPayment
import com.unotangozero.app.domain.models.DebtSummary
import com.unotangozero.app.domain.models.Expense
import com.unotangozero.app.domain.repositories.BudgetRepository
import com.unotangozero.app.domain.repositories.DebtRepository
import com.unotangozero.app.domain.repositories.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {
    override suspend fun save(expense: Expense): Result<Unit> = runCatching {
        expenseDao.insert(expense.toEntity())
    }

    override suspend fun delete(expenseId: String): Result<Unit> = runCatching {
        expenseDao.deleteById(expenseId)
    }

    override fun observeById(expenseId: String): Flow<Expense?> {
        return expenseDao.observeById(expenseId).map { it?.toDomain() }
    }

    override fun observeByMonth(month: YearMonth): Flow<List<Expense>> {
        val start = month.atDay(1).toEpochMillis()
        val end = month.atEndOfMonth().plusDays(1).toEpochMillis() - 1
        return expenseDao.observeByDateRange(start, end).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeByDate(date: LocalDate): Flow<List<Expense>> {
        val start = date.toEpochMillis()
        val end = date.plusDays(1).toEpochMillis() - 1
        return expenseDao.observeByDateRange(start, end).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeTotalByDate(date: LocalDate): Flow<Long> {
        val start = date.toEpochMillis()
        val end = date.plusDays(1).toEpochMillis() - 1
        return expenseDao.observeTotalByDateRange(start, end)
    }
}

class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao
) : BudgetRepository {
    override suspend fun save(budget: Budget): Result<Unit> = runCatching {
        budgetDao.insert(budget.toEntity())
    }

    override suspend fun delete(budgetId: String): Result<Unit> = runCatching {
        budgetDao.deleteById(budgetId)
    }

    override fun observeByMonth(month: YearMonth): Flow<List<Budget>> {
        return budgetDao.observeByMonth(month.toString()).map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeBudgetStatus(month: YearMonth): Flow<List<BudgetStatus>> {
        val start = month.atDay(1).toEpochMillis()
        val end = month.atEndOfMonth().plusDays(1).toEpochMillis() - 1
        return combine(
            budgetDao.observeByMonth(month.toString()),
            expenseDao.observeByDateRange(start, end)
        ) { budgetEntities, expenseEntities ->
            budgetEntities.map { budgetEntity ->
                val budget = budgetEntity.toDomain()
                val spent = expenseEntities
                    .filter { it.category == budget.category.name }
                    .sumOf { it.amountInCents }
                BudgetStatus(
                    category = budget.category,
                    limitAmountInCents = budget.limitAmountInCents,
                    spentAmountInCents = spent
                )
            }
        }
    }
}

class DebtRepositoryImpl @Inject constructor(
    private val debtDao: DebtDao,
    private val debtPaymentDao: DebtPaymentDao
) : DebtRepository {
    override suspend fun save(debt: Debt): Result<Unit> = runCatching {
        debtDao.insert(debt.toEntity())
    }

    override suspend fun delete(debtId: String): Result<Unit> = runCatching {
        debtDao.deleteById(debtId)
    }

    override suspend fun recordPayment(payment: DebtPayment): Result<Unit> = runCatching {
        debtPaymentDao.insert(payment.toEntity())
    }

    override fun observeById(debtId: String): Flow<Debt?> {
        return combine(
            debtDao.observeById(debtId),
            debtPaymentDao.observeByDebtId(debtId)
        ) { debtEntity, paymentEntities ->
            debtEntity?.toDomain(paymentHistory = paymentEntities.map { it.toDomain() })
        }
    }

    override fun observeActive(): Flow<List<Debt>> {
        return debtDao.observeActive().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeAll(): Flow<List<Debt>> {
        return debtDao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeSummary(): Flow<DebtSummary> {
        return observeAll().map { debts ->
            val active = debts.filter { it.status != DebtStatus.PAID }
            val nextDue = active.minByOrNull { it.dueDate }
            DebtSummary(
                totalDebts = debts.size,
                activeDebts = active.size,
                paidDebts = debts.count { it.status == DebtStatus.PAID },
                totalDebtAmountInCents = active.sumOf { it.remainingAmountInCents },
                totalDebtWithInterestInCents = active.sumOf { it.totalDueInCents },
                nextDueDate = nextDue?.dueDate,
                nextDueAmountInCents = nextDue?.totalDueInCents ?: 0L
            )
        }
    }
}
