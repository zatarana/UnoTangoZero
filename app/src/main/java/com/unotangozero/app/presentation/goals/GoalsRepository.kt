package com.unotangozero.app.presentation.goals

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalsRepository @Inject constructor() {
    private val _goals = MutableStateFlow<List<GoalUi>>(emptyList())
    val goals: StateFlow<List<GoalUi>> = _goals.asStateFlow()

    fun createGoal(goal: GoalUi) {
        _goals.value = _goals.value + goal
    }

    fun deleteGoal(goalId: String) {
        _goals.value = _goals.value.filterNot { it.id == goalId }
    }
}
