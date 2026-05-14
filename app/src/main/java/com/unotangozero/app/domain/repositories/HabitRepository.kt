package com.unotangozero.app.domain.repositories

import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitCheckIn
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Repository interface for Habit operations.
 */
interface HabitRepository {
    /**
     * Get all active habits as a flow for real-time updates.
     */
    fun getAllHabits(): Flow<List<Habit>>

    /**
     * Get a specific habit by ID.
     */
    suspend fun getHabitById(id: Long): Habit?

    /**
     * Create a new habit.
     */
    suspend fun createHabit(habit: Habit): Long

    /**
     * Update an existing habit.
     */
    suspend fun updateHabit(habit: Habit)

    /**
     * Delete a habit.
     */
    suspend fun deleteHabit(id: Long)

    /**
     * Check in to a habit.
     */
    suspend fun checkInHabit(habitId: Long, date: LocalDate = LocalDate.now())

    /**
     * Get check-ins for a habit within a date range.
     */
    fun getHabitCheckIns(habitId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCheckIn>>

    /**
     * Calculate current streak for a habit.
     */
    suspend fun calculateStreak(habitId: Long): Int
}
