package com.unotangozero.app.data.tasks

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.taskDurationDataStore by preferencesDataStore(name = "task_durations")

@Singleton
class TaskDurationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val durationsKey = stringPreferencesKey("durations_json")
    private val mapType = object : TypeToken<Map<String, Int>>() {}.type

    val durations: Flow<Map<String, Int>> = context.taskDurationDataStore.data.map { preferences ->
        parse(preferences[durationsKey])
    }

    suspend fun setDuration(taskId: String, minutes: Int): Result<Unit> = runCatching {
        context.taskDurationDataStore.edit { preferences ->
            val current = parse(preferences[durationsKey]).toMutableMap()
            if (minutes <= 0) current.remove(taskId) else current[taskId] = minutes
            preferences[durationsKey] = gson.toJson(current)
        }
    }

    private fun parse(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching { gson.fromJson<Map<String, Int>>(json, mapType) }.getOrDefault(emptyMap())
    }
}
