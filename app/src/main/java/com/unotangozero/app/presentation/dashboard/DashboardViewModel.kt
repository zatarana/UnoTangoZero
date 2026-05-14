package com.unotangozero.app.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.data.finance.FinancialMovementRepository
import com.unotangozero.app.domain.models.FinancialMovementType
import com.unotangozero.app.domain.repositories.HabitRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import com.unotangozero.app.presentation.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    financialMovementRepository: FinancialMovementRepository,
    taskRepository: TaskRepository,
    habitRepository: HabitRepository
) : ViewModel() {
    val uiState: StateFlow<UiState<DashboardUiState>> = combine(
        financialMovementRepository.accountBalances,
        financialMovementRepository.movements,
        taskRepository.getTasksDueToday(),
        habitRepository.getAllHabits()
    ) { accountBalances, movements, todayTasks, habits ->
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

        UiState.Success(
            DashboardUiState(
                currentBalanceInCents = currentBalanceInCents,
                upcomingBills = upcomingBills,
                todayTasksCount = todayTasks.count { !it.completed },
                maxHabitStreak = habits.filter { it.active }.maxOfOrNull { it.currentStreak } ?: 0,
                averageGoalsProgressPercent = 0
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
    val averageGoalsProgressPercent: Int = 0
)

data class UpcomingBillUi(
    val id: String,
    val title: String,
    val amountInCents: Long,
    val dueDate: LocalDate
)
