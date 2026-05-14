package com.unotangozero.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.data.goals.GoalsRepository
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.HabitRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import com.unotangozero.app.presentation.common.UiState
import com.unotangozero.app.presentation.goals.GoalStepType
import com.unotangozero.app.presentation.goals.GoalUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    financialMovementRepository: FinancialMovementRepository,
    taskRepository: TaskRepository,
    habitRepository: HabitRepository,
    goalsRepository: GoalsRepository
) : ViewModel() {
    val uiState: StateFlow<UiState<DashboardUiState>> = combine(
        financialMovementRepository.accountBalances,
        financialMovementRepository.movements,
        taskRepository.getTasksDueToday(),
        taskRepository.getAllTasks(),
        habitRepository.getAllHabits(),
        goalsRepository.observeGoals()
    ) { accountBalances, movements, todayTasks, allTasks, habits, goals ->
        val today = LocalDate.now()
        val currentBalanceInCents = accountBalances.sumOf { it.currentBalanceInCents }
        val upcomingBills = movements
            .filter { movement ->
                movement.type == FinancialMovementType.EXPENSE && !movement.date.isBefore(today)
            }
            .sortedBy { it.date }
            .take(3)
            .map { movement ->
                UpcomingBillUi(
                    id = movement.id,
                    title = movement.description,
                    amountInCents = movement.amountInCents,
                    dueDate = movement.date
                )
            }
        val monthlyExpensesByCategory = movements
            .filter { movement ->
                movement.type == FinancialMovementType.EXPENSE &&
                    movement.date.year == today.year &&
                    movement.date.month == today.month
            }
            .groupBy { movement -> movement.category?.takeIf { it.isNotBlank() } ?: "Sem categoria" }
            .map { (category, categoryMovements) ->
                ExpenseCategorySliceUi(
                    category = category,
                    amountInCents = categoryMovements.sumOf { it.amountInCents }
                )
            }
            .filter { it.amountInCents > 0L }
            .sortedByDescending { it.amountInCents }

        UiState.Success(
            DashboardUiState(
                currentBalanceInCents = currentBalanceInCents,
                upcomingBills = upcomingBills,
                todayTasksCount = todayTasks.count { !it.completed },
                maxHabitStreak = habits.filter { it.active }.maxOfOrNull { it.currentStreak } ?: 0,
                averageGoalsProgressPercent = calculateAverageGoalsProgressPercent(
                    goals = goals,
                    tasks = allTasks,
                    habits = habits
                ),
                monthlyExpenseSlices = monthlyExpensesByCategory
            )
        )
    }
        .catch { emit(UiState.Error(it.message ?: "Não foi possível carregar o Dashboard.")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UiState.Loading
        )
}

data class DashboardUiState(
    val currentBalanceInCents: Long = 0L,
    val upcomingBills: List<UpcomingBillUi> = emptyList(),
    val todayTasksCount: Int = 0,
    val maxHabitStreak: Int = 0,
    val averageGoalsProgressPercent: Int = 0,
    val monthlyExpenseSlices: List<ExpenseCategorySliceUi> = emptyList()
)

data class UpcomingBillUi(
    val id: String,
    val title: String,
    val amountInCents: Long,
    val dueDate: LocalDate
)

data class ExpenseCategorySliceUi(
    val category: String,
    val amountInCents: Long
)

private fun calculateAverageGoalsProgressPercent(
    goals: List<GoalUi>,
    tasks: List<Task>,
    habits: List<Habit>
): Int {
    if (goals.isEmpty()) return 0

    val progressValues = goals.map { goal ->
        calculateGoalProgressPercent(
            goal = goal,
            tasks = tasks,
            habits = habits
        )
    }

    return progressValues.average().toInt().coerceIn(0, 100)
}

private fun calculateGoalProgressPercent(
    goal: GoalUi,
    tasks: List<Task>,
    habits: List<Habit>
): Int {
    if (goal.steps.isEmpty()) return 0

    val completedSteps = goal.steps.count { step ->
        when (step.type) {
            GoalStepType.TASK -> tasks.any { task ->
                task.title.equals(step.title, ignoreCase = true) &&
                    task.description.contains(goal.title, ignoreCase = true) &&
                    task.completed
            }
            GoalStepType.HABIT -> habits.any { habit ->
                habit.name.equals(step.title, ignoreCase = true) &&
                    habit.description.contains(goal.title, ignoreCase = true) &&
                    (habit.checkIns.isNotEmpty() || habit.lastCheckIn != null || habit.currentStreak > 0)
            }
        }
    }

    return ((completedSteps.toDouble() / goal.steps.size.toDouble()) * 100).toInt().coerceIn(0, 100)
}
