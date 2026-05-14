package com.unotangozero.app.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unotangozero.app.domain.models.Habit
import com.unotangozero.app.domain.models.HabitFrequency
import com.unotangozero.app.domain.models.Priority
import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.HabitRepository
import com.unotangozero.app.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.round

enum class GoalStepType(val displayName: String) {
    HABIT("Hábito"),
    TASK("Tarefa")
}

data class GoalStepUi(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val type: GoalStepType
)

data class GoalUi(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val targetValueInCents: Long?,
    val deadline: LocalDate,
    val colorHex: String,
    val steps: List<GoalStepUi> = emptyList(),
    val createdAt: LocalDate = LocalDate.now()
)

data class GoalFormUiState(
    val title: String = "",
    val description: String = "",
    val targetValueText: String = "",
    val deadline: LocalDate = LocalDate.now().plusMonths(1),
    val colorHex: String = "#6750A4",
    val stepTitle: String = "",
    val stepType: GoalStepType = GoalStepType.HABIT,
    val steps: List<GoalStepUi> = emptyList()
)

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val habitRepository: HabitRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    private val _goals = MutableStateFlow<List<GoalUi>>(emptyList())
    val goals: StateFlow<List<GoalUi>> = _goals.asStateFlow()

    private val _formState = MutableStateFlow(GoalFormUiState())
    val formState: StateFlow<GoalFormUiState> = _formState.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun onTitleChange(value: String) {
        _formState.value = _formState.value.copy(title = value)
    }

    fun onDescriptionChange(value: String) {
        _formState.value = _formState.value.copy(description = value)
    }

    fun onTargetValueChange(value: String) {
        _formState.value = _formState.value.copy(targetValueText = value.filterMoneyChars())
    }

    fun onDeadlineChange(value: LocalDate) {
        _formState.value = _formState.value.copy(deadline = value)
    }

    fun onColorChange(value: String) {
        _formState.value = _formState.value.copy(colorHex = value)
    }

    fun onStepTitleChange(value: String) {
        _formState.value = _formState.value.copy(stepTitle = value)
    }

    fun onStepTypeChange(value: GoalStepType) {
        _formState.value = _formState.value.copy(stepType = value)
    }

    fun addStep() {
        val state = _formState.value
        val stepTitle = state.stepTitle.trim()
        if (stepTitle.isBlank()) {
            _message.value = "Digite o nome do passo."
            return
        }

        _formState.value = state.copy(
            stepTitle = "",
            steps = state.steps + GoalStepUi(
                title = stepTitle,
                type = state.stepType
            )
        )
    }

    fun removeStep(stepId: String) {
        _formState.value = _formState.value.copy(
            steps = _formState.value.steps.filterNot { it.id == stepId }
        )
    }

    fun createGoal() {
        val state = _formState.value
        val title = state.title.trim()
        val description = state.description.trim()
        val targetValue = state.targetValueText.takeIf { it.isNotBlank() }?.let { parseMoneyToCents(it) }

        if (title.isBlank()) {
            _message.value = "Digite o título da meta."
            return
        }

        if (state.targetValueText.isNotBlank() && (targetValue == null || targetValue <= 0L)) {
            _message.value = "Digite um valor alvo válido ou deixe em branco."
            return
        }

        val goal = GoalUi(
            title = title,
            description = description,
            targetValueInCents = targetValue,
            deadline = state.deadline,
            colorHex = state.colorHex,
            steps = state.steps
        )

        _goals.value = _goals.value + goal
        _formState.value = GoalFormUiState()

        viewModelScope.launch {
            val createdSteps = generateLinkedSteps(goal)
            _message.value = if (goal.steps.isEmpty()) {
                "Meta criada."
            } else {
                "Meta criada com $createdSteps de ${goal.steps.size} passo(s) vinculado(s)."
            }
        }
    }

    fun deleteGoal(goalId: String) {
        _goals.value = _goals.value.filterNot { it.id == goalId }
        _message.value = "Meta excluída."
    }

    fun clearMessage() {
        _message.value = null
    }

    private suspend fun generateLinkedSteps(goal: GoalUi): Int {
        var created = 0
        goal.steps.forEach { step ->
            val result = when (step.type) {
                GoalStepType.HABIT -> runCatching {
                    habitRepository.createHabit(
                        Habit(
                            name = step.title,
                            description = "Passo gerado automaticamente pela meta: ${goal.title}",
                            frequency = HabitFrequency.DAILY,
                            goal = 1,
                            color = goal.colorHex,
                            icon = "flag"
                        )
                    )
                }
                GoalStepType.TASK -> runCatching {
                    taskRepository.createTask(
                        Task(
                            title = step.title,
                            description = "Passo gerado automaticamente pela meta: ${goal.title}",
                            priority = Priority.MEDIUM,
                            dueDate = goal.deadline.atTime(9, 0),
                            remindAt = goal.deadline.atTime(9, 0),
                            tags = listOf("meta", goal.title.lowercase().take(24))
                        )
                    )
                }
            }
            if (result.isSuccess) created++
        }
        return created
    }

    private fun String.filterMoneyChars(): String = filter { it.isDigit() || it == ',' || it == '.' }

    private fun parseMoneyToCents(rawValue: String): Long {
        val normalized = rawValue.trim().replace(".", "").replace(",", ".")
        val amount = normalized.toDoubleOrNull() ?: return 0L
        return round(amount * 100).toLong()
    }
}
