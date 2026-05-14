package com.unotangozero.app.data.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["dueDate"]),
        Index(value = ["category"]),
        Index(value = ["isCompleted"])
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val dueDate: Long,
    val dueTime: String? = null,
    val category: String,
    val priority: String,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceType: String? = null,
    val recurrenceEndDate: Long? = null,
    val estimatedDurationMinutes: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "subtasks",
    foreignKeys = [ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["taskId"])]
)
data class SubTaskEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(
    tableName = "reminders",
    foreignKeys = [ForeignKey(entity = TaskEntity::class, parentColumns = ["id"], childColumns = ["taskId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["taskId"]), Index(value = ["reminderTime"]), Index(value = ["isActive"])]
)
data class ReminderEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val reminderTime: Long,
    val reminderType: String,
    val isActive: Boolean = true,
    val voiceNotePath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Int = 100,
    val recurrencePattern: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "expenses",
    indices = [Index(value = ["date"]), Index(value = ["category"]), Index(value = ["createdAt"])]
)
data class ExpenseEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val amountInCents: Long,
    val category: String,
    val description: String,
    val date: Long,
    val receiptImagePath: String? = null,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["yearMonth", "category"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val yearMonth: String,
    val category: String,
    val limitAmountInCents: Long,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debts",
    indices = [Index(value = ["dueDate"]), Index(value = ["status"]), Index(value = ["createdAt"])]
)
data class DebtEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val creditor: String,
    val originalAmountInCents: Long,
    val remainingAmountInCents: Long,
    val dueDate: Long,
    val monthlyInterestRate: Double = 0.0,
    val interestAccumulatedInCents: Long = 0L,
    val status: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "debt_payments",
    foreignKeys = [ForeignKey(entity = DebtEntity::class, parentColumns = ["id"], childColumns = ["debtId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["debtId"]), Index(value = ["paymentDate"])]
)
data class DebtPaymentEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val debtId: String,
    val amountInCents: Long,
    val paymentDate: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)

@Entity(
    tableName = "shopping_items",
    foreignKeys = [ForeignKey(entity = ShoppingListEntity::class, parentColumns = ["id"], childColumns = ["listId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["listId"]), Index(value = ["isPurchased"])]
)
data class ShoppingItemEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val itemName: String,
    val quantity: Double = 1.0,
    val unit: String = "un",
    val estimatedPriceInCents: Long? = null,
    val category: String? = null,
    val isPurchased: Boolean = false,
    val purchasedPriceInCents: Long? = null,
    val purchasedAt: Long? = null
)

@Entity(tableName = "habits", indices = [Index(value = ["isActive"])])
data class HabitEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val frequency: String,
    val targetDaysPerWeek: Int? = null,
    val colorHex: String = "#4CAF50",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "habit_logs",
    foreignKeys = [ForeignKey(entity = HabitEntity::class, parentColumns = ["id"], childColumns = ["habitId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["habitId", "completedDate"], unique = true)]
)
data class HabitLogEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val completedDate: Long,
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notes", indices = [Index(value = ["createdAt"]), Index(value = ["isPinned"])])
data class NoteEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val tags: String = "",
    val isPinned: Boolean = false,
    val colorHex: String = "#FFFFFF",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "goals", indices = [Index(value = ["deadline"]), Index(value = ["createdAt"])])
data class GoalEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetValueInCents: Long?,
    val deadline: Long,
    val colorHex: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "goal_steps",
    foreignKeys = [ForeignKey(entity = GoalEntity::class, parentColumns = ["id"], childColumns = ["goalId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["goalId"])]
)
data class GoalStepEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val goalId: String,
    val title: String,
    val type: String
)
