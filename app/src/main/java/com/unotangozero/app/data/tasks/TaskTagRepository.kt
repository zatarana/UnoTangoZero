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

private val Context.taskTagDataStore by preferencesDataStore(name = "task_tags")

@Singleton
class TaskTagRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val tagsKey = stringPreferencesKey("tags_json")
    private val mapType = object : TypeToken<Map<String, List<String>>>() {}.type

    val tags: Flow<Map<String, List<String>>> = context.taskTagDataStore.data.map { preferences ->
        parse(preferences[tagsKey])
    }

    suspend fun setTags(taskId: String, tags: List<String>): Result<Unit> = runCatching {
        context.taskTagDataStore.edit { preferences ->
            val current = parse(preferences[tagsKey]).toMutableMap()
            val normalized = tags
                .map { it.trim().lowercase() }
                .filter { it.isNotBlank() }
                .distinct()
            if (normalized.isEmpty()) current.remove(taskId) else current[taskId] = normalized
            preferences[tagsKey] = gson.toJson(current)
        }
    }

    suspend fun clear(taskId: String): Result<Unit> = runCatching {
        context.taskTagDataStore.edit { preferences ->
            val current = parse(preferences[tagsKey]).toMutableMap()
            current.remove(taskId)
            preferences[tagsKey] = gson.toJson(current)
        }
    }

    private fun parse(json: String?): Map<String, List<String>> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            gson.fromJson<Map<String, List<String>>>(json, mapType)
        }.getOrDefault(emptyMap())
    }
}
