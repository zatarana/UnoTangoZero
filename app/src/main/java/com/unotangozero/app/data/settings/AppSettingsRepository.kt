package com.unotangozero.app.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class AppSettings(
    val notificationsEnabled: Boolean = true,
    val defaultReminderHour: Int = 9,
    val defaultReminderMinute: Int = 0
)

private val Context.appSettingsDataStore by preferencesDataStore(name = "app_settings")

@Singleton
class AppSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val notificationsEnabled = booleanPreferencesKey("notifications_enabled")
        val defaultReminderHour = intPreferencesKey("default_reminder_hour")
        val defaultReminderMinute = intPreferencesKey("default_reminder_minute")
    }

    val settings: Flow<AppSettings> = context.appSettingsDataStore.data.map { preferences ->
        AppSettings(
            notificationsEnabled = preferences[Keys.notificationsEnabled] ?: true,
            defaultReminderHour = preferences[Keys.defaultReminderHour] ?: 9,
            defaultReminderMinute = preferences[Keys.defaultReminderMinute] ?: 0
        )
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.notificationsEnabled] = enabled
        }
    }

    suspend fun setDefaultReminderTime(hour: Int, minute: Int) {
        context.appSettingsDataStore.edit { preferences ->
            preferences[Keys.defaultReminderHour] = hour.coerceIn(0, 23)
            preferences[Keys.defaultReminderMinute] = minute.coerceIn(0, 59)
        }
    }
}
