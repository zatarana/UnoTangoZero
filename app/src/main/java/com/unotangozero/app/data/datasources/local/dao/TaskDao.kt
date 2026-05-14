package com.unotangozero.app.data.datasources.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotangozero.app.data.models.entities.TaskEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task operations.
 */
@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY dueDate ASC")
    fun getTasksByProjectId(projectId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE date(dueDate) = date('now') AND completed = 0 ORDER BY dueDate ASC")
    fun getTasksDueToday(): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("UPDATE tasks SET completed = 1, completedAt = datetime('now') WHERE id = :id")
    suspend fun completeTask(id: Long)
}
