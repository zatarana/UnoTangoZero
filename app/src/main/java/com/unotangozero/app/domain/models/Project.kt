package com.unotangozero.app.domain.models

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Domain model representing a project.
 */
data class Project(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val status: ProjectStatus = ProjectStatus.ACTIVE,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val owner: String = "",
    val color: String = "#4A90E2",
    val progress: Int = 0,
    val tasks: List<Task> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class ProjectStatus {
    PLANNING, ACTIVE, ON_HOLD, COMPLETED, CANCELLED
}
