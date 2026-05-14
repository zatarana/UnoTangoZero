package com.unotangozero.app.data.datasources.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotangozero.app.data.models.entities.HabitCheckInEntity
import com.unotangozero.app.data.models.entities.HabitEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Data Access Object for Habit operations.
 */
@Dao
interface HabitDao {
    @Query("SELECT * FROM habits WHERE active = 1 ORDER BY name ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Long): HabitEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    suspend fun updateHabit(habit: HabitEntity)

    @Delete
    suspend fun deleteHabit(habit: HabitEntity)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    // Habit Check-in operations
    @Query("SELECT * FROM habit_check_ins WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getHabitCheckIns(habitId: Long, startDate: LocalDate, endDate: LocalDate): Flow<List<HabitCheckInEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCheckIn(checkIn: HabitCheckInEntity): Long

    @Query("SELECT COUNT(*) FROM habit_check_ins WHERE habitId = :habitId AND date = :date")
    suspend fun hasCheckInForDate(habitId: Long, date: LocalDate): Boolean

    @Query("SELECT MAX(date) FROM habit_check_ins WHERE habitId = :habitId")
    suspend fun getLastCheckInDate(habitId: Long): LocalDate?
}
