package com.unotangozero.app.domain.usecases.habits

import com.unotangozero.app.domain.repositories.HabitRepository
import java.time.LocalDate
import javax.inject.Inject

/**
 * Use case for checking in to a habit.
 */
class CheckInHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long, date: LocalDate = LocalDate.now()) {
        require(habitId > 0) { "Habit ID must be valid" }
        habitRepository.checkInHabit(habitId, date)
    }
}
