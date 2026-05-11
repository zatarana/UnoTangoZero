package com.unotangozero.app.domain.models

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Project(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String? = null,
    val deadline: LocalDate? = null,
    val sections: List<ProjectSection> = emptyList(),
    val tasks: List<ProjectTask> = emptyList(),
    val isArchived: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    val allTasks: List<ProjectTask>
        get() = tasks + sections.flatMap { it.tasks }

    val totalTasks: Int
        get() = allTasks.size

    val completedTasks: Int
        get() = allTasks.count { it.isCompleted }

    val progressPercent: Int
        get() = if (totalTasks == 0) 0 else ((completedTasks.toDouble() / totalTasks.toDouble()) * 100).toInt()
}

data class ProjectSection(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCollapsed: Boolean = false,
    val tasks: List<ProjectTask> = emptyList(),
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    val totalTasks: Int
        get() = tasks.size

    val completedTasks: Int
        get() = tasks.count { it.isCompleted }
}

data class ProjectTask(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val isCompleted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now()
)
