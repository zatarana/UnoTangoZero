package com.unotangozero.app.data.focus

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
import com.unotangozero.app.domain.models.FocusProfile
import com.unotangozero.app.domain.models.FocusSessionLog
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.focusDataStore by preferencesDataStore(name = "focus_timer")

@Singleton
class FocusRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString) })
        .create()

    private val profilesKey = stringPreferencesKey("profiles_json")
    private val logsKey = stringPreferencesKey("logs_json")
    private val profilesType = object : TypeToken<List<FocusProfile>>() {}.type
    private val logsType = object : TypeToken<List<FocusSessionLog>>() {}.type

    private val defaultProfiles = listOf(
        FocusProfile("classic", "Clássico", 25, 5, 20, 4, 4),
        FocusProfile("deep_work", "Deep Work", 90, 20, 0, 99, 2),
        FocusProfile("quick_review", "Revisão rápida", 15, 3, 0, 99, 4)
    )

    val profiles: Flow<List<FocusProfile>> = context.focusDataStore.data.map { preferences ->
        val saved = parseProfiles(preferences[profilesKey])
        if (saved.isEmpty()) defaultProfiles else saved
    }

    val logs: Flow<List<FocusSessionLog>> = context.focusDataStore.data.map { preferences ->
        parseLogs(preferences[logsKey]).sortedByDescending { it.createdAt }
    }

    suspend fun saveProfile(profile: FocusProfile): Result<Unit> = runCatching {
        context.focusDataStore.edit { preferences ->
            val current = parseProfiles(preferences[profilesKey]).ifEmpty { defaultProfiles }
            val updated = current.filterNot { it.id == profile.id } + profile
            preferences[profilesKey] = gson.toJson(updated)
        }
    }

    suspend fun addLog(log: FocusSessionLog): Result<Unit> = runCatching {
        context.focusDataStore.edit { preferences ->
            val current = parseLogs(preferences[logsKey])
            preferences[logsKey] = gson.toJson((current + log).takeLast(200))
        }
    }

    private fun parseProfiles(json: String?): List<FocusProfile> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<FocusProfile>>(json, profilesType) }.getOrDefault(emptyList())
    }

    private fun parseLogs(json: String?): List<FocusSessionLog> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<FocusSessionLog>>(json, logsType) }.getOrDefault(emptyList())
    }
}
