package com.unotangozero.app.data.models.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Room entity representing a Project.
 */
@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val status: String = "ACTIVE",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate? = null,
    val owner: String = "",
    val color: String = "#4A90E2",
    val progress: Int = 0,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
