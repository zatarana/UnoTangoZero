package com.unotangozero.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [Index("type"), Index("isArchived")]
)
data class AccountEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val initialBalanceInCents: Long,
    val isArchived: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "bills",
    indices = [Index("dueDateMillis"), Index("status"), Index("categoryId"), Index("accountId")]
)
data class BillEntity(
    @PrimaryKey val id: String,
    val description: String,
    val amountInCents: Long,
    val dueDateMillis: Long,
    val paidDateMillis: Long? = null,
    val status: String,
    val categoryId: String? = null,
    val accountId: String? = null,
    val isRecurring: Boolean = false,
    val recurrenceRule: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["categoryId", "monthYear"], unique = true), Index("monthYear")]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val categoryId: String,
    val monthYear: String,
    val limitInCents: Long,
    val alertPercent: Int = 80,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "categories",
    indices = [Index("type"), Index("name")]
)
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val colorArgb: Long? = null,
    val iconName: String? = null,
    val isArchived: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "transactions",
    indices = [Index("dateMillis"), Index("type"), Index("categoryId"), Index("accountId"), Index("installmentGroupId")]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val type: String,
    val description: String,
    val amountInCents: Long,
    val dateMillis: Long,
    val categoryId: String? = null,
    val accountId: String? = null,
    val fromAccountId: String? = null,
    val toAccountId: String? = null,
    val installmentGroupId: String? = null,
    val installmentNumber: Int? = null,
    val installmentTotal: Int? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "goals",
    indices = [Index("type"), Index("deadlineMillis"), Index("status")]
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val title: String,
    val type: String,
    val targetValueInCents: Long? = null,
    val currentValueInCents: Long? = null,
    val targetCount: Int? = null,
    val completedCount: Int? = null,
    val deadlineMillis: Long? = null,
    val status: String,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "habits",
    indices = [Index("scheduledDateMillis"), Index("frequency"), Index("isArchived")]
)
data class HabitEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val frequency: String,
    val scheduledDateMillis: Long? = null,
    val reminderTimeMinutes: Int? = null,
    val streak: Int = 0,
    val completedCount: Int = 0,
    val isArchived: Boolean = false,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "tasks",
    indices = [Index("dueDateMillis"), Index("status"), Index("categoryId"), Index("priority"), Index("projectId")]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String? = null,
    val dueDateMillis: Long? = null,
    val reminderTimeMinutes: Int? = null,
    val status: String,
    val categoryId: String? = null,
    val priority: String,
    val projectId: String? = null,
    val recurrenceRule: String? = null,
    val estimatedDurationMinutes: Int = 0,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

@Entity(
    tableName = "focus_sessions",
    indices = [Index("taskId"), Index("projectId"), Index("startedAtMillis")]
)
data class FocusSessionEntity(
    @PrimaryKey val id: String,
    val taskId: String? = null,
    val projectId: String? = null,
    val startedAtMillis: Long,
    val endedAtMillis: Long? = null,
    val durationMinutes: Int,
    val note: String? = null,
    val createdAtMillis: Long
)

@Entity(
    tableName = "debts",
    indices = [Index("status"), Index("dueDateMillis"), Index("creditor")]
)
data class DebtEntity(
    @PrimaryKey val id: String,
    val creditor: String,
    val originalAmountInCents: Long,
    val remainingAmountInCents: Long,
    val dueDateMillis: Long,
    val status: String,
    val description: String? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
