package com.unotangozero.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.unotangozero.app.data.db.dao.BudgetDao
import com.unotangozero.app.data.db.dao.DebtDao
import com.unotangozero.app.data.db.dao.DebtPaymentDao
import com.unotangozero.app.data.db.dao.ExpenseDao
import com.unotangozero.app.data.db.dao.HabitDao
import com.unotangozero.app.data.db.dao.HabitLogDao
import com.unotangozero.app.data.db.dao.GoalDao
import com.unotangozero.app.data.db.dao.GoalStepDao
import com.unotangozero.app.data.db.dao.NoteDao
import com.unotangozero.app.data.db.dao.ReminderDao
import com.unotangozero.app.data.db.dao.ShoppingItemDao
import com.unotangozero.app.data.db.dao.ShoppingListDao
import com.unotangozero.app.data.db.dao.SubTaskDao
import com.unotangozero.app.data.db.dao.TaskDao
import com.unotangozero.app.data.db.entities.BudgetEntity
import com.unotangozero.app.data.db.entities.DebtEntity
import com.unotangozero.app.data.db.entities.DebtPaymentEntity
import com.unotangozero.app.data.db.entities.ExpenseEntity
import com.unotangozero.app.data.db.entities.HabitEntity
import com.unotangozero.app.data.db.entities.HabitLogEntity
import com.unotangozero.app.data.db.entities.NoteEntity
import com.unotangozero.app.data.db.entities.GoalEntity
import com.unotangozero.app.data.db.entities.GoalStepEntity
import com.unotangozero.app.data.db.entities.ReminderEntity
import com.unotangozero.app.data.db.entities.ShoppingItemEntity
import com.unotangozero.app.data.db.entities.ShoppingListEntity
import com.unotangozero.app.data.db.entities.SubTaskEntity
import com.unotangozero.app.data.db.entities.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        SubTaskEntity::class,
        ReminderEntity::class,
        ExpenseEntity::class,
        BudgetEntity::class,
        DebtEntity::class,
        DebtPaymentEntity::class,
        ShoppingListEntity::class,
        ShoppingItemEntity::class,
        HabitEntity::class,
        HabitLogEntity::class,
        NoteEntity::class,
        GoalEntity::class,
        GoalStepEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun subTaskDao(): SubTaskDao
    abstract fun reminderDao(): ReminderDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun debtDao(): DebtDao
    abstract fun debtPaymentDao(): DebtPaymentDao
    abstract fun shoppingListDao(): ShoppingListDao
    abstract fun shoppingItemDao(): ShoppingItemDao
    abstract fun habitDao(): HabitDao
    abstract fun habitLogDao(): HabitLogDao
    abstract fun noteDao(): NoteDao
    abstract fun goalDao(): GoalDao
    abstract fun goalStepDao(): GoalStepDao
}
