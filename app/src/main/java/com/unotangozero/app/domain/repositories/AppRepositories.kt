package com.unotangozero.app.domain.repositories

import com.unotangozero.app.domain.models.Budget
import com.unotangozero.app.domain.models.BudgetStatus
import com.unotangozero.app.domain.models.DashboardSummary
import com.unotangozero.app.domain.models.Debt
import com.unotangozero.app.domain.models.DebtPayment
import com.unotangozero.app.domain.models.DebtSummary
import com.unotangozero.app.domain.models.Expense
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitLog
import com.unotangozero.app.domain.models.HabitWithStats
import com.unotangozero.app.domain.models.Note
import com.unotangozero.app.domain.models.Reminder
import com.unotangozero.app.domain.models.ShoppingItem
import com.unotangozero.app.domain.models.ShoppingList
import com.unotangozero.app.domain.models.SubTask
import com.unotangozero.app.domain.models.Task
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

interface TaskRepository {
    suspend fun save(task: Task): Result<Unit>
    suspend fun delete(taskId: String): Result<Unit>
    suspend fun setCompleted(taskId: String, completed: Boolean): Result<Unit>
    fun observeById(taskId: String): Flow<Task?>
    fun observeToday(): Flow<List<Task>>
    fun observeByDateRange(startDate: LocalDate, endDate: LocalDate): Flow<List<Task>>
    fun observePending(): Flow<List<Task>>
    fun observeAll(): Flow<List<Task>>
}

interface SubTaskRepository {
    suspend fun save(subTask: SubTask): Result<Unit>
    suspend fun delete(subTaskId: String): Result<Unit>
    fun observeByTaskId(taskId: String): Flow<List<SubTask>>
}

interface ReminderRepository {
    suspend fun save(reminder: Reminder): Result<Unit>
    suspend fun delete(reminderId: String): Result<Unit>
    suspend fun deactivate(reminderId: String): Result<Unit>
    fun observeActive(): Flow<List<Reminder>>
    fun observeByTaskId(taskId: String): Flow<List<Reminder>>
}

interface ExpenseRepository {
    suspend fun save(expense: Expense): Result<Unit>
    suspend fun delete(expenseId: String): Result<Unit>
    fun observeById(expenseId: String): Flow<Expense?>
    fun observeByMonth(month: YearMonth): Flow<List<Expense>>
    fun observeByDate(date: LocalDate): Flow<List<Expense>>
    fun observeTotalByDate(date: LocalDate): Flow<Long>
}

interface BudgetRepository {
    suspend fun save(budget: Budget): Result<Unit>
    suspend fun delete(budgetId: String): Result<Unit>
    fun observeByMonth(month: YearMonth): Flow<List<Budget>>
    fun observeBudgetStatus(month: YearMonth): Flow<List<BudgetStatus>>
}

interface DebtRepository {
    suspend fun save(debt: Debt): Result<Unit>
    suspend fun delete(debtId: String): Result<Unit>
    suspend fun recordPayment(payment: DebtPayment): Result<Unit>
    fun observeById(debtId: String): Flow<Debt?>
    fun observeActive(): Flow<List<Debt>>
    fun observeAll(): Flow<List<Debt>>
    fun observeSummary(): Flow<DebtSummary>
}

interface ShoppingRepository {
    suspend fun saveList(list: ShoppingList): Result<Unit>
    suspend fun deleteList(listId: String): Result<Unit>
    suspend fun saveItem(item: ShoppingItem): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    fun observeActiveLists(): Flow<List<ShoppingList>>
    fun observeAllLists(): Flow<List<ShoppingList>>
    fun observeItems(listId: String): Flow<List<ShoppingItem>>
}

interface HabitRepository {
    suspend fun save(habit: Habit): Result<Unit>
    suspend fun delete(habitId: String): Result<Unit>
    suspend fun log(log: HabitLog): Result<Unit>
    suspend fun deleteLog(logId: String): Result<Unit>
    fun observeActive(): Flow<List<Habit>>
    fun observeAll(): Flow<List<Habit>>
    fun observeLogs(habitId: String): Flow<List<HabitLog>>
    fun observeWithStats(habit: Habit): Flow<HabitWithStats>
}

interface NoteRepository {
    suspend fun save(note: Note): Result<Unit>
    suspend fun delete(noteId: String): Result<Unit>
    suspend fun setPinned(noteId: String, pinned: Boolean): Result<Unit>
    fun observeById(noteId: String): Flow<Note?>
    fun observeAll(): Flow<List<Note>>
    fun search(query: String): Flow<List<Note>>
}

interface DashboardRepository {
    fun observeSummary(): Flow<DashboardSummary>
}
