package com.unotangozero.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.unotangozero.app.data.local.entity.FocusSessionEntity
import com.unotangozero.app.data.local.entity.GoalEntity
import com.unotangozero.app.data.local.entity.HabitEntity
import com.unotangozero.app.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity)

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("SELECT * FROM goals ORDER BY deadlineMillis ASC")
    fun observeAll(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE type = :type ORDER BY deadlineMillis ASC")
    fun observeByType(type: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE status = :status ORDER BY deadlineMillis ASC")
    fun observeByStatus(status: String): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE deadlineMillis BETWEEN :startMillis AND :endMillis ORDER BY deadlineMillis ASC")
    fun observeDueBetween(startMillis: Long, endMillis: Long): Flow<List<GoalEntity>>
}

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Update
    suspend fun update(habit: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY title ASC")
    fun observeAll(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE scheduledDateMillis = :dateMillis AND isArchived = 0 ORDER BY title ASC")
    fun getHabitsForDate(dateMillis: Long): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE frequency = :frequency AND isArchived = 0 ORDER BY title ASC")
    fun observeByFrequency(frequency: String): Flow<List<HabitEntity>>

    @Query("UPDATE habits SET streak = streak + 1, completedCount = completedCount + 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun incrementStreak(id: String, updatedAtMillis: Long)

    @Query("UPDATE habits SET streak = 0, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun resetStreak(id: String, updatedAtMillis: Long)

    @Query("UPDATE habits SET isArchived = 1, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun archive(id: String, updatedAtMillis: Long)
}

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("SELECT * FROM tasks ORDER BY dueDateMillis ASC, priority DESC")
    fun observeAll(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status = :status ORDER BY dueDateMillis ASC")
    fun observeByStatus(status: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE dueDateMillis BETWEEN :startMillis AND :endMillis ORDER BY dueDateMillis ASC")
    fun observeDueBetween(startMillis: Long, endMillis: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE categoryId = :categoryId ORDER BY dueDateMillis ASC")
    fun observeByCategory(categoryId: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE projectId = :projectId ORDER BY dueDateMillis ASC")
    fun observeByProject(projectId: String): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET status = :status, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAtMillis: Long)
}

@Dao
interface FocusSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: FocusSessionEntity)

    @Update
    suspend fun update(session: FocusSessionEntity)

    @Delete
    suspend fun delete(session: FocusSessionEntity)

    @Query("SELECT * FROM focus_sessions ORDER BY startedAtMillis DESC")
    fun observeAll(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE taskId = :taskId ORDER BY startedAtMillis DESC")
    fun observeByTask(taskId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE projectId = :projectId ORDER BY startedAtMillis DESC")
    fun observeByProject(projectId: String): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions WHERE startedAtMillis BETWEEN :startMillis AND :endMillis ORDER BY startedAtMillis DESC")
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<FocusSessionEntity>>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM focus_sessions WHERE taskId = :taskId")
    fun observeTotalMinutesByTask(taskId: String): Flow<Int>
}
