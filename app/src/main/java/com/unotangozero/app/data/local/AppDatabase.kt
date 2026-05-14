package com.unotangozero.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.unotangozero.app.data.local.dao.AccountDao
import com.unotangozero.app.data.local.dao.BillDao
import com.unotangozero.app.data.local.dao.BudgetDao
import com.unotangozero.app.data.local.dao.CategoryDao
import com.unotangozero.app.data.local.dao.DebtDao
import com.unotangozero.app.data.local.dao.FocusSessionDao
import com.unotangozero.app.data.local.dao.GoalDao
import com.unotangozero.app.data.local.dao.HabitDao
import com.unotangozero.app.data.local.dao.TaskDao
import com.unotangozero.app.data.local.dao.TransactionDao
import com.unotangozero.app.data.local.entity.AccountEntity
import com.unotangozero.app.data.local.entity.BillEntity
import com.unotangozero.app.data.local.entity.BudgetEntity
import com.unotangozero.app.data.local.entity.CategoryEntity
import com.unotangozero.app.data.local.entity.DebtEntity
import com.unotangozero.app.data.local.entity.FocusSessionEntity
import com.unotangozero.app.data.local.entity.GoalEntity
import com.unotangozero.app.data.local.entity.HabitEntity
import com.unotangozero.app.data.local.entity.TaskEntity
import com.unotangozero.app.data.local.entity.TransactionEntity

@Database(
    entities = [
        AccountEntity::class,
        BillEntity::class,
        BudgetEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        GoalEntity::class,
        HabitEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        DebtEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun billDao(): BillDao
    abstract fun budgetDao(): BudgetDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun goalDao(): GoalDao
    abstract fun habitDao(): HabitDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun debtDao(): DebtDao
}
