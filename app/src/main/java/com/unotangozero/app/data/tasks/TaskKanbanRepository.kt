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

private val Context.taskKanbanDataStore by preferencesDataStore(name = "task_kanban")

enum class TaskKanbanColumn(val displayName: String) {
    TODO("A fazer"),
    IN_PROGRESS("Em andamento"),
    DONE("Concluído")
}

@Singleton
class TaskKanbanRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    private val statusesKey = stringPreferencesKey("statuses_json")
    private val mapType = object : TypeToken<Map<String, String>>() {}.type

    val statuses: Flow<Map<String, TaskKanbanColumn>> = context.taskKanbanDataStore.data.map { preferences ->
        parse(preferences[statusesKey])
    }

    suspend fun setColumn(taskId: String, column: TaskKanbanColumn): Result<Unit> = runCatching {
        context.taskKanbanDataStore.edit { preferences ->
            val current = parse(preferences[statusesKey]).toMutableMap()
            current[taskId] = column
            preferences[statusesKey] = gson.toJson(current.mapValues { it.value.name })
        }
    }

    suspend fun clear(taskId: String): Result<Unit> = runCatching {
        context.taskKanbanDataStore.edit { preferences ->
            val current = parse(preferences[statusesKey]).toMutableMap()
            current.remove(taskId)
            preferences[statusesKey] = gson.toJson(current.mapValues { it.value.name })
        }
    }

    private fun parse(json: String?): Map<String, TaskKanbanColumn> {
        if (json.isNullOrBlank()) return emptyMap()
        return runCatching {
            val raw = gson.fromJson<Map<String, String>>(json, mapType)
            raw.mapValues { (_, value) ->
                runCatching { TaskKanbanColumn.valueOf(value) }.getOrDefault(TaskKanbanColumn.TODO)
            }
        }.getOrDefault(emptyMap())
    }
}
