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

private val Context.focusSessionDataStore by preferencesDataStore(name = "focus_sessions")

@Singleton
class FocusSessionRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val totalsKey = stringPreferencesKey("focus_seconds_by_task_json")
    private val mapType = object : TypeToken<Map<String, Int>>() {}.type

    val focusSecondsByTask: Flow<Map<String, Int>> = context.focusSessionDataStore.data.map { preferences ->
        parse(preferences[totalsKey])
    }

    suspend fun addFocusedSeconds(taskId: String, seconds: Int): Result<Int> = runCatching {
        if (seconds <= 0) return@runCatching currentSeconds(taskId)
        var updatedSeconds = 0
        context.focusSessionDataStore.edit { preferences ->
            val current = parse(preferences[totalsKey]).toMutableMap()
            updatedSeconds = ((current[taskId] ?: 0) + seconds).coerceAtLeast(0)
            current[taskId] = updatedSeconds
            preferences[totalsKey] = gson.toJson(current)
        }
        updatedSeconds
    }

    suspend fun clearTask(taskId: String): Result<Unit> = runCatching {
        context.focusSessionDataStore.edit { preferences ->
            val current = parse(preferences[totalsKey]).toMutableMap()
            current.remove(taskId)
            preferences[totalsKey] = gson.toJson(current)
        }
    }

    private suspend fun currentSeconds(taskId: String): Int {
        var value = 0
        context.focusSessionDataStore.edit { preferences ->
            value = parse(preferences[totalsKey])[taskId] ?: 0
        }
        return value
    }

    private fun parse(json: String?): Map<String, Int> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching { gson.fromJson<Map<String, Int>>(json, mapType) }.getOrDefault(emptyMap())
    }
}
