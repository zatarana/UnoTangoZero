package com.unotangozero.app.domain.models

import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.enums.ExpenseCategory
import com.unotangozero.app.domain.enums.HabitFrequency
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.enums.ReminderType
import com.unotangozero.app.domain.enums.TaskCategory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val dueDate: LocalDate,
    val dueTime: LocalTime? = null,
    val category: TaskCategory = TaskCategory.PERSONAL,
    val priority: Priority = Priority.MEDIUM,
    val isCompleted: Boolean = false,
    val isRecurring: Boolean = false,
    val recurrenceType: RecurrenceType? = null,
    val recurrenceEndDate: LocalDate? = null,
    val estimatedDurationMinutes: Int = 0,
    val subtasks: List<SubTask> = emptyList(),
    val reminders: List<Reminder> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class SubTask(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isCompleted: Boolean = false
)

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val reminderTime: LocalDateTime,
    val reminderType: ReminderType = ReminderType.ONE_TIME,
    val isActive: Boolean = true,
    val voiceNotePath: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val radiusMeters: Int = 100,
    val recurrencePattern: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amountInCents: Long,
    val category: ExpenseCategory,
    val description: String,
    val date: LocalDate,
    val receiptImagePath: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val yearMonth: String,
    val category: ExpenseCategory,
    val limitAmountInCents: Long,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class BudgetStatus(
    val category: ExpenseCategory,
    val limitAmountInCents: Long,
    val spentAmountInCents: Long,
) {
    val remainingAmountInCents: Long = limitAmountInCents - spentAmountInCents
    val percentageUsed: Double = if (limitAmountInCents > 0) {
        spentAmountInCents.toDouble() / limitAmountInCents.toDouble() * 100.0
    } else {
        0.0
    }
    val isOverBudget: Boolean = spentAmountInCents > limitAmountInCents
    val isWarning: Boolean = percentageUsed >= 80.0 && !isOverBudget
}

data class Debt(
    val id: String = UUID.randomUUID().toString(),
    val creditor: String,
    val originalAmountInCents: Long,
    val remainingAmountInCents: Long,
    val dueDate: LocalDate,
    val monthlyInterestRate: Double = 0.0,
    val interestAccumulatedInCents: Long = 0L,
    val status: DebtStatus = DebtStatus.PENDING,
    val description: String? = null,
    val paymentHistory: List<DebtPayment> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val totalDueInCents: Long = remainingAmountInCents + interestAccumulatedInCents
}

data class DebtPayment(
    val id: String = UUID.randomUUID().toString(),
    val debtId: String,
    val amountInCents: Long,
    val paymentDate: LocalDate,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class DebtSummary(
    val totalDebts: Int = 0,
    val activeDebts: Int = 0,
    val paidDebts: Int = 0,
    val totalDebtAmountInCents: Long = 0L,
    val totalDebtWithInterestInCents: Long = 0L,
    val nextDueDate: LocalDate? = null,
    val nextDueAmountInCents: Long = 0L
)

data class ShoppingList(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val items: List<ShoppingItem> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val completedAt: LocalDateTime? = null
) {
    val isCompleted: Boolean = completedAt != null
}

data class ShoppingItem(
    val id: String = UUID.randomUUID().toString(),
    val listId: String,
    val itemName: String,
    val quantity: Double = 1.0,
    val unit: String = "un",
    val estimatedPriceInCents: Long? = null,
    val category: String? = null,
    val isPurchased: Boolean = false,
    val purchasedPriceInCents: Long? = null,
    val purchasedAt: LocalDateTime? = null
)

data class Habit(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String? = null,
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val targetDaysPerWeek: Int? = null,
    val colorHex: String = "#4CAF50",
    val isActive: Boolean = true,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class HabitLog(
    val id: String = UUID.randomUUID().toString(),
    val habitId: String,
    val completedDate: LocalDate,
    val note: String? = null,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class HabitWithStats(
    val habit: Habit,
    val logs: List<HabitLog> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val completionPercentageLast30Days: Double = 0.0
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val tags: List<String> = emptyList(),
    val isPinned: Boolean = false,
    val colorHex: String = "#FFFFFF",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

data class DashboardSummary(
    val todayTasks: List<Task> = emptyList(),
    val debtSummary: DebtSummary = DebtSummary(),
    val totalTodaySpentInCents: Long = 0L,
    val activeHabits: List<Habit> = emptyList(),
    val activeShoppingLists: List<ShoppingList> = emptyList()
) {
    val todayTasksCompleted: Int = todayTasks.count { it.isCompleted }
    val todayTasksPending: Int = todayTasks.count { !it.isCompleted }
}
