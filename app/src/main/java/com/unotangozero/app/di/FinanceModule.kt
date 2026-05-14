package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.BudgetRepositoryImpl
import com.unotangozero.app.data.repositories.DebtRepositoryImpl
import com.unotangozero.app.data.repositories.ExpenseRepositoryImpl
import com.unotangozero.app.domain.repositories.BudgetRepository
import com.unotangozero.app.domain.repositories.DebtRepository
import com.unotangozero.app.domain.repositories.ExpenseRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FinanceModule {
    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository
}
