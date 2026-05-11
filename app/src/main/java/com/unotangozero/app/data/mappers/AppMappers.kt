package com.unotangozero.app.data.mappers

import com.unotangozero.app.data.db.entities.BudgetEntity
import com.unotangozero.app.data.db.entities.DebtEntity
import com.unotangozero.app.data.db.entities.DebtPaymentEntity
import com.unotangozero.app.data.db.entities.ExpenseEntity
import com.unotangozero.app.data.db.entities.HabitEntity
import com.unotangozero.app.data.db.entities.HabitLogEntity
import com.unotangozero.app.data.db.entities.NoteEntity
import com.unotangozero.app.data.db.entities.ReminderEntity
import com.unotangozero.app.data.db.entities.ShoppingItemEntity
import com.unotangozero.app.data.db.entities.ShoppingListEntity
import com.unotangozero.app.data.db.entities.SubTaskEntity
import com.unotangozero.app.data.db.entities.TaskEntity
import com.unotangozero.app.domain.enums.DebtStatus
import com.unotangozero.app.domain.enums.ExpenseCategory
import com.unotangozero.app.domain.enums.HabitFrequency
import com.unotangozero.app.domain.enums.Priority
import com.unotangozero.app.domain.enums.RecurrenceType
import com.unotangozero.app.domain.enums.ReminderType
import com.unotangozero.app.domain.enums.TaskCategory
import com.unotangozero.app.domain.models.Budget
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtPayment
import com.unotangozero.app.domain.models.Expense
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitLog
import com.unotangozero.app.domain.models.Note
import com.unotangozero.app.domain.models.Reminder
import com.unotangozero.app.domain.models.ShoppingItem
import com.unotangozero.app.domain.models.ShoppingList
import com.unotangozero.app.domain.models.SubTask
import com.unotangozero.app.domain.models.Task
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()
fun Long.toLocalDateTime(): LocalDateTime = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
fun LocalDate.toEpochMillis(): Long = atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
fun LocalDateTime.toEpochMillis(): Long = atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

private fun String.toTagList(): List<String> = split(",").map { it.trim() }.filter { it.isNotBlank() }
private fun List<String>.toTagString(): String = joinToString(",")

fun TaskEntity.toDomain(
    subtasks: List<SubTask> = emptyList(),
    reminders: List<Reminder> = emptyList()
): Task = Task(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate.toLocalDate(),
    dueTime = dueTime?.let { LocalTime.parse(it) },
    category = TaskCategory.valueOf(category),
    priority = Priority.valueOf(priority),
    isCompleted = isCompleted,
    isRecurring = isRecurring,
    recurrenceType = recurrenceType?.let { RecurrenceType.valueOf(it) },
    recurrenceEndDate = recurrenceEndDate?.toLocalDate(),
    subtasks = subtasks,
    reminders = reminders,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime()
)

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    title = title,
    description = description,
    dueDate = dueDate.toEpochMillis(),
    dueTime = dueTime?.toString(),
    category = category.name,
    priority = priority.name,
    isCompleted = isCompleted,
    isRecurring = isRecurring,
    recurrenceType = recurrenceType?.name,
    recurrenceEndDate = recurrenceEndDate?.toEpochMillis(),
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)

fun SubTaskEntity.toDomain(): SubTask = SubTask(id = id, taskId = taskId, title = title, isCompleted = isCompleted)
fun SubTask.toEntity(): SubTaskEntity = SubTaskEntity(id = id, taskId = taskId, title = title, isCompleted = isCompleted)

fun ReminderEntity.toDomain(): Reminder = Reminder(
    id = id,
    taskId = taskId,
    reminderTime = reminderTime.toLocalDateTime(),
    reminderType = ReminderType.valueOf(reminderType),
    isActive = isActive,
    voiceNotePath = voiceNotePath,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    recurrencePattern = recurrencePattern,
    createdAt = createdAt.toLocalDateTime()
)

fun Reminder.toEntity(): ReminderEntity = ReminderEntity(
    id = id,
    taskId = taskId,
    reminderTime = reminderTime.toEpochMillis(),
    reminderType = reminderType.name,
    isActive = isActive,
    voiceNotePath = voiceNotePath,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    recurrencePattern = recurrencePattern,
    createdAt = createdAt.toEpochMillis()
)

fun ExpenseEntity.toDomain(): Expense = Expense(
    id = id,
    amountInCents = amountInCents,
    category = ExpenseCategory.valueOf(category),
    description = description,
    date = date.toLocalDate(),
    receiptImagePath = receiptImagePath,
    tags = tags.toTagList(),
    createdAt = createdAt.toLocalDateTime()
)

fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
    id = id,
    amountInCents = amountInCents,
    category = category.name,
    description = description,
    date = date.toEpochMillis(),
    receiptImagePath = receiptImagePath,
    tags = tags.toTagString(),
    createdAt = createdAt.toEpochMillis()
)

fun BudgetEntity.toDomain(): Budget = Budget(
    id = id,
    yearMonth = yearMonth,
    category = ExpenseCategory.valueOf(category),
    limitAmountInCents = limitAmountInCents,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime()
)

fun Budget.toEntity(): BudgetEntity = BudgetEntity(
    id = id,
    yearMonth = yearMonth,
    category = category.name,
    limitAmountInCents = limitAmountInCents,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)

fun DebtEntity.toDomain(paymentHistory: List<DebtPayment> = emptyList()): Debt = Debt(
    id = id,
    creditor = creditor,
    originalAmountInCents = originalAmountInCents,
    remainingAmountInCents = remainingAmountInCents,
    dueDate = dueDate.toLocalDate(),
    monthlyInterestRate = monthlyInterestRate,
    interestAccumulatedInCents = interestAccumulatedInCents,
    status = DebtStatus.valueOf(status),
    description = description,
    paymentHistory = paymentHistory,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime()
)

fun Debt.toEntity(): DebtEntity = DebtEntity(
    id = id,
    creditor = creditor,
    originalAmountInCents = originalAmountInCents,
    remainingAmountInCents = remainingAmountInCents,
    dueDate = dueDate.toEpochMillis(),
    monthlyInterestRate = monthlyInterestRate,
    interestAccumulatedInCents = interestAccumulatedInCents,
    status = status.name,
    description = description,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)

fun DebtPaymentEntity.toDomain(): DebtPayment = DebtPayment(
    id = id,
    debtId = debtId,
    amountInCents = amountInCents,
    paymentDate = paymentDate.toLocalDate(),
    note = note,
    createdAt = createdAt.toLocalDateTime()
)

fun DebtPayment.toEntity(): DebtPaymentEntity = DebtPaymentEntity(
    id = id,
    debtId = debtId,
    amountInCents = amountInCents,
    paymentDate = paymentDate.toEpochMillis(),
    note = note,
    createdAt = createdAt.toEpochMillis()
)

fun ShoppingListEntity.toDomain(items: List<ShoppingItem> = emptyList()): ShoppingList = ShoppingList(
    id = id,
    name = name,
    items = items,
    createdAt = createdAt.toLocalDateTime(),
    completedAt = completedAt?.toLocalDateTime()
)

fun ShoppingList.toEntity(): ShoppingListEntity = ShoppingListEntity(
    id = id,
    name = name,
    createdAt = createdAt.toEpochMillis(),
    completedAt = completedAt?.toEpochMillis()
)

fun ShoppingItemEntity.toDomain(): ShoppingItem = ShoppingItem(
    id = id,
    listId = listId,
    itemName = itemName,
    quantity = quantity,
    unit = unit,
    estimatedPriceInCents = estimatedPriceInCents,
    category = category,
    isPurchased = isPurchased,
    purchasedPriceInCents = purchasedPriceInCents,
    purchasedAt = purchasedAt?.toLocalDateTime()
)

fun ShoppingItem.toEntity(): ShoppingItemEntity = ShoppingItemEntity(
    id = id,
    listId = listId,
    itemName = itemName,
    quantity = quantity,
    unit = unit,
    estimatedPriceInCents = estimatedPriceInCents,
    category = category,
    isPurchased = isPurchased,
    purchasedPriceInCents = purchasedPriceInCents,
    purchasedAt = purchasedAt?.toEpochMillis()
)

fun HabitEntity.toDomain(): Habit = Habit(
    id = id,
    name = name,
    description = description,
    frequency = HabitFrequency.valueOf(frequency),
    targetDaysPerWeek = targetDaysPerWeek,
    colorHex = colorHex,
    isActive = isActive,
    createdAt = createdAt.toLocalDateTime()
)

fun Habit.toEntity(): HabitEntity = HabitEntity(
    id = id,
    name = name,
    description = description,
    frequency = frequency.name,
    targetDaysPerWeek = targetDaysPerWeek,
    colorHex = colorHex,
    isActive = isActive,
    createdAt = createdAt.toEpochMillis()
)

fun HabitLogEntity.toDomain(): HabitLog = HabitLog(
    id = id,
    habitId = habitId,
    completedDate = completedDate.toLocalDate(),
    note = note,
    createdAt = createdAt.toLocalDateTime()
)

fun HabitLog.toEntity(): HabitLogEntity = HabitLogEntity(
    id = id,
    habitId = habitId,
    completedDate = completedDate.toEpochMillis(),
    note = note,
    createdAt = createdAt.toEpochMillis()
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    content = content,
    tags = tags.toTagList(),
    isPinned = isPinned,
    colorHex = colorHex,
    createdAt = createdAt.toLocalDateTime(),
    updatedAt = updatedAt.toLocalDateTime()
)

fun Note.toEntity(): NoteEntity = NoteEntity(
    id = id,
    title = title,
    content = content,
    tags = tags.toTagString(),
    isPinned = isPinned,
    colorHex = colorHex,
    createdAt = createdAt.toEpochMillis(),
    updatedAt = updatedAt.toEpochMillis()
)
