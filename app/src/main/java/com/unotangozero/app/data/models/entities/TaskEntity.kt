package com.unotangozero.app.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

/**
 * Room entity representing a Task.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val projectId: Long? = null,
    val priority: String = "MEDIUM",
    val dueDate: LocalDateTime? = null,
    val completed: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val remindAt: LocalDateTime? = null,
    val tags: String = "" // JSON string
)
