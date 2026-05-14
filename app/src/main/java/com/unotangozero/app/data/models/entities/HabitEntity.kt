package com.unotangozero.app.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity representing a Habit.
 */
@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val frequency: String = "DAILY",
    val goal: Int = 1,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckIn: LocalDate? = null,
    val color: String = "#FF6B6B",
    val icon: String = "favorite",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val active: Boolean = true
)

@Entity(tableName = "habit_check_ins")
data class HabitCheckInEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val habitId: Long,
    val date: LocalDate,
    val completedTimes: Int = 1,
    val notes: String = ""
)
