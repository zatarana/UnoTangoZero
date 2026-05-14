package com.unotangozero.app.data.goals

import com.unotangozero.app.data.db.dao.GoalDao
import com.unotangozero.app.data.db.dao.GoalStepDao
import com.unotangozero.app.data.db.entities.GoalEntity
import com.unotangozero.app.data.db.entities.GoalStepEntity
import com.unotangozero.app.presentation.goals.GoalStepType
import com.unotangozero.app.presentation.goals.GoalStepUi
import com.unotangozero.app.presentation.goals.GoalUi
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

@Singleton
class GoalsRepository @Inject constructor(
    private val goalDao: GoalDao,
    private val goalStepDao: GoalStepDao
) {
    fun observeGoals(): Flow<List<GoalUi>> = combine(goalDao.observeAll(), goalStepDao.observeAll()) { goals, steps ->
        goals.map { goal ->
            val goalSteps = steps.filter { it.goalId == goal.id }.map { step ->
                GoalStepUi(
                    id = step.id,
                    title = step.title,
                    type = GoalStepType.valueOf(step.type)
                )
            }
            goal.toUi(goalSteps)
        }
    }

    suspend fun saveGoal(goal: GoalUi) {
        goalDao.insert(goal.toEntity())
        goalStepDao.deleteByGoalId(goal.id)
        goalStepDao.insertAll(goal.steps.map { step ->
            GoalStepEntity(id = step.id, goalId = goal.id, title = step.title, type = step.type.name)
        })
    }

    suspend fun deleteGoal(goalId: String) {
        goalStepDao.deleteByGoalId(goalId)
        goalDao.deleteById(goalId)
    }
}

private fun GoalEntity.toUi(steps: List<GoalStepUi>): GoalUi = GoalUi(
    id = id,
    title = title,
    description = description,
    targetValueInCents = targetValueInCents,
    deadline = Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault()).toLocalDate(),
    colorHex = colorHex,
    steps = steps,
    createdAt = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
)

private fun GoalUi.toEntity(): GoalEntity = GoalEntity(
    id = id,
    title = title,
    description = description,
    targetValueInCents = targetValueInCents,
    deadline = deadline.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
    colorHex = colorHex,
    createdAt = createdAt.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
)
