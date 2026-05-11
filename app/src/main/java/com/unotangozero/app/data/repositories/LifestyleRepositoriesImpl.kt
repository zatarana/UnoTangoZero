package com.unotangozero.app.data.repositories

import com.unotangozero.app.data.db.dao.HabitDao
import com.unotangozero.app.data.db.dao.HabitLogDao
import com.unotangozero.app.data.db.dao.NoteDao
import com.unotangozero.app.data.db.dao.ShoppingItemDao
import com.unotangozero.app.data.db.dao.ShoppingListDao
import com.unotangozero.app.data.mappers.toDomain
import com.unotangozero.app.data.mappers.toEntity
import com.unotangozero.app.domain.models.DashboardSummary
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitLog
import com.unotangozero.app.domain.models.HabitWithStats
import com.unotangozero.app.domain.models.Note
import com.unotangozero.app.domain.models.ShoppingItem
import com.unotangozero.app.domain.models.ShoppingList
import com.unotangozero.app.domain.repositories.DashboardRepository
import com.unotangozero.app.domain.repositories.DebtRepository
import com.unotangozero.app.domain.repositories.ExpenseRepository
import com.unotangozero.app.domain.repositories.HabitRepository
import com.unotangozero.app.domain.repositories.NoteRepository
import com.unotangozero.app.domain.repositories.ShoppingRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject

class ShoppingRepositoryImpl @Inject constructor(
    private val shoppingListDao: ShoppingListDao,
    private val shoppingItemDao: ShoppingItemDao
) : ShoppingRepository {
    override suspend fun saveList(list: ShoppingList): Result<Unit> = runCatching {
        shoppingListDao.insert(list.toEntity())
    }

    override suspend fun deleteList(listId: String): Result<Unit> = runCatching {
        shoppingListDao.deleteById(listId)
    }

    override suspend fun saveItem(item: ShoppingItem): Result<Unit> = runCatching {
        shoppingItemDao.insert(item.toEntity())
    }

    override suspend fun deleteItem(itemId: String): Result<Unit> = runCatching {
        shoppingItemDao.deleteById(itemId)
    }

    override fun observeActiveLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.observeActive().map { lists ->
            lists.map { it.toDomain() }
        }
    }

    override fun observeAllLists(): Flow<List<ShoppingList>> {
        return shoppingListDao.observeAll().map { lists ->
            lists.map { it.toDomain() }
        }
    }

    override fun observeItems(listId: String): Flow<List<ShoppingItem>> {
        return shoppingItemDao.observeByListId(listId).map { items ->
            items.map { it.toDomain() }
        }
    }
}

class HabitRepositoryImpl @Inject constructor(
    private val habitDao: HabitDao,
    private val habitLogDao: HabitLogDao
) : HabitRepository {
    override suspend fun save(habit: Habit): Result<Unit> = runCatching {
        habitDao.insert(habit.toEntity())
    }

    override suspend fun delete(habitId: String): Result<Unit> = runCatching {
        habitDao.deleteById(habitId)
    }

    override suspend fun log(log: HabitLog): Result<Unit> = runCatching {
        habitLogDao.insert(log.toEntity())
    }

    override suspend fun deleteLog(logId: String): Result<Unit> = runCatching {
        habitLogDao.deleteById(logId)
    }

    override fun observeActive(): Flow<List<Habit>> {
        return habitDao.observeActive().map { habits -> habits.map { it.toDomain() } }
    }

    override fun observeAll(): Flow<List<Habit>> {
        return habitDao.observeAll().map { habits -> habits.map { it.toDomain() } }
    }

    override fun observeLogs(habitId: String): Flow<List<HabitLog>> {
        return habitLogDao.observeByHabitId(habitId).map { logs -> logs.map { it.toDomain() } }
    }

    override fun observeWithStats(habit: Habit): Flow<HabitWithStats> {
        return observeLogs(habit.id).map { logs ->
            HabitWithStats(
                habit = habit,
                logs = logs,
                currentStreak = calculateCurrentStreak(logs),
                longestStreak = calculateLongestStreak(logs),
                completionPercentageLast30Days = calculateCompletionPercentageLast30Days(logs)
            )
        }
    }

    private fun calculateCurrentStreak(logs: List<HabitLog>): Int {
        val dates = logs.map { it.completedDate }.toSet()
        var streak = 0
        var date = LocalDate.now()
        while (dates.contains(date)) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    private fun calculateLongestStreak(logs: List<HabitLog>): Int {
        val dates = logs.map { it.completedDate }.distinct().sorted()
        if (dates.isEmpty()) return 0

        var longest = 1
        var current = 1
        for (index in 1 until dates.size) {
            if (dates[index] == dates[index - 1].plusDays(1)) {
                current++
                longest = maxOf(longest, current)
            } else {
                current = 1
            }
        }
        return longest
    }

    private fun calculateCompletionPercentageLast30Days(logs: List<HabitLog>): Double {
        val today = LocalDate.now()
        val start = today.minusDays(29)
        val completed = logs
            .map { it.completedDate }
            .filter { it in start..today }
            .distinct()
            .size
        return completed / 30.0 * 100.0
    }
}

class NoteRepositoryImpl @Inject constructor(
    private val noteDao: NoteDao
) : NoteRepository {
    override suspend fun save(note: Note): Result<Unit> = runCatching {
        noteDao.insert(note.toEntity())
    }

    override suspend fun delete(noteId: String): Result<Unit> = runCatching {
        noteDao.deleteById(noteId)
    }

    override suspend fun setPinned(noteId: String, pinned: Boolean): Result<Unit> = runCatching {
        noteDao.updatePinned(noteId, pinned)
    }

    override fun observeById(noteId: String): Flow<Note?> {
        return noteDao.observeById(noteId).map { it?.toDomain() }
    }

    override fun observeAll(): Flow<List<Note>> {
        return noteDao.observeAll().map { notes -> notes.map { it.toDomain() } }
    }

    override fun search(query: String): Flow<List<Note>> {
        val normalizedQuery = "%${query.trim()}%"
        return noteDao.search(normalizedQuery).map { notes -> notes.map { it.toDomain() } }
    }
}

class DashboardRepositoryImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val debtRepository: DebtRepository,
    private val expenseRepository: ExpenseRepository,
    private val habitRepository: HabitRepository,
    private val shoppingRepository: ShoppingRepository
) : DashboardRepository {
    override fun observeSummary(): Flow<DashboardSummary> {
        return combine(
            taskRepository.observeToday(),
            debtRepository.observeSummary(),
            expenseRepository.observeTotalByDate(LocalDate.now()),
            habitRepository.observeActive(),
            shoppingRepository.observeActiveLists()
        ) { todayTasks, debtSummary, totalTodaySpent, activeHabits, activeShoppingLists ->
            DashboardSummary(
                todayTasks = todayTasks,
                debtSummary = debtSummary,
                totalTodaySpentInCents = totalTodaySpent,
                activeHabits = activeHabits,
                activeShoppingLists = activeShoppingLists
            )
        }
    }
}
