package com.unotangozero.app.di

import com.unotangozero.app.data.repositories.BudgetRepositoryImpl
import com.unotangozero.app.data.repositories.DashboardRepositoryImpl
import com.unotangozero.app.data.repositories.DebtRepositoryImpl
import com.unotangozero.app.data.repositories.ExpenseRepositoryImpl
import com.unotangozero.app.data.repositories.HabitRepositoryImpl
import com.unotangozero.app.data.repositories.NoteRepositoryImpl
import com.unotangozero.app.data.repositories.ReminderRepositoryImpl
import com.unotangozero.app.data.repositories.ShoppingRepositoryImpl
import com.unotangozero.app.data.repositories.SubTaskRepositoryImpl
import com.unotangozero.app.data.repositories.TaskRepositoryImpl
import com.unotangozero.app.domain.repositories.BudgetRepository
import com.unotangozero.app.domain.repositories.DashboardRepository
import com.unotangozero.app.domain.repositories.DebtRepository
import com.unotangozero.app.domain.repositories.ExpenseRepository
import com.unotangozero.app.domain.repositories.HabitRepository
import com.unotangozero.app.domain.repositories.NoteRepository
import com.unotangozero.app.domain.repositories.ReminderRepository
import com.unotangozero.app.domain.repositories.ShoppingRepository
import com.unotangozero.app.domain.repositories.SubTaskRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds
    @Singleton
    abstract fun bindSubTaskRepository(impl: SubTaskRepositoryImpl): SubTaskRepository

    @Binds
    @Singleton
    abstract fun bindReminderRepository(impl: ReminderRepositoryImpl): ReminderRepository

    @Binds
    @Singleton
    abstract fun bindExpenseRepository(impl: ExpenseRepositoryImpl): ExpenseRepository

    @Binds
    @Singleton
    abstract fun bindBudgetRepository(impl: BudgetRepositoryImpl): BudgetRepository

    @Binds
    @Singleton
    abstract fun bindDebtRepository(impl: DebtRepositoryImpl): DebtRepository

    @Binds
    @Singleton
    abstract fun bindShoppingRepository(impl: ShoppingRepositoryImpl): ShoppingRepository

    @Binds
    @Singleton
    abstract fun bindHabitRepository(impl: HabitRepositoryImpl): HabitRepository

    @Binds
    @Singleton
    abstract fun bindNoteRepository(impl: NoteRepositoryImpl): NoteRepository

    @Binds
    @Singleton
    abstract fun bindDashboardRepository(impl: DashboardRepositoryImpl): DashboardRepository
}
