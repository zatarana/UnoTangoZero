package com.unotangozero.app.domain.usecases.habits

import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.repositories.HabitRepository
import javax.inject.Inject

/**
 * Use case for creating a new habit.
 */
class CreateHabitUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habit: Habit): Long {
        require(habit.name.isNotBlank()) { "Habit name cannot be empty" }
        require(habit.goal > 0) { "Habit goal must be greater than 0" }
        return habitRepository.createHabit(habit)
    }
}
