package com.unotangozero.app.data.projects

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
import com.unotangozero.app.domain.models.Project
import com.unotangozero.app.domain.models.ProjectSection
import com.unotangozero.app.domain.models.ProjectTask
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

private val Context.projectsDataStore by preferencesDataStore(name = "projects")

@Singleton
class ProjectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ -> LocalDate.parse(json.asString) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonSerializer<LocalDateTime> { src, _, _ -> JsonPrimitive(src.toString()) })
        .registerTypeAdapter(LocalDateTime::class.java, JsonDeserializer { json, _, _ -> LocalDateTime.parse(json.asString) })
        .create()

    private val projectsKey = stringPreferencesKey("projects_json")
    private val listType = object : TypeToken<List<Project>>() {}.type

    val projects: Flow<List<Project>> = context.projectsDataStore.data.map { preferences ->
        parseProjects(preferences[projectsKey]).filter { !it.isArchived }
    }

    suspend fun save(project: Project): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.filterNot { it.id == project.id } + project.copy(updatedAt = LocalDateTime.now())
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    suspend fun archive(projectId: String): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.map { project ->
                if (project.id == projectId) project.copy(isArchived = true, updatedAt = LocalDateTime.now()) else project
            }
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    suspend fun addSection(projectId: String, title: String): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.map { project ->
                if (project.id == projectId) {
                    project.copy(sections = project.sections + ProjectSection(title = title), updatedAt = LocalDateTime.now())
                } else project
            }
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    suspend fun toggleSection(projectId: String, sectionId: String): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        sections = project.sections.map { section ->
                            if (section.id == sectionId) section.copy(isCollapsed = !section.isCollapsed) else section
                        },
                        updatedAt = LocalDateTime.now()
                    )
                } else project
            }
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    suspend fun addTask(projectId: String, title: String, sectionId: String? = null): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.map { project ->
                if (project.id == projectId) {
                    val task = ProjectTask(title = title)
                    if (sectionId == null) {
                        project.copy(tasks = project.tasks + task, updatedAt = LocalDateTime.now())
                    } else {
                        project.copy(
                            sections = project.sections.map { section ->
                                if (section.id == sectionId) section.copy(tasks = section.tasks + task) else section
                            },
                            updatedAt = LocalDateTime.now()
                        )
                    }
                } else project
            }
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    suspend fun toggleTask(projectId: String, taskId: String): Result<Unit> = runCatching {
        context.projectsDataStore.edit { preferences ->
            val current = parseProjects(preferences[projectsKey])
            val updated = current.map { project ->
                if (project.id == projectId) {
                    project.copy(
                        tasks = project.tasks.map { task ->
                            if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                        },
                        sections = project.sections.map { section ->
                            section.copy(tasks = section.tasks.map { task ->
                                if (task.id == taskId) task.copy(isCompleted = !task.isCompleted) else task
                            })
                        },
                        updatedAt = LocalDateTime.now()
                    )
                } else project
            }
            preferences[projectsKey] = gson.toJson(updated)
        }
    }

    private fun parseProjects(json: String?): List<Project> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching { gson.fromJson<List<Project>>(json, listType) }.getOrDefault(emptyList())
    }
}
