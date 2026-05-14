package com.unotangozero.app.domain.models

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain model representing a habit to track.
 */
data class Habit(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val frequency: HabitFrequency = HabitFrequency.DAILY,
    val trackingType: HabitTrackingType = HabitTrackingType.YES_NO,
    val goal: Int = 1, // Times per day/week/month
    val specificDays: Set<DayOfWeek> = emptySet(),
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 8,
    val reminderMinute: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCheckIn: LocalDate? = null,
    val color: String = "#FF6B6B",
    val icon: String = "favorite",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val active: Boolean = true,
    val checkIns: List<HabitCheckIn> = emptyList()
)

enum class HabitFrequency {
    DAILY, WEEKLY, MONTHLY, CUSTOM
}

enum class HabitTrackingType(val displayName: String) {
    YES_NO("Sim/não"),
    NUMERIC("Numérico")
}

data class HabitCheckIn(
    val id: Long = 0,
    val habitId: Long = 0,
    val date: LocalDate = LocalDate.now(),
    val completedTimes: Int = 1,
    val notes: String = ""
)
