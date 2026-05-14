package com.unotangozero.app.domain.repositories

import com.unotangozero.app.domain.models.Task
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for Task operations.
 */
interface TaskRepository {
    /**
     * Get all tasks as a flow for real-time updates.
     */
    fun getAllTasks(): Flow<List<Task>>

    /**
     * Get tasks by project ID.
     */
    fun getTasksByProjectId(projectId: Long): Flow<List<Task>>

    /**
     * Get a specific task by ID.
     */
    suspend fun getTaskById(id: Long): Task?

    /**
     * Create a new task.
     */
    suspend fun createTask(task: Task): Long

    /**
     * Update an existing task.
     */
    suspend fun updateTask(task: Task)

    /**
     * Delete a task.
     */
    suspend fun deleteTask(id: Long)

    /**
     * Mark task as completed.
     */
    suspend fun completeTask(id: Long)

    /**
     * Get tasks due today.
     */
    fun getTasksDueToday(): Flow<List<Task>>
}
