package com.unotangozero.app.domain.usecases.habits

import com.unotangozero.app.domain.repositories.HabitRepository
import javax.inject.Inject

/**
 * Use case for calculating the current streak of a habit.
 */
class CalculateStreakUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long): Int {
        require(habitId > 0) { "Habit ID must be valid" }
        return habitRepository.calculateStreak(habitId)
    }
}
