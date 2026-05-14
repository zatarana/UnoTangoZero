package com.unotangozero.app.domain.repositories

import com.unotangozero.app.domain.models.Project
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Project operations.
 */
interface ProjectRepository {
    /**
     * Get all projects as a flow for real-time updates.
     */
    fun getAllProjects(): Flow<List<Project>>

    /**
     * Get a specific project by ID.
     */
    suspend fun getProjectById(id: Long): Project?

    /**
     * Create a new project.
     */
    suspend fun createProject(project: Project): Long

    /**
     * Update an existing project.
     */
    suspend fun updateProject(project: Project)

    /**
     * Delete a project.
     */
    suspend fun deleteProject(id: Long)

    /**
     * Get active projects.
     */
    fun getActiveProjects(): Flow<List<Project>>

    /**
     * Update project progress.
     */
    suspend fun updateProjectProgress(projectId: Long, progress: Int)
}
