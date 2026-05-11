package com.unotangozero.app.data.goals

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import com.unotangozero.app.domain.models.SavingsGoal
import com.unotangozero.app.domain.models.SavingsGoalDeposit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.goalsDataStore by preferencesDataStore(name = "savings_goals")

@Singleton
class SavingsGoalRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString) })
        .create()

    private val goalsKey = stringPreferencesKey("goals_json")
    private val logsKey = stringPreferencesKey("goal_logs_json")
    private val goalsType = object : TypeToken<List<SavingsGoal>>() {}.type
    private val logsType = object : TypeToken<List<SavingsGoalDeposit>>() {}.type

    val goals: Flow<List<SavingsGoal>> = context.goalsDataStore.data.map { preferences ->
        readGoals(preferences[goalsKey]).sortedWith(compareBy<SavingsGoal> { it.isCompleted }.thenBy { it.targetDate ?: LocalDate.MAX })
    }

    val goalLogs: Flow<List<SavingsGoalDeposit>> = context.goalsDataStore.data.map { preferences ->
        readLogs(preferences[logsKey]).sortedByDescending { it.date }
    }

    suspend fun saveGoal(goal: SavingsGoal): Result<Unit> = runCatching {
        context.goalsDataStore.edit { preferences ->
            val current = readGoals(preferences[goalsKey])
            preferences[goalsKey] = gson.toJson(current.filterNot { it.id == goal.id } + goal.copy(updatedAt = LocalDateTime.now()))
        }
    }

    suspend fun deleteGoal(goalId: String): Result<Unit> = runCatching {
        context.goalsDataStore.edit { preferences ->
            preferences[goalsKey] = gson.toJson(readGoals(preferences[goalsKey]).filterNot { it.id == goalId })
            preferences[logsKey] = gson.toJson(readLogs(preferences[logsKey]).filterNot { it.goalId == goalId })
        }
    }

    suspend fun addGoalValue(goal: SavingsGoal, valueInCents: Long, note: String?): Result<Unit> = runCatching {
        context.goalsDataStore.edit { preferences ->
            val log = SavingsGoalDeposit(goalId = goal.id, amountInCents = valueInCents, note = note?.trim()?.ifBlank { null })
            val updatedGoals = readGoals(preferences[goalsKey]).map { item ->
                if (item.id == goal.id) {
                    val newValue = item.currentAmountInCents + valueInCents
                    item.copy(currentAmountInCents = newValue, isCompleted = newValue >= item.targetAmountInCents, updatedAt = LocalDateTime.now())
                } else item
            }
            preferences[goalsKey] = gson.toJson(updatedGoals)
            preferences[logsKey] = gson.toJson(readLogs(preferences[logsKey]) + log)
        }
    }

    private fun readGoals(json: String?): List<SavingsGoal> = runCatching {
        if (json.isNullOrBlank()) emptyList() else gson.fromJson<List<SavingsGoal>>(json, goalsType)
    }.getOrDefault(emptyList())

    private fun readLogs(json: String?): List<SavingsGoalDeposit> = runCatching {
        if (json.isNullOrBlank()) emptyList() else gson.fromJson<List<SavingsGoalDeposit>>(json, logsType)
    }.getOrDefault(emptyList())
}
