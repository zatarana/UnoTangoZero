package com.unotangozero.app.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteById(taskId: String)

    @Query("UPDATE tasks SET isCompleted = :completed, updatedAt = :updatedAt WHERE id = :taskId")
    suspend fun updateCompleted(taskId: String, completed: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeById(taskId: String): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE dueDate >= :startDate AND dueDate <= :endDate ORDER BY dueDate ASC, dueTime ASC")
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC, dueTime ASC")
    fun observePending(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY dueDate ASC, dueTime ASC")
    fun observeAll(): Flow<List<TaskEntity>>
}

@Dao
interface SubTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subTask: SubTaskEntity)

    @Update
    suspend fun update(subTask: SubTaskEntity)

    @Query("DELETE FROM subtasks WHERE id = :subTaskId")
    suspend fun deleteById(subTaskId: String)

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY title ASC")
    fun observeByTaskId(taskId: String): Flow<List<SubTaskEntity>>
}

@Dao
interface ReminderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reminder: ReminderEntity)

    @Update
    suspend fun update(reminder: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteById(reminderId: String)

    @Query("UPDATE reminders SET isActive = 0 WHERE id = :reminderId")
    suspend fun deactivate(reminderId: String)

    @Query("SELECT * FROM reminders WHERE taskId = :taskId ORDER BY reminderTime ASC")
    fun observeByTaskId(taskId: String): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isActive = 1 ORDER BY reminderTime ASC")
    fun observeActive(): Flow<List<ReminderEntity>>
}

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: ExpenseEntity)

    @Update
    suspend fun update(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteById(expenseId: String)

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    fun observeById(expenseId: String): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun observeByDateRange(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amountInCents), 0) FROM expenses WHERE date >= :startDate AND date <= :endDate")
    fun observeTotalByDateRange(startDate: Long, endDate: Long): Flow<Long>
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: BudgetEntity)

    @Update
    suspend fun update(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :budgetId")
    suspend fun deleteById(budgetId: String)

    @Query("SELECT * FROM budgets WHERE yearMonth = :yearMonth ORDER BY category ASC")
    fun observeByMonth(yearMonth: String): Flow<List<BudgetEntity>>
}

@Dao
interface DebtDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(debt: DebtEntity)

    @Update
    suspend fun update(debt: DebtEntity)

    @Query("DELETE FROM debts WHERE id = :debtId")
    suspend fun deleteById(debtId: String)

    @Query("SELECT * FROM debts WHERE id = :debtId")
    fun observeById(debtId: String): Flow<DebtEntity?>

    @Query("SELECT * FROM debts WHERE status != 'PAID' ORDER BY dueDate ASC")
    fun observeActive(): Flow<List<DebtEntity>>

    @Query("SELECT * FROM debts ORDER BY dueDate ASC")
    fun observeAll(): Flow<List<DebtEntity>>
}

@Dao
interface DebtPaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: DebtPaymentEntity)

    @Delete
    suspend fun delete(payment: DebtPaymentEntity)

    @Query("DELETE FROM debt_payments WHERE id = :paymentId")
    suspend fun deleteById(paymentId: String)

    @Query("SELECT * FROM debt_payments WHERE debtId = :debtId ORDER BY paymentDate DESC")
    fun observeByDebtId(debtId: String): Flow<List<DebtPaymentEntity>>
}

@Dao
interface ShoppingListDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(list: ShoppingListEntity)

    @Update
    suspend fun update(list: ShoppingListEntity)

    @Query("DELETE FROM shopping_lists WHERE id = :listId")
    suspend fun deleteById(listId: String)

    @Query("SELECT * FROM shopping_lists WHERE completedAt IS NULL ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<ShoppingListEntity>>

    @Query("SELECT * FROM shopping_lists ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ShoppingListEntity>>
}

@Dao
interface ShoppingItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ShoppingItemEntity)

    @Update
    suspend fun update(item: ShoppingItemEntity)

    @Query("DELETE FROM shopping_items WHERE id = :itemId")
    suspend fun deleteById(itemId: String)

    @Query("SELECT * FROM shopping_items WHERE listId = :listId ORDER BY isPurchased ASC, itemName ASC")
    fun observeByListId(listId: String): Flow<List<ShoppingItemEntity>>
}

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :habitId")
    suspend fun deleteById(habitId: String)

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt DESC")
    fun observeActive(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<HabitEntity>>
}

@Dao
interface HabitLogDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: HabitLogEntity)

    @Query("DELETE FROM habit_logs WHERE id = :logId")
    suspend fun deleteById(logId: String)

    @Query("SELECT * FROM habit_logs WHERE habitId = :habitId ORDER BY completedDate DESC")
    fun observeByHabitId(habitId: String): Flow<List<HabitLogEntity>>
}

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :noteId")
    suspend fun deleteById(noteId: String)

    @Query("UPDATE notes SET isPinned = :isPinned, updatedAt = :updatedAt WHERE id = :noteId")
    suspend fun updatePinned(noteId: String, isPinned: Boolean, updatedAt: Long = System.currentTimeMillis())

    @Query("SELECT * FROM notes WHERE id = :noteId")
    fun observeById(noteId: String): Flow<NoteEntity?>

    @Query("SELECT * FROM notes ORDER BY isPinned DESC, updatedAt DESC")
    fun observeAll(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE title LIKE :query OR content LIKE :query ORDER BY updatedAt DESC")
    fun search(query: String): Flow<List<NoteEntity>>
}
