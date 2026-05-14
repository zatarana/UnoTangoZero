package com.unotangozero.app.domain.models

import java.time.LocalDateTime

/**
 * Domain model representing a task.
 */
data class Task(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val projectId: Long? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDateTime? = null,
    val completed: Boolean = false,
    val completedAt: LocalDateTime? = null,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val remindAt: LocalDateTime? = null,
    val tags: List<String> = emptyList()
)

enum class Priority {
    LOW, MEDIUM, HIGH, URGENT
}
