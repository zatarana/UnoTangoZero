package com.unotangozero.app.domain.usecases.habits

import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.repositories.HabitRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching all habits.
 */
class GetHabitsUseCase @Inject constructor(
    private val habitRepository: HabitRepository
) {
    operator fun invoke(): Flow<List<Habit>> {
        return habitRepository.getAllHabits()
    }
}
